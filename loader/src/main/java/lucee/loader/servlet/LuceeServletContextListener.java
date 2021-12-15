package lucee.loader.servlet;

import java.util.Enumeration;
import java.util.HashSet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import lucee.cli.util.EnumerationWrapper;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;

@WebListener
public class LuceeServletContextListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			CFMLEngine engine = CFMLEngineFactory.getInstance();
			// FUTURE add exeServletContextEvent
			engine.addServletConfig(new LuceeServletContextListenerImpl(sce, "init"));
		}
		catch (Exception se) {
			se.printStackTrace();
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		try {
			CFMLEngine engine = CFMLEngineFactory.getInstance();
			// FUTURE add addServletContextEvent
			engine.addServletConfig(new LuceeServletContextListenerImpl(sce, "release"));
		}
		catch (Exception se) {
			se.printStackTrace();
		}
	}

	public static class LuceeServletContextListenerImpl implements ServletConfig {

		private ServletContextEvent sce;
		private String status;

		public LuceeServletContextListenerImpl(ServletContextEvent sce, String status) {
			this.sce = sce;
			this.status = status;
		}

		@Override
		public String getServletName() {
			return "LuceeServletContextListener";
		}

		@Override
		public ServletContext getServletContext() {
			return sce.getServletContext();
		}

		public ServletContextEvent getServletContextEvent() {
			return sce;
		}

		@Override
		public String getInitParameter(String name) {
			if ("status".equalsIgnoreCase(name)) return status;
			return null;
		}

		@Override
		public Enumeration<String> getInitParameterNames() {
			HashSet<String> set = new HashSet<String>();
			set.add("status");
			return new EnumerationWrapper<String>(set);
		}

	}
}
