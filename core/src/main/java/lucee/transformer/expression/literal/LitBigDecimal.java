package lucee.transformer.expression.literal;

import java.math.BigDecimal;

public interface LitBigDecimal extends LitNumber, Literal {

	public BigDecimal getBigDecimal();
}