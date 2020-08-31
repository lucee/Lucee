package lucee.runtime.tag.listener;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;

public interface TagListener {
	public Struct before(PageContext pc, Struct args) throws PageException;

	public Struct after(PageContext pc, Struct args) throws PageException;

	public boolean hasError();

	public Struct error(PageContext pc, Struct args) throws PageException;

	public static Object toCFML(TagListener tl, Object defaultValue) {
		if (tl == null) return defaultValue;

		if (tl instanceof ComponentTagListener) return ((ComponentTagListener) tl).getComponent();
		else if (tl instanceof UDFTagListener) return ((UDFTagListener) tl).getStruct();
		else return defaultValue;
	}
}
