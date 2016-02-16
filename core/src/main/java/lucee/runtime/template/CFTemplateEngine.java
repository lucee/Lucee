package lucee.runtime.template;

import lucee.loader.engine.CFMLEngine;
import lucee.runtime.config.Config;

public class CFTemplateEngine extends CoreTemplateEngine {
	
	public CFTemplateEngine(Config cfg) {super(cfg);}

	@Override
	int _getDialect() {
		return CFMLEngine.DIALECT_CFML;
	}
	
}
