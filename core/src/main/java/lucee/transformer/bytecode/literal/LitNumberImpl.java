package lucee.transformer.bytecode.literal;

import java.math.BigDecimal;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.LiteralValue;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.ExprNumber;
import lucee.transformer.expression.literal.LitNumber;

/**
 * A Literal String
 */
public class LitNumberImpl extends ExpressionBase implements LitNumber, ExprNumber {

	private static final Type LITERAL_VALUE = Type.getType(LiteralValue.class);

	private static final Method CONSTR_STRING = new Method("<init>", Types.VOID, new Type[] { Types.STRING });
	private static final Method VALUE_OF = new Method("valueOf", Types.BIG_DECIMAL, new Type[] { Types.LONG_VALUE });
	private static final Method TO_NUMBER_LONG_VALUE_1 = new Method("toNumber", Types.NUMBER, new Type[] { Types.LONG_VALUE });
	private static final Method TO_NUMBER_LONG_VALUE_2 = new Method("toNumber", Types.NUMBER, new Type[] { Types.PAGE_CONTEXT, Types.LONG_VALUE });
	private static final Method TO_NUMBER_STRING_1 = new Method("toNumber", Types.NUMBER, new Type[] { Types.STRING });
	private static final Method TO_NUMBER_STRING_2 = new Method("toNumber", Types.NUMBER, new Type[] { Types.PAGE_CONTEXT, Types.STRING });

	private static final int CONSTANT_LENGTH = 21;
	private static final Method[] CONSTANTS_0 = new Method[CONSTANT_LENGTH];
	private static final Method[] CONSTANTS_1 = new Method[CONSTANT_LENGTH];

	static {
		for (int i = 0; i < CONSTANT_LENGTH; i++) {
			CONSTANTS_0[i] = new Method("l" + i, Types.NUMBER, new Type[] {});
			CONSTANTS_1[i] = new Method("l" + i, Types.NUMBER, new Type[] { Types.PAGE_CONTEXT });
		}
	}

	private String number;
	private BigDecimal bd;

	public LitNumberImpl(Factory f, String number, Position start, Position end) {
		super(f, start, end);
		this.number = number;
	}

	public LitNumberImpl(Factory f, BigDecimal bd, Position start, Position end) {
		super(f, start, end);
		this.bd = bd;
		this.number = Caster.toString(bd);

	}

	@Override
	public Number getNumber() {
		try {
			return getBigDecimal();
		}
		catch (CasterException e) {
			throw new PageRuntimeException(e);
		}
	}

	@Override
	public Number getNumber(Number defaultValue) {
		try {
			return getBigDecimal();
		}
		catch (CasterException e) {
			return defaultValue;
		}
	}

	@Override
	public String getString() {
		return number;
	}

	@Override
	public Boolean getBoolean(Boolean defaultValue) {
		try {
			return getBigDecimal().compareTo(BigDecimal.ZERO) != 0;
		}
		catch (CasterException e) {
			return defaultValue;
		}
	}

	public BigDecimal getBigDecimal() throws CasterException {
		if (bd == null) bd = Caster.toBigDecimal(number);
		return bd;
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();

		// are we within a method not providing PageContext as first argument?
		boolean firstIsPC = ASMUtil.isFirstArgumentPageContext(bc);

		if (MODE_VALUE == mode) {
			try {
				adapter.push(getBigDecimal().doubleValue());
			}
			catch (CasterException e) {
				new TransformerException(bc, e, getStart());
			}
			return Types.DOUBLE_VALUE;
		}
		Long l = justNumberDigits(number) ? Caster.toLong(number, null) : null;

		if (l != null && Caster.toString(l).equals(number)) {
			if (firstIsPC) adapter.loadArg(0);

			if (l.longValue() >= 0L && l.longValue() < CONSTANT_LENGTH) {
				int idx = (int) l.longValue();
				adapter.invokeStatic(LITERAL_VALUE, firstIsPC ? CONSTANTS_1[idx] : CONSTANTS_0[idx]);
			}
			else {
				adapter.push(l.longValue());
				adapter.invokeStatic(LITERAL_VALUE, firstIsPC ? TO_NUMBER_LONG_VALUE_2 : TO_NUMBER_LONG_VALUE_1);
			}
		}
		else {
			if (firstIsPC) adapter.loadArg(0);
			adapter.push(number);
			adapter.invokeStatic(LITERAL_VALUE, firstIsPC ? TO_NUMBER_STRING_2 : TO_NUMBER_STRING_1);
		}

		// adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_BIG_DECIMAL_STR); // TODOX call constructor
		// directly
		return Types.NUMBER;
	}

	private static boolean justNumberDigits(String number) {
		int idx = 0;
		for (char c: number.toCharArray()) {
			if (c >= '0' && c <= '9') continue;
			if (idx++ == 0 && c == '-') continue;
			return false;
		}

		return true;
	}

}