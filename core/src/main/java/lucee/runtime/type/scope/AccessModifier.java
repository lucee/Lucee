package lucee.runtime.type.scope;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection.Key;

public interface AccessModifier {
	public Object set(PageContext pc, Key key, Object value, int access, int modifier) throws PageException;
}
