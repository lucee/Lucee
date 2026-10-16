package lucee.runtime.component;

import java.lang.ref.WeakReference;

import lucee.commons.io.SystemUtil;
import lucee.runtime.ComponentPageImpl;
import lucee.runtime.PageSource;
import lucee.runtime.exp.PageException;

public class ComponentPageRef {

	private PageSource ps;
	private WeakReference<ComponentPageImpl> ref;

	public ComponentPageRef(ComponentPageImpl cp) {
		this.ref = new WeakReference<ComponentPageImpl>(cp);
		ps = cp.getPageSource();
	}

	public ComponentPageImpl get() throws PageException {

		ComponentPageImpl cp = ref.get();
		if (cp == null) {
			synchronized (SystemUtil.createToken("ComponentPageRef", "" + ps.hashCode())) {
				cp = ref.get();
				if (cp == null) {
					cp = (ComponentPageImpl) ps.loadPage(null, false);
					this.ref = new WeakReference<ComponentPageImpl>(cp);
				}
			}

		}
		return cp;
	}

	public ComponentPageImpl get(ComponentPageImpl defaultValue) {
		try {
			return get();
		}
		catch (Exception e) {
			return defaultValue;
		}
	}
}
