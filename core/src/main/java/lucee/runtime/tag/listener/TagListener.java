package lucee.runtime.tag.listener;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;

public interface TagListener {
	public Struct before(PageContext pc, Struct args) throws PageException;

	public Struct after(PageContext pc, Struct args) throws PageException;

	public Struct fail(PageContext pc, Struct args) throws PageException;
}
