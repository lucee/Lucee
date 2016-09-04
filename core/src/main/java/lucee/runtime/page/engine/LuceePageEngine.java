package lucee.runtime.page.engine;

import lucee.loader.engine.CFMLEngine;
import lucee.runtime.config.Config;

public class LuceePageEngine extends CorePageEngine {

	public LuceePageEngine(Config cfg) {
		super(cfg);
		// when the time comes to switch it on by default ;)
		//((ConfigImpl)cfg).setAllowLuceeDialect(true);
	}

	@Override
	public int getDialect() {
		return CFMLEngine.DIALECT_LUCEE;
	}
	
	
}
