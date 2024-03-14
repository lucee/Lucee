package lucee.servlet.http;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

public class HTTPServletImpl extends HttpServlet {
	private static final long serialVersionUID = 3270816399105433603L;

	private final ServletConfig config;
	private final ServletContext context;
	private final String servletName;

	public HTTPServletImpl(final ServletConfig config, final ServletContext context, final String servletName) {
		this.config = config;
		this.context = context;
		this.servletName = servletName;
	}

	@Override
	public ServletConfig getServletConfig() {
		return config;
	}

	@Override
	public ServletContext getServletContext() {
		return context;
	}

	@Override
	public String getServletName() {
		return servletName;
	}

}