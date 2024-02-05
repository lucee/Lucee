package lucee.transformer.expression.var;

import lucee.transformer.expression.ExprString;

public interface NamedMember extends Member {
	/**
	 * @return the name
	 */
	public ExprString getName();
}
