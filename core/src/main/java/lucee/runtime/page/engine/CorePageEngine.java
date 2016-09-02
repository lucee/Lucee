package lucee.runtime.page.engine;

import lucee.runtime.config.Config;
import lucee.runtime.page.engine.PageEngine;
import lucee.runtime.page.engine.PageFactory;

public abstract class CorePageEngine extends PageEngine {
	
	protected final Config cfg;
	protected final PageFactory pageFactory;
	
	public CorePageEngine(Config cfg) {
		this.cfg = cfg;
		pageFactory = new CorePageFactory(this);
	}
	
	@Override
	public PageFactory getPageFactory() {
		return pageFactory;
	}
	
}
