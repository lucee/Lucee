package lucee.runtime.engine.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import lucee.runtime.engine.CFMLEngineImpl;

public class CFMLServletContextListener implements ServletContextListener {

	private CFMLEngineImpl engine;

	public CFMLServletContextListener(CFMLEngineImpl engine) {
		this.engine = engine;
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		engine.reset();
	}
}