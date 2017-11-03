package lucee.runtime.spooler;

import lucee.commons.io.SystemUtil.TemplateLine;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;

public class ComponentSpoolerTaskListener extends CFMLSpoolerTaskListener {

	private static final long serialVersionUID = -4726393142628827635L;
	private Component component;

	public ComponentSpoolerTaskListener(TemplateLine currTemplate, SpoolerTask task, Component component) {
		super(currTemplate, task);
		this.component=component;
	}

	@Override
	public void _listen(PageContext pc, Struct args) throws PageException {
		component.callWithNamedValues(pc, "listen", args);
	}

}
