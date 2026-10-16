package lucee.runtime.op;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

public interface CastablePro extends Castable {

	public String castToString(PageContext pc) throws PageException;

	public String castToString(PageContext pc, String defaultValue);
}
