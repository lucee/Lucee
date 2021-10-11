package lucee.transformer.bytecode.literal;

import java.math.BigDecimal;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.runtime.op.Caster;
import lucee.runtime.type.LiteralValue;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
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
	private static final Method TO_NUMBER_LONG_VALUE = new Method("toNumber", Types.NUMBER, new Type[] { Types.PAGE_CONTEXT, Types.LONG_VALUE });
	private static final Method TO_NUMBER_STRING = new Method("toNumber", Types.NUMBER, new Type[] { Types.PAGE_CONTEXT, Types.STRING });

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
		return getBigDecimal();
	}

	@Override
	public Number getNumber(Number defaultValue) {
		return getBigDecimal();
	}

	@Override
	public String getString() {
		return number;
	}

	@Override
	public Boolean getBoolean(Boolean defaultValue) {
		return getBigDecimal().compareTo(BigDecimal.ZERO) != 0;
	}

	public BigDecimal getBigDecimal() {
		if (bd == null) bd = new BigDecimal(number);
		return bd;
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();

		if (MODE_VALUE == mode) {
			adapter.push(getBigDecimal().doubleValue());
			// print.ds();
			return Types.DOUBLE_VALUE;
		}

		Long l = justNumberDigits(number) ? Caster.toLong(number, null) : null;
		if (l != null) {
			adapter.loadArg(0);
			adapter.push(l.longValue());
			adapter.invokeStatic(LITERAL_VALUE, TO_NUMBER_LONG_VALUE);
		}
		else {
			adapter.loadArg(0);
			adapter.push(number);
			adapter.invokeStatic(LITERAL_VALUE, TO_NUMBER_STRING);
		}

		// adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_BIG_DECIMAL_STR); // TODOX call constructor
		// directly
		return Types.NUMBER;
	}

	private static boolean justNumberDigits(String number) {
		for (char c: number.toCharArray()) {
			if (c >= '0' && c <= '9') continue;
			return false;
		}

		return true;
	}

}