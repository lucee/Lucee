package lucee.runtime.template;

import lucee.loader.engine.CFMLEngine;
import lucee.runtime.config.Config;

public class LuceeTemplateEngine extends CoreTemplateEngine {

	public LuceeTemplateEngine(Config cfg) {
		super(cfg);
		// when the time comes to switch it on by default ;)
		//((ConfigImpl)cfg).setAllowLuceeDialect(true);
	}

	@Override
	int _getDialect() {
		return CFMLEngine.DIALECT_LUCEE;
	}
	
}
