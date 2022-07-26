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
 * The CFML Engine
 */
public interface CFMLEngine {

	public static final int DIALECT_LUCEE = 0;
	public static final int DIALECT_CFML = 1;
	public static final int DIALECT_BOTH = 3;

	/**
	 * Field <code>CAN_UPDATE</code>
	 */
	public static int CAN_UPDATE = 0;

	/**
	 * Field <code>CAN_RESTART</code>
	 */
	public static int CAN_RESTART = 1;
	public static int CAN_RESTART_ALL = CAN_RESTART;
	public static int CAN_RESTART_CONTEXT = 2;

	public abstract CFMLFactory getCFMLFactory(ServletConfig srvConfig, HttpServletRequest req) throws ServletException;

	public abstract void addServletConfig(ServletConfig config) throws ServletException;

	public void service(HttpServlet servlet, HttpServletRequest req, HttpServletResponse rsp) throws IOException, ServletException;

	public void serviceCFML(HttpServlet servlet, HttpServletRequest req, HttpServletResponse rsp) throws IOException, ServletException;

	public void serviceAMF(HttpServlet servlet, HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException;

	public void serviceFile(HttpServlet servlet, HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException;

	public abstract void serviceRest(HttpServlet servlet, HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException;

	public Info getInfo();

	/**
	 * @return returns the version of the engine in the format [x.x.x.xxx]
	 * @deprecated use instead getInfo()
	 */
	@Deprecated
	public String getVersion();

	/**
	 * @return returns how this engine will be updated (auto, manual)
	 */
	public String getUpdateType();

	/**
	 * @return return location URL to get updates for the engines
	 */
	public URL getUpdateLocation();

	public Identification getIdentification();

	/**
	 * checks if process has the right to do was given with type, the engine with given password
	 * 
	 * @param type restart type (CFMLEngine.CAN_UPDATE, CFMLEngine.CAN_RESTART)
	 * @param password password for the env
	 * @return has right
	 */
	public boolean can(int type, Password password);

	/**
	 * @return returns the engine that has produced this engine
	 */
	public CFMLEngineFactory getCFMLEngineFactory();

	/**
	 * reset the engine
	 */
	public void reset();

	/**
	 * reset a specific config
	 * 
	 * @param configId id of the config to reset
	 */
	public void reset(String configId);

	/**
	 * return the cast util
	 * 
	 * @return operaton util
	 */
	public Cast getCastUtil();

	/**
	 * return the operation util
	 * 
	 * @return operaton util
	 */
	public Operation getOperatonUtil();

	/**
	 * returns the decision util
	 * 
	 * @return decision util
	 */
	public Decision getDecisionUtil();

	/**
	 * returns the decision util
	 * 
	 * @return decision util
	 */
	public Excepton getExceptionUtil();

	/**
	 * returns the decision util
	 * 
	 * @return decision util
	 */
	public Creation getCreationUtil();

	public Object getJavaProxyUtil();// FUTURE return JavaProxyUtil

	/**
	 * returns the IO util
	 * 
	 * @return decision util
	 */
	public IO getIOUtil();

	/**
	 * returns the IO util
	 * 
	 * @return decision util
	 */
	public Strings getStringUtil();

	public ClassUtil getClassUtil();

	/**
	 * returns the FusionDebug Engine
	 * 
	 * @return IFDController
	 */
	public Object getFDController();

	/*
	 * removed to avoid library conflicts, the blazeDS implementation is no longer under development an
	 * in a separate jar
	 */
	// public Object getBlazeDSUtil();

	/**
	 * returns the Resource Util
	 * 
	 * @return Blaze DS Util
	 */
	public ResourceUtil getResourceUtil();

	/**
	 * returns the HTTP Util
	 * 
	 * @return the HTTP Util
	 */
	public HTTPUtil getHTTPUtil();

	// public XMLUtil getXMLUtil();

	public ListUtil getListUtil();

	public HTMLUtil getHTMLUtil();

	public DBUtil getDBUtil();

	public Instrumentation getInstrumentation();

	public abstract ORMUtil getORMUtil();

	/**
	 * @return return existing PageContext for the current PageContext
	 */
	public PageContext getThreadPageContext();

	public Config getThreadConfig();

	public TimeZone getThreadTimeZone();

	/**
	 * create and register a PageContext, use releasePageContext when done
	 * 
	 * @param contextRoot context root
	 * @param host host name
	 * @param scriptName script name
	 * @param queryString query string
	 * @param cookies cookies
	 * @param headers header elements
	 * @param parameters parameters
	 * @param attributes attributes
	 * @param os output stream to write response body
	 * @param timeout timeout for the thread
	 * @param register register to thread or not
	 * @return PageContext Object created
	 * @throws ServletException in case the PC cannot be created
	 */
	public PageContext createPageContext(File contextRoot, String host, String scriptName, String queryString, Cookie[] cookies, Map<String, Object> headers,
			Map<String, String> parameters, Map<String, Object> attributes, OutputStream os, long timeout, boolean register) throws ServletException;

	public void releasePageContext(PageContext pc, boolean unregister);

	public ConfigWeb createConfig(File contextRoot, String host, String scriptName) throws ServletException;

	public VideoUtil getVideoUtil();

	public ZipUtil getZipUtil();

	public abstract void cli(Map<String, String> config, ServletConfig servletConfig) throws IOException, JspException, ServletException;

	public abstract void registerThreadPageContext(PageContext pc);

	public ConfigServer getConfigServer(Password password) throws PageException;

	public ConfigServer getConfigServer(String key, long timeNonce) throws PageException;

	public long uptime();

	public ServletConfig[] getServletConfigs();

	/*
	 * get the OSGi Bundle of the core
	 * 
	 * @return / public abstract Bundle getCoreBundle();
	 */

	public BundleCollection getBundleCollection();

	public BundleContext getBundleContext();

	public ScriptEngineFactory getScriptEngineFactory(int dialect);

	public ScriptEngineFactory getTagEngineFactory(int dialect);

	public abstract TemplateUtil getTemplateUtil();

	public abstract SystemUtil getSystemUtil();

}