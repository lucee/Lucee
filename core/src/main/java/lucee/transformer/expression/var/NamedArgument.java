package lucee.transformer.expression.var;

import lucee.transformer.expression.Expression;

public interface NamedArgument extends Argument {

	public static final char SEPARATOR_COLON = ':';
	public static final char SEPARATOR_EQUALS = '=';

	public Expression getName();

	/**
	 * Returns the separator character used between key and value.
	 * Returns ':' for colon syntax (key: value) or '=' for equals syntax (key=value).
	 */
	public char getSeparator();
}
