package lucee.transformer.interpreter.literal;

import java.math.BigDecimal;

import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.expression.ExprNumber;
import lucee.transformer.expression.literal.LitBigDecimal;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

/**
 * A Literal String
 */
public class LitBigDecimalImpl extends ExpressionBase implements LitBigDecimal, ExprNumber {

	private BigDecimal bd;
	private String number;

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
	public Class<?> _writeOut(InterpreterContext ic, int mode) {
		ic.stack(getBigDecimal());
		return BigDecimal.class;
	}

}