package lucee.runtime.functions.closure;

import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

public interface ClosureFunc {

	public static final short TYPE_UNDEFINED = 0;
	public static final short TYPE_STRUCT = 1;
	public static final short TYPE_ARRAY = 2;
	public static final short TYPE_QUERY = 4;


	/**
	 * casts the result of a closure to a boolean, when the closure returned no value, a clear error
	 * message is thrown instead of a confusing cast error ("Can't cast String [] to a boolean")
	 * 
	 * @param res result of the closure call
	 * @param functionName name of the function (some, every, filter) calling the closure
	 * @return boolean value of the result
	 * @throws PageException
	 */
	public static boolean toBooleanResult(Object res, String functionName) throws PageException {
		if (res == null) throw new ExpressionException("The closure passed to the function [" + functionName + "] did not return a value, it must return a boolean value");
		return Caster.toBooleanValue(res);
	}

}
