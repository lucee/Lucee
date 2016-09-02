package lucee.runtime.page.engine;

import lucee.loader.engine.CFMLEngine;
import lucee.runtime.config.Config;

public class CFPageEngine extends CorePageEngine {
	
	public CFPageEngine(Config cfg) {super(cfg);}

	@Override
	int _getDialect() {
		return CFMLEngine.DIALECT_CFML;
	}
	
}