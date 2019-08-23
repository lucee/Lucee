/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package lucee.cli.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

import org.apache.felix.framework.Logger;

import lucee.cli.util.EnumerationWrapper;

public class ServletContextImpl implements ServletContext {
	private final Map<String, Object> attributes;
	private final Map<String, String> parameters;
	private final int majorVersion;
	private final int minorVersion;
	private final File root;
	private Logger logger;

	public ServletContextImpl(final File root, final Map<String, Object> attributes, final Map<String, String> parameters, final int majorVersion, final int minorVersion) {
		this.root = root;
		this.attributes = attributes;
		this.parameters = parameters;
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
	}

	/**
	 * @see javax.servlet.ServletContext#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute(final String key) {
		return attributes.get(key);
	}

	/**
	 * @see javax.servlet.ServletContext#getAttributeNames()
	 */
	@Override
	public Enumeration<String> getAttributeNames() {
		return new EnumerationWrapper<String>(attributes);
	}

	/**
	 * @see javax.servlet.ServletContext#getInitParameter(java.lang.String)
	 */
	@Override
	public String getInitParameter(final String key) {
		return parameters.get(key);
	}

	/**
	 * @see javax.servlet.ServletContext#getInitParameterNames()
	 */
	@Override
	public Enumeration<String> getInitParameterNames() {
		return new EnumerationWrapper<String>(parameters);
	}

	/**
	 * @see javax.servlet.ServletContext#getMajorVersion()
	 */
	@Override
	public int getMajorVersion() {
		return majorVersion;
	}

	/**
	 * @see javax.servlet.ServletContext#getMinorVersion()
	 */
	@Override
	public int getMinorVersion() {
		return minorVersion;
	}

	/**
	 * @see javax.servlet.ServletContext#getMimeType(java.lang.String)
	 */
	@Override
	public String getMimeType(final String file) {
		throw notSupported("getMimeType(String file)");
	}

	/**
	 * @see javax.servlet.ServletContext#getRealPath(java.lang.String)
	 */
	@Override
	public String getRealPath(final String realpath) {
		return getRealFile(realpath).getAbsolutePath();
	}

	/**
	 * @see javax.servlet.ServletContext#getResource(java.lang.String)
	 */
	@Override
	public URL getResource(final String realpath) throws MalformedURLException {
		final File file = getRealFile(realpath);
		return file.toURI().toURL();
	}

	/**
	 * @see javax.servlet.ServletContext#getResourceAsStream(java.lang.String)
	 */
	@Override
	public InputStream getResourceAsStream(final String realpath) {
		try {
			return new FileInputStream(getRealFile(realpath));
		}
		catch (final IOException e) {
			return null;
		}
	}

	public File getRealFile(final String realpath) {
		return new File(root, realpath);
	}

	public File getRoot() {
		return root;
	}

	@Override
	public Set<String> getResourcePaths(final String realpath) {
		throw notSupported("getResourcePaths(String realpath)");
	}

	@Override
	public RequestDispatcher getRequestDispatcher(final String path) {
		throw notSupported("getNamedDispatcher(String name)");
	}

	@Override
	public ServletContext getContext(final String key) {
		// TODO ?
		return this;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(final String name) {
		throw notSupported("getNamedDispatcher(String name)");
	}

	/**
	 * @see javax.servlet.ServletContext#log(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void log(final String msg, final Throwable t) {// TODO better
		if (logger == null) return;

		if (t == null) logger.log(Logger.LOG_INFO, msg);
		else logger.log(Logger.LOG_ERROR, msg, t);
	}

	/**
	 * @see javax.servlet.ServletContext#log(java.lang.Exception, java.lang.String)
	 */
	@Override
	public void log(final Exception e, final String msg) {
		log(msg, e);
	}

	/**
	 * @see javax.servlet.ServletContext#log(java.lang.String)
	 */
	@Override
	public void log(final String msg) {
		log(msg, null);
	}

	/**
	 * @see javax.servlet.ServletContext#removeAttribute(java.lang.String)
	 */
	@Override
	public void removeAttribute(final String key) {
		attributes.remove(key);
	}

	/**
	 * @see javax.servlet.ServletContext#setAttribute(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setAttribute(final String key, final Object value) {
		attributes.put(key, value);
	}

	@Override
	public String getServletContextName() {
		// can return null
		return null;
	}

	@Override
	public String getServerInfo() {
		// deprecated
		throw notSupported("getServlet()");
	}

	@Override
	public Servlet getServlet(final String arg0) throws ServletException {
		// deprecated
		throw notSupported("getServlet()");
	}

	@Override
	public Enumeration<String> getServletNames() {
		// deprecated
		throw notSupported("getServlet()");
	}

	@Override
	public Enumeration<Servlet> getServlets() {
		// deprecated
		throw notSupported("getServlet()");
	}

	private RuntimeException notSupported(final String method) {
		throw new RuntimeException(new ServletException("method " + method + " not supported"));
	}

	@Override
	public Dynamic addFilter(final String arg0, final String arg1) {
		throw notSupported("");
	}

	@Override
	public Dynamic addFilter(final String arg0, final Filter arg1) {
		throw notSupported("");
	}

	@Override
	public Dynamic addFilter(final String arg0, final Class<? extends Filter> arg1) {
		throw notSupported("");
	}

	@Override
	public void addListener(final String arg0) {
		throw notSupported("");
	}

	@Override
	public <T extends EventListener> void addListener(final T arg0) {
		throw notSupported("");
	}

	@Override
	public void addListener(final Class<? extends EventListener> arg0) {
		throw notSupported("");
	}

	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(final String arg0, final String arg1) {
		throw notSupported("");
	}

	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(final String arg0, final Servlet arg1) {
		throw notSupported("");
	}

	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(final String arg0, final Class<? extends Servlet> arg1) {
		throw notSupported("addServlet");
	}

	@Override
	public <T extends Filter> T createFilter(final Class<T> arg0) throws ServletException {
		throw notSupported("createFilter");
	}

	@Override
	public <T extends EventListener> T createListener(final Class<T> arg0) throws ServletException {
		throw notSupported("createListener");
	}

	@Override
	public <T extends Servlet> T createServlet(final Class<T> arg0) throws ServletException {
		throw notSupported("createServlet");
	}

	@Override
	public void declareRoles(final String... arg0) {
		throw notSupported("declareRoles(String ...)");

	}

	@Override
	public ClassLoader getClassLoader() {
		return this.getClass().getClassLoader();
	}

	@Override
	public String getContextPath() {
		return root.getAbsolutePath();
	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		throw notSupported("getDefaultSessionTrackingModes()");
	}

	@Override
	public int getEffectiveMajorVersion() {
		return getMajorVersion();
	}

	@Override
	public int getEffectiveMinorVersion() {
		return getMinorVersion();
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		throw notSupported("getEffectiveSessionTrackingModes()");
	}

	@Override
	public FilterRegistration getFilterRegistration(final String arg0) {
		throw notSupported("getFilterRegistration(String)");
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		throw notSupported("getFilterRegistrations()");
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		throw notSupported("getJspConfigDescriptor()");
	}

	@Override
	public ServletRegistration getServletRegistration(final String arg0) {
		throw notSupported("getServletRegistration(String)");
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		throw notSupported("getServletRegistrations()");
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		throw notSupported("getSessionCookieConfig()");
	}

	@Override
	public String getVirtualServerName() {
		throw notSupported("getVirtualServerName()");
	}

	@Override
	public boolean setInitParameter(final String name, final String value) {
		if (!parameters.containsKey(name)) {
			this.parameters.put(name, value);
			return true;
		}
		return false;
	}

	@Override
	public void setSessionTrackingModes(final Set<SessionTrackingMode> arg0) {
		throw notSupported("setSessionTrackingModes(Set<SessionTrackingMode>) ");

	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	/* noop impl for abstract methods added in Servlet 4.0 */
	public ServletRegistration.Dynamic addJspFile(String s, String s1) {
		return null;
	}

	public int getSessionTimeout() {
		return 0;
	}

	public void setSessionTimeout(int i) {

	}

	public String getRequestCharacterEncoding() {
		return null;
	}

	public void setRequestCharacterEncoding(String s) {

	}

	public String getResponseCharacterEncoding() {
		return null;
	}

	public void setResponseCharacterEncoding(String s) {

	}

}