package lucee.runtime.exp;

import lucee.runtime.PageContext;

public class ParentException extends Exception {
	private static final long serialVersionUID = 8698505541398848801L;

	public ParentException(PageContext parent) {

		setStackTrace(parent.getThread().getStackTrace());

		PageContext gp = parent.getParentPageContext();
		if (gp != null && gp != parent) {
			this.initCause(new ParentException(gp));
		}
	}
}
