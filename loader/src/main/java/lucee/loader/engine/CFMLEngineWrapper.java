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
package lucee.loader.engine;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.Map;
import java.util.TimeZone;

import javax.script.ScriptEngineFactory;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;

import org.osgi.framework.BundleContext;

import lucee.Info;
import lucee.loader.osgi.BundleCollection;
import lucee.runtime.CFMLFactory;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigServer;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.Identification;
import lucee.runtime.config.Password;
import lucee.runtime.exp.PageException;
import lucee.runtime.util.Cast;
import lucee.runtime.util.ClassUtil;
import lucee.runtime.util.Creation;
import lucee.runtime.util.DBUtil;
import lucee.runtime.util.Decision;
import lucee.runtime.util.Excepton;
import lucee.runtime.util.HTMLUtil;
import lucee.runtime.util.HTTPUtil;
import lucee.runtime.util.IO;
import lucee.runtime.util.ListUtil;
import lucee.runtime.util.ORMUtil;
import lucee.runtime.util.Operation;
import lucee.runtime.util.ResourceUtil;
import lucee.runtime.util.Strings;
import lucee.runtime.util.SystemUtil;
import lucee.runtime.util.TemplateUtil;
import lucee.runtime.util.ZipUtil;
import lucee.runtime.video.VideoUtil;

/**
 * wrapper for a CFMlEngine
 */
public class CFMLEngineWrapper implements CFMLEngine {

	private CFMLEngine engine;

	/**
	 * constructor of the class
	 * 
	 * @param engine engine to wrap
	 */
	public CFMLEngineWrapper(final CFMLEngine engine) {
		this.engine = engine;
	}

	@Override
	public void addServletConfig(final ServletConfig config) throws ServletException {
		engine.addServletConfig(config);
	}

	@Override
	public void service(final HttpServlet servlet, final HttpServletRequest req, final HttpServletResponse rsp) throws ServletException, IOException {
		engine.service(servlet, req, rsp);
	}

	@Override
	public void serviceCFML(final HttpServlet servlet, final HttpServletRequest req, final HttpServletResponse rsp) throws ServletException, IOException {
		engine.serviceCFML(servlet, req, rsp);
	}

	@Override
	public void serviceAMF(final HttpServlet servlet, final HttpServletRequest req, final HttpServletResponse rsp) throws ServletException, IOException {
		engine.serviceAMF(servlet, req, rsp);
	}

	@Override
	public void serviceFile(final HttpServlet servlet, final HttpServletRequest req, final HttpServletResponse rsp) throws ServletException, IOException {
		engine.serviceFile(servlet, req, rsp);
	}

	@Override
	public void serviceRest(final HttpServlet servlet, final HttpServletRequest req, final HttpServletResponse rsp) throws ServletException, IOException {
		engine.serviceRest(servlet, req, rsp);
	}

	@Override
	public String getVersion() {
		return engine.getInfo().getVersion().toString();
	}

	@Override
	public String getUpdateType() {
		return engine.getUpdateType();
	}

	@Override
	public URL getUpdateLocation() {
		return engine.getUpdateLocation();
	}

	@Override
	public Identification getIdentification() {
		return engine.getIdentification();
	}

	@Override
	public boolean can(final int type, final Password password) {
		return engine.can(type, password);
	}

	@Override
	public CFMLEngineFactory getCFMLEngineFactory() {
		return engine.getCFMLEngineFactory();
	}

	@Override
	public void reset() {
		engine.reset();
	}

	@Override
	public void reset(final String configId) {
		engine.reset(configId);
	}

	public void setEngine(final CFMLEngine engine) {
		this.engine = engine;
	}

	public CFMLEngine getEngine() {
		return this.engine;
	}

	public boolean isIdentical(final CFMLEngine engine) {
		return this.engine == engine;
	}

	@Override
	public Cast getCastUtil() {
		return engine.getCastUtil();
	}

	@Override
	public Operation getOperatonUtil() {
		return engine.getOperatonUtil();
	}

	@Override
	public Decision getDecisionUtil() {
		return engine.getDecisionUtil();
	}

	@Override
	public Excepton getExceptionUtil() {
		return engine.getExceptionUtil();
	}

	@Override
	public Creation getCreationUtil() {
		return engine.getCreationUtil();
	}

	@Override
	public Object getJavaProxyUtil() {// FUTURE return JavaProxyUtil
		return engine.getJavaProxyUtil();
	}

	@Override
	public IO getIOUtil() {
		return engine.getIOUtil();
	}

	@Override
	public CFMLFactory getCFMLFactory(final ServletConfig srvConfig, final HttpServletRequest req) throws ServletException {
		return engine.getCFMLFactory(srvConfig, req);
	}

	@Override
	public Object getFDController() {
		return engine.getFDController();
	}

	@Override
	public HTTPUtil getHTTPUtil() {
		return engine.getHTTPUtil();
	}

	@Override
	public ResourceUtil getResourceUtil() {
		return engine.getResourceUtil();
	}

	@Override
	public PageContext getThreadPageContext() {
		return engine.getThreadPageContext();
	}

	@Override
	public Config getThreadConfig() {
		return engine.getThreadConfig();
	}

	@Override
	public VideoUtil getVideoUtil() {
		return engine.getVideoUtil();
	}

	@Override
	public ZipUtil getZipUtil() {
		return engine.getZipUtil();
	}

	@Override
	public Strings getStringUtil() {
		return engine.getStringUtil();
	}

	/*
	 * public String getState() { return engine.getInfo().getStateAsString(); }
	 */

	/**
	 * this interface is new to this class and not officially part of Lucee 3.x, do not use outside the
	 * loader
	 * 
	 * @param other engine to compare
	 * @param checkReferenceEqualityOnly check reference equality only
	 * @return is equal to given engine
	 */
	public boolean equalTo(CFMLEngine other, final boolean checkReferenceEqualityOnly) {
		while (other instanceof CFMLEngineWrapper)
			other = ((CFMLEngineWrapper) other).engine;
		if (checkReferenceEqualityOnly) return engine == other;
		return engine.equals(other);
	}

	@Override
	public void cli(final Map<String, String> config, final ServletConfig servletConfig) throws IOException, JspException, ServletException {
		engine.cli(config, servletConfig);
	}

	@Override
	public void registerThreadPageContext(final PageContext pc) {
		engine.registerThreadPageContext(pc);
	}

	@Override
	public ConfigServer getConfigServer(final Password password) throws PageException {
		return engine.getConfigServer(password);
	}

	@Override
	public ConfigServer getConfigServer(final String key, final long timeNonce) throws PageException {
		return engine.getConfigServer(key, timeNonce);
	}

	@Override
	public long uptime() {
		return engine.uptime();
	}

	@Override
	public Info getInfo() {
		return engine.getInfo();
	}

	@Override
	public BundleContext getBundleContext() {
		return engine.getBundleContext();
	}

	@Override
	public ClassUtil getClassUtil() {
		return engine.getClassUtil();
	}

	/*
	 * @Override public XMLUtil getXMLUtil() { return engine.getXMLUtil(); }
	 */

	@Override
	public ScriptEngineFactory getScriptEngineFactory(final int dialect) {
		return engine.getScriptEngineFactory(dialect);
	}

	@Override
	public ScriptEngineFactory getTagEngineFactory(final int dialect) {
		return engine.getTagEngineFactory(dialect);
	}

	@Override
	public ServletConfig[] getServletConfigs() {
		return engine.getServletConfigs();
	}

	@Override
	public ListUtil getListUtil() {
		return engine.getListUtil();
	}

	@Override
	public DBUtil getDBUtil() {
		return engine.getDBUtil();
	}

	@Override
	public ORMUtil getORMUtil() {
		return engine.getORMUtil();
	}

	@Override
	public TemplateUtil getTemplateUtil() {
		return engine.getTemplateUtil();
	}

	@Override
	public PageContext createPageContext(final File contextRoot, final String host, final String scriptName, final String queryString, final Cookie[] cookies,
			final Map<String, Object> headers, final Map<String, String> parameters, final Map<String, Object> attributes, final OutputStream os, final long timeout,
			final boolean register) throws ServletException {
		return engine.createPageContext(contextRoot, host, scriptName, queryString, cookies, headers, parameters, attributes, os, timeout, register);
	}

	@Override
	public void releasePageContext(final PageContext pc, final boolean unregister) {
		engine.releasePageContext(pc, unregister);
	}

	@Override
	public ConfigWeb createConfig(final File contextRoot, final String host, final String scriptName) throws ServletException {
		return engine.createConfig(contextRoot, host, scriptName);
	}

	@Override
	public BundleCollection getBundleCollection() {
		return engine.getBundleCollection();
	}

	@Override
	public HTMLUtil getHTMLUtil() {
		return engine.getHTMLUtil();
	}

	@Override
	public TimeZone getThreadTimeZone() {
		return engine.getThreadTimeZone();
	}

	@Override
	public SystemUtil getSystemUtil() {
		return engine.getSystemUtil();
	}

	@Override
	public Instrumentation getInstrumentation() {
		return engine.getInstrumentation();
	}
}