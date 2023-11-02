package lucee.loader.servlet;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import lucee.cli.util.EnumerationWrapper;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;

public class LuceeFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		try {
			CFMLEngine engine = CFMLEngineFactory.getInstance();
			// FUTURE add exeFilter
			engine.addServletConfig(new LuceeFilterImpl(request, response, chain, "filter"));
		}
		catch (Exception se) {
			se.printStackTrace();
		}
	}

	@Override
	public void destroy() {

	}

	public static class LuceeFilterImpl implements ServletConfig {

		private ServletRequest request;
		private ServletResponse response;
		private FilterChain chain;
		private String status;

		public LuceeFilterImpl(ServletRequest request, ServletResponse response, FilterChain chain, String status) {
			this.request = request;
			this.response = response;
			this.chain = chain;
			this.status = status;
		}

		@Override
		public String getServletName() {
			return "LuceeFilter";
		}

		@Override
		public ServletContext getServletContext() {
			return request.getServletContext();
		}

		public ServletRequest getServletRequest() {
			return request;
		}

		public ServletResponse getServletResponse() {
			return response;
		}

		public FilterChain getFilterChain() {
			return chain;
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
