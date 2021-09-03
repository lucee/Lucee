package lucee.transformer.bytecode.literal;

import java.math.BigDecimal;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.Methods;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.ExprNumber;
import lucee.transformer.expression.literal.LitBigDecimal;

/**
 * A Literal String
 */
public class LitBigDecimalImpl extends ExpressionBase implements LitBigDecimal, ExprNumber {
	private String number;
	private BigDecimal bd;

	public LitBigDecimalImpl(Factory f, String number, Position start, Position end) {
		super(f, start, end);
		this.number = number;

	}

	public LitBigDecimalImpl(Factory f, BigDecimal bd, Position start, Position end) {
		super(f, start, end);
		this.bd = bd;
		this.number = bd.toPlainString();

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

	@Override
	public BigDecimal getBigDecimal() {
		if (bd == null) bd = new BigDecimal(number);
		return bd;
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		adapter.push(number);
		adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_BIG_DECIMAL_STR); // TODOX call constructor directly
		return Types.BIG_DECIMAL;
	}

}