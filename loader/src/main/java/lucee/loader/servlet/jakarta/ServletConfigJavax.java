package lucee.loader.servlet.jakarta;

import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class ServletConfigJavax implements ServletConfig, Jakarta {

	private jakarta.servlet.ServletConfig config;
	private ServletContextJavax context;
	private static Map<jakarta.servlet.ServletConfig, ServletConfigJavax> configs = new IdentityHashMap<>();

	public static ServletConfig getInstance(jakarta.servlet.ServletConfig servletConfig) {
		ServletConfigJavax c = configs.get(servletConfig);
		if (c == null) {
			synchronized (configs) {
				c = configs.get(servletConfig);
				if (c == null) {
					c = new ServletConfigJavax(servletConfig);
					configs.put(servletConfig, c);
				}
			}
		}
		return c;
	}

	private ServletConfigJavax(jakarta.servlet.ServletConfig config) {
		this.config = config;
	}

	@Override
	public String getServletName() {
		return config.getServletName();
	}

	@Override
	public ServletContext getServletContext() {
		if (context == null) {
			synchronized (config) {
				if (context == null) {
					context = new ServletContextJavax(config.getServletContext());
				}
			}
		}
		return context;
	}

	@Override
	public String getInitParameter(String name) {
		return config.getInitParameter(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return config.getInitParameterNames();
	}

	@Override
	public Object getJakartaInstance() {
		return config;
	}

}
