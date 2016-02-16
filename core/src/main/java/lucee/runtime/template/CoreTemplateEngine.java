package lucee.runtime.template;

import lucee.runtime.config.Config;

public abstract class CoreTemplateEngine extends TemplateEngine {
	
	protected final Config cfg;
	protected final TemplatePageFactory pageFactory;
	
	public CoreTemplateEngine(Config cfg) {
		this.cfg = cfg;
		pageFactory = new CorePageFactory(this);
	}
	
	@Override
	public TemplatePageFactory getPageFactory() {
		return pageFactory;
	}
	
}
