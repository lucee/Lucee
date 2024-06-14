package lucee.loader.servlet.jakarta;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

public class ServletContextJavax implements ServletContext {

	private jakarta.servlet.ServletContext context;

	public ServletContextJavax(jakarta.servlet.ServletContext context) {
		this.context = context;
	}

	@Override
	public String getContextPath() {
		return context.getContextPath();
	}

	@Override
	public ServletContext getContext(String uripath) {
		return new ServletContextJavax(context.getContext(uripath));
	}

	@Override
	public int getMajorVersion() {
		return context.getMajorVersion();
	}

	@Override
	public int getMinorVersion() {
		return context.getMinorVersion();
	}

	@Override
	public int getEffectiveMajorVersion() {
		return context.getEffectiveMajorVersion();
	}

	@Override
	public int getEffectiveMinorVersion() {
		return context.getEffectiveMinorVersion();
	}

	@Override
	public String getMimeType(String file) {
		return context.getMimeType(file);
	}

	@Override
	public Set<String> getResourcePaths(String path) {
		return context.getResourcePaths(path);
	}

	@Override
	public URL getResource(String path) throws MalformedURLException {
		return context.getResource(path);
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		return context.getResourceAsStream(path);
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return new RequestDispatcherJavax(context.getRequestDispatcher(path));
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String name) {
		return new RequestDispatcherJavax(context.getNamedDispatcher(name));
	}

	@Override
	public Servlet getServlet(String name) throws ServletException {
		throw new RuntimeException("the method [getServlets] is not supported");
	}

	@Override
	public Enumeration<Servlet> getServlets() {
		throw new RuntimeException("the method [getServlets] is not supported");
	}

	@Override
	public Enumeration<String> getServletNames() {
		throw new RuntimeException("the method [getServletNames] is not supported");
	}

	@Override
	public void log(String msg) {
		context.log(msg);
	}

	@Override
	public void log(Exception exception, String msg) {
		context.log(msg, exception);
	}

	@Override
	public void log(String message, Throwable throwable) {
		context.log(message, throwable);
	}

	@Override
	public String getRealPath(String path) {
		return context.getRealPath(path);
	}

	@Override
	public String getServerInfo() {
		return context.getServerInfo();
	}

	@Override
	public String getInitParameter(String name) {
		return context.getInitParameter(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return context.getInitParameterNames();
	}

	@Override
	public boolean setInitParameter(String name, String value) {
		return context.setInitParameter(name, value);
	}

	@Override
	public Object getAttribute(String name) {
		return context.getAttribute(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return context.getAttributeNames();
	}

	@Override
	public void setAttribute(String name, Object object) {
		context.setAttribute(name, object);
	}

	@Override
	public void removeAttribute(String name) {
		context.removeAttribute(name);
	}

	@Override
	public String getServletContextName() {
		return context.getServletContextName();
	}

	@Override
	public Dynamic addServlet(String servletName, String className) {
		throw new RuntimeException("the method is not supported");
	}

	@Override
	public Dynamic addServlet(String servletName, Servlet servlet) {
		throw new RuntimeException("the method is not supported");
	}

	@Override
	public Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
		throw new RuntimeException("the method is not supported");
	}

	@Override
	public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
		throw new RuntimeException("the method is not supported");
	}

	@Override
	public ServletRegistration getServletRegistration(String servletName) {
		throw new RuntimeException("the method is not supported");
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		throw new RuntimeException("the method is not supported");
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className) {
		throw new RuntimeException("the method is not supported");
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, javax.servlet.Filter filter) {
		throw new RuntimeException("the method is not supported");
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Class<? extends javax.servlet.Filter> filterClass) {
		throw new RuntimeException("the method is not supported");
	}

	@Override
	public <T extends javax.servlet.Filter> T createFilter(Class<T> clazz) throws ServletException {
		throw new RuntimeException("the method is not supported");
	}

	@Override
	public javax.servlet.FilterRegistration getFilterRegistration(String filterName) {
		throw new RuntimeException("the method is not supported");
	}

	@Override
	public Map<String, ? extends javax.servlet.FilterRegistration> getFilterRegistrations() {
		throw new RuntimeException("the method is not supported");
	}

	@Override
	public javax.servlet.SessionCookieConfig getSessionCookieConfig() {
		throw new RuntimeException("the method is not supported");
	}

	@Override
	public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
		throw new RuntimeException("the method is not supported");
	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		throw new RuntimeException("the method is not supported");
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		throw new RuntimeException("the method is not supported");
	}

	@Override
	public void addListener(String className) {
		context.addListener(className);
	}

	@Override
	public <T extends EventListener> void addListener(T t) {
		context.addListener(t);
	}

	@Override
	public void addListener(Class<? extends EventListener> listenerClass) {
		context.addListener(listenerClass);
	}

	@Override
	public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
		throw new RuntimeException("the method is not supported");
	}

	@Override
	public void declareRoles(String... roleNames) {
		context.declareRoles(roleNames);
	}

	@Override
	public ClassLoader getClassLoader() {
		return context.getClassLoader();
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		throw new RuntimeException("the method is not supported");
	}

	@Override
	public void setRequestCharacterEncoding(String encoding) {
		context.setRequestCharacterEncoding(encoding);
	}

	@Override
	public void setResponseCharacterEncoding(String encoding) {
		context.setResponseCharacterEncoding(encoding);
	}

	@Override
	public String getRequestCharacterEncoding() {
		return context.getRequestCharacterEncoding();
	}

	@Override
	public String getResponseCharacterEncoding() {
		return context.getResponseCharacterEncoding();
	}

	public jakarta.servlet.ServletContext getJakartaContext() {
		return context;
	}

	@Override
	public Dynamic addJspFile(String servletName, String jspFile) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVirtualServerName() {
		return context.getVirtualServerName();
	}

	@Override
	public int getSessionTimeout() {
		return context.getSessionTimeout();
	}

	@Override
	public void setSessionTimeout(int sessionTimeout) {
		context.setSessionTimeout(sessionTimeout);
	}
}