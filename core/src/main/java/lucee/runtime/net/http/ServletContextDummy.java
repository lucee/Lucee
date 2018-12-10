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
package lucee.runtime.net.http;

import java.io.File;
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

import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.it.ItAsEnum;
import lucee.runtime.util.EnumerationWrapper;

public class ServletContextDummy implements ServletContext {
    private Struct attributes;
    private Struct parameters;
    private int majorVersion;
    private int minorVersion;
    private Config config;
    private Log log;
    private Resource root;

    public ServletContextDummy(Config config, Resource root, Struct attributes, Struct parameters, int majorVersion, int minorVersion) {
	this.config = config;
	this.root = root;
	this.attributes = attributes;
	this.parameters = parameters;
	this.majorVersion = majorVersion;
	this.minorVersion = minorVersion;
	log = ((ConfigImpl) config).getLogEngine().getConsoleLog(false, "servlet-context-dummy", Log.LEVEL_INFO);

    }

    @Override
    public Object getAttribute(String key) {
	return attributes.get(key, null);
    }

    @Override
    public Enumeration getAttributeNames() {
	return ItAsEnum.toStringEnumeration(attributes.keyIterator());
    }

    @Override
    public String getInitParameter(String key) {
	return Caster.toString(parameters.get(key, null), null);
    }

    @Override
    public Enumeration getInitParameterNames() {
	return new EnumerationWrapper(parameters.keyIterator());
    }

    @Override
    public int getMajorVersion() {
	return majorVersion;
    }

    @Override
    public int getMinorVersion() {
	return minorVersion;
    }

    @Override
    public String getMimeType(String file) {
	return ResourceUtil.getMimeType(config.getResource(file), null);
    }

    @Override
    public String getRealPath(String realpath) {
	return root.getRealResource(realpath).getAbsolutePath();
    }

    @Override
    public URL getResource(String realpath) throws MalformedURLException {
	Resource res = getRealResource(realpath);
	if (res instanceof File) return ((File) res).toURL();
	return new URL(res.getAbsolutePath());
    }

    @Override
    public InputStream getResourceAsStream(String realpath) {
	try {
	    return getRealResource(realpath).getInputStream();
	}
	catch (IOException e) {
	    return null;
	}
    }

    public Resource getRealResource(String realpath) {
	return root.getRealResource(realpath);
    }

    @Override
    public Set getResourcePaths(String realpath) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public ServletContext getContext(String key) {
	// TODO ?
	return this;
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void log(String msg, Throwable t) {
	if (t == null) log.log(Log.LEVEL_INFO, null, msg);
	else log.log(Log.LEVEL_ERROR, null, msg, t);
    }

    @Override
    public void log(Exception e, String msg) {
	log(msg, e);
    }

    @Override
    public void log(String msg) {
	log(msg, null);
    }

    @Override
    public void removeAttribute(String key) {
	attributes.removeEL(KeyImpl.init(key));
    }

    @Override
    public void setAttribute(String key, Object value) {
	attributes.setEL(KeyImpl.init(key), value);
    }

    public String getServletContextName() {
	throw new RuntimeException("not supported");
    }

    public String getServerInfo() {
	throw new RuntimeException("not supported");
    }

    public Servlet getServlet(String arg0) throws ServletException {
	throw new RuntimeException("not supported");
    }

    public Enumeration getServletNames() {
	// deprecated
	return null;
    }

    public Enumeration getServlets() {
	throw new RuntimeException("not supported");
    }

    public Dynamic addFilter(String arg0, String arg1) {
	throw new RuntimeException("not supported");
    }

    public Dynamic addFilter(String arg0, Filter arg1) {
	// TODO Auto-generated method stub
	return null;
    }

    public Dynamic addFilter(String arg0, Class<? extends Filter> arg1) {
	throw new RuntimeException("not supported");
    }

    public void addListener(String arg0) {
	throw new RuntimeException("not supported");
    }

    public <T extends EventListener> void addListener(T arg0) {
	throw new RuntimeException("not supported");
    }

    public void addListener(Class<? extends EventListener> arg0) {
	throw new RuntimeException("not supported");
    }

    public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0, String arg1) {
	throw new RuntimeException("not supported");
    }

    public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0, Servlet arg1) {
	throw new RuntimeException("not supported");
    }

    public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0, Class<? extends Servlet> arg1) {
	throw new RuntimeException("not supported");
    }

    public <T extends Filter> T createFilter(Class<T> arg0) throws ServletException {
	throw new RuntimeException("not supported");
    }

    public <T extends EventListener> T createListener(Class<T> arg0) throws ServletException {
	throw new RuntimeException("not supported");
    }

    public <T extends Servlet> T createServlet(Class<T> arg0) throws ServletException {
	throw new RuntimeException("not supported");
    }

    public void declareRoles(String... arg0) {
	throw new RuntimeException("not supported");
    }

    public ClassLoader getClassLoader() {
	throw new RuntimeException("not supported");
    }

    public String getContextPath() {
	throw new RuntimeException("not supported");
    }

    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
	throw new RuntimeException("not supported");
    }

    public int getEffectiveMajorVersion() {
	return getMajorVersion();
    }

    public int getEffectiveMinorVersion() {
	return getMinorVersion();
    }

    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
	throw new RuntimeException("not supported");
    }

    public FilterRegistration getFilterRegistration(String arg0) {
	throw new RuntimeException("not supported");
    }

    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
	throw new RuntimeException("not supported");
    }

    public JspConfigDescriptor getJspConfigDescriptor() {
	throw new RuntimeException("not supported");
    }

    public ServletRegistration getServletRegistration(String arg0) {
	throw new RuntimeException("not supported");
    }

    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
	throw new RuntimeException("not supported");
    }

    public SessionCookieConfig getSessionCookieConfig() {
	throw new RuntimeException("not supported");
    }

    public String getVirtualServerName() {
	throw new RuntimeException("not supported");
    }

    public boolean setInitParameter(String arg0, String arg1) {
	throw new RuntimeException("not supported");
    }

    public void setSessionTrackingModes(Set<SessionTrackingMode> arg0) {
	throw new RuntimeException("not supported");
    }

}