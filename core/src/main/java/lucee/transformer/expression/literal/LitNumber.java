package lucee.transformer.expression.literal;

import lucee.transformer.expression.ExprNumber;

public interface LitNumber extends Literal, ExprNumber {

	/**
	 * @return return value as a boolean value
	 */
	public Number getNumber();

}
