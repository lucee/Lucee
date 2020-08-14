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
package lucee.runtime.config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.allaire.cfx.CustomTag;

import lucee.commons.digest.MD5;
import lucee.commons.io.FileUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.cache.Cache;
import lucee.commons.io.compress.Pack200Util;
import lucee.commons.io.compress.ZipUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.io.res.filter.ResourceNameFilter;
import lucee.commons.io.res.util.FileWrapper;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.HTTPUtil;
import lucee.commons.net.IPRange;
import lucee.commons.net.URLEncoder;
import lucee.commons.net.http.HTTPEngine;
import lucee.commons.net.http.HTTPResponse;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.osgi.BundleCollection;
import lucee.loader.util.ExtensionFilter;
import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.cfx.CFXTagException;
import lucee.runtime.cfx.CFXTagPool;
import lucee.runtime.config.ConfigImpl.Startup;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.WDDXConverter;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.ParamSyntax;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.HTTPException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.SecurityException;
import lucee.runtime.exp.XMLException;
import lucee.runtime.extension.Extension;
import lucee.runtime.extension.ExtensionDefintion;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.functions.other.CreateObject;
import lucee.runtime.functions.other.CreateUUID;
import lucee.runtime.functions.other.URLEncodedFormat;
import lucee.runtime.functions.string.Hash;
import lucee.runtime.functions.system.IsZipFile;
import lucee.runtime.gateway.GatewayEngineImpl;
import lucee.runtime.gateway.GatewayEntry;
import lucee.runtime.gateway.GatewayEntryImpl;
import lucee.runtime.listener.AppListenerUtil;
import lucee.runtime.monitor.Monitor;
import lucee.runtime.net.amf.AMFEngine;
import lucee.runtime.net.ntp.NtpClient;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.orm.ORMConfiguration;
import lucee.runtime.orm.ORMConfigurationImpl;
import lucee.runtime.orm.ORMEngine;
import lucee.runtime.osgi.BundleBuilderFactory;
import lucee.runtime.osgi.BundleFile;
import lucee.runtime.osgi.BundleInfo;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.osgi.OSGiUtil.BundleDefinition;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.regex.RegexFactory;
import lucee.runtime.search.SearchEngine;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.security.SecurityManagerImpl;
import lucee.runtime.security.SerialNumber;
import lucee.runtime.text.xml.XMLCaster;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.scope.Cluster;
import lucee.runtime.type.scope.ClusterNotSupported;
import lucee.runtime.type.scope.ClusterRemote;
import lucee.runtime.type.scope.ScopeContext;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ComponentUtil;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.video.VideoExecuter;
import lucee.runtime.video.VideoExecuterNotSupported;
import lucee.transformer.library.ClassDefinitionImpl;
import lucee.transformer.library.function.FunctionLibException;
import lucee.transformer.library.tag.TagLibException;

/**
 * 
 */
public final class XMLConfigAdmin {

	private static final BundleInfo[] EMPTY = new BundleInfo[0];
	private ConfigImpl config;
	private Document doc;
	private Password password;

	/**
	 * 
	 * @param config
	 * @param password
	 * @return returns a new instance of the class
	 * @throws SAXException
	 * @throws IOException
	 */
	public static XMLConfigAdmin newInstance(ConfigImpl config, Password password) throws XMLException, IOException {
		return new XMLConfigAdmin(config, password);
	}

	private void checkWriteAccess() throws SecurityException {
		ConfigWebUtil.checkGeneralWriteAccess(config, password);
	}

	private void checkReadAccess() throws SecurityException {
		ConfigWebUtil.checkGeneralReadAccess(config, password);
	}

	/**
	 * @param password
	 * @throws IOException
	 * @throws DOMException
	 * @throws ExpressionException
	 */
	public void setPassword(Password password) throws SecurityException, DOMException, IOException {
		checkWriteAccess();
		PasswordImpl.writeToXML(doc.getDocumentElement(), password, false);
	}

	/*
	 * public void setVersion(double version) { setVersion(doc,version);
	 * 
	 * }
	 */

	public static void setVersion(Document doc, Version version) {
		Element root = doc.getDocumentElement();
		root.setAttribute("version", version.getMajor() + "." + version.getMinor());

	}
	/*
	 * public void setId(String id) {
	 * 
	 * Element root=doc.getDocumentElement(); if(!StringUtil.isEmpty(root.getAttribute("id"))) return;
	 * root.setAttribute("id",id); try { store(config); } catch (Exception e) {} }
	 */

	/**
	 * @param contextPath
	 * @param password
	 * @throws FunctionLibException
	 * @throws TagLibException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws SAXException
	 * @throws PageException
	 * @throws BundleException
	 */
	public void removePassword(String contextPath) throws PageException, SAXException, ClassException, IOException, TagLibException, FunctionLibException, BundleException {
		checkWriteAccess();
		if (contextPath == null || contextPath.length() == 0 || !(config instanceof ConfigServerImpl)) {
			// config.setPassword(password); do nothing!
		}
		else {
			ConfigServerImpl cs = (ConfigServerImpl) config;
			ConfigWebImpl cw = cs.getConfigWebImpl(contextPath);
			if (cw != null) cw.updatePassword(false, cw.getPassword(), null);
		}
	}

	private XMLConfigAdmin(ConfigImpl config, Password password) throws IOException, XMLException {
		this.config = config;
		this.password = password;
		doc = XMLUtil.createDocument(config.getConfigFile(), false);
	}

	public static void checkForChangesInConfigFile(Config config) {
		ConfigImpl ci = (ConfigImpl) config;
		if (!ci.checkForChangesInConfigFile()) return;

		Resource file = config.getConfigFile();
		long diff = file.lastModified() - ci.lastModified();
		if (diff < 10 && diff > -10) return;
		// reload
		try {
			XMLConfigAdmin admin = XMLConfigAdmin.newInstance(ci, null);
			admin._reload();
			LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_INFO, XMLConfigAdmin.class.getName(), "reloaded the configuration [" + file + "] automatically");
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	private void addResourceProvider(String scheme, ClassDefinition cd, String arguments) throws PageException {
		checkWriteAccess();

		Element resources = _getRootElement("resources");
		Element[] rpElements = XMLConfigWebFactory.getChildren(resources, "resource-provider");
		String s;
		// update
		if (rpElements != null) {
			for (int i = 0; i < rpElements.length; i++) {
				s = rpElements[i].getAttribute("scheme");
				if (!StringUtil.isEmpty(s) && s.equalsIgnoreCase(scheme)) {
					setClass(rpElements[i], null, "", cd);
					rpElements[i].setAttribute("scheme", scheme);
					rpElements[i].setAttribute("arguments", arguments);
					return;
				}
			}
		}
		// Insert
		Element el = doc.createElement("resource-provider");
		resources.appendChild(XMLCaster.toRawNode(el));
		setClass(el, null, "", cd);
		el.setAttribute("scheme", scheme);
		el.setAttribute("arguments", arguments);
	}

	public static synchronized void _storeAndReload(ConfigImpl config)
			throws PageException, SAXException, ClassException, IOException, TagLibException, FunctionLibException, BundleException {
		XMLConfigAdmin admin = new XMLConfigAdmin(config, null);
		admin._store();
		admin._reload();
	}

	private synchronized void _storeAndReload() throws PageException, SAXException, ClassException, IOException, TagLibException, FunctionLibException, BundleException {
		_store();
		_reload();
	}

	public synchronized void storeAndReload() throws PageException, SAXException, ClassException, IOException, TagLibException, FunctionLibException, BundleException {
		checkWriteAccess();
		_store();
		_reload();
	}

	private synchronized void _store() throws PageException {
		XMLCaster.writeTo(doc, config.getConfigFile());
	}

	private synchronized void _reload() throws PageException, SAXException, ClassException, IOException, TagLibException, FunctionLibException, BundleException {

		// if(storeInMemoryData)XMLCaster.writeTo(doc,config.getConfigFile());
		CFMLEngine engine = ConfigWebUtil.getEngine(config);
		if (config instanceof ConfigServerImpl) {

			ConfigServerImpl cs = (ConfigServerImpl) config;
			XMLConfigServerFactory.reloadInstance(engine, cs);
			ConfigWeb[] webs = cs.getConfigWebs();
			for (int i = 0; i < webs.length; i++) {
				XMLConfigWebFactory.reloadInstance(engine, (ConfigServerImpl) config, (ConfigWebImpl) webs[i], true);
			}
		}
		else {
			ConfigServerImpl cs = ((ConfigWebImpl) config).getConfigServerImpl();
			XMLConfigWebFactory.reloadInstance(engine, cs, (ConfigWebImpl) config, false);
		}
	}

	/*
	 * private void createAbort() { try {
	 * ConfigWebFactory.getChildByName(doc.getDocumentElement(),"cfabort",true); } catch(Throwable t)
	 * {ExceptionUtil.rethrowIfNecessary(t);} }
	 */

	public void setTaskMaxThreads(Integer maxThreads) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update task settings");
		Element mail = _getRootElement("remote-clients");
		mail.setAttribute("max-threads", Caster.toString(maxThreads, ""));
	}

	/**
	 * sets Mail Logger to Config
	 * 
	 * @param logFile
	 * @param level
	 * @throws PageException
	 */
	public void setMailLog(Config config, String logFile, String level) throws PageException {
		ConfigImpl ci = (ConfigImpl) config;
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_MAIL);

		if (!hasAccess) throw new SecurityException("no access to update mail server settings");
		ConfigWebUtil.getFile(config, config.getRootDirectory(), logFile, FileUtil.TYPE_FILE);

		Element logging = XMLConfigWebFactory.getChildByName(doc.getDocumentElement(), "logging");
		Element[] children = XMLUtil.getChildElementsAsArray(logging);
		Element logger = null;

		for (int i = 0; i < children.length; i++) {
			if (children[i].getTagName().equals("logger") && "mail".equalsIgnoreCase(children[i].getAttribute("name"))) {
				logger = children[i];
				break;
			}
		}
		if (logger == null) {
			logger = doc.createElement("logger");
			logging.appendChild(logger);
		}
		logger.setAttribute("name", "mail");
		if ("console".equalsIgnoreCase(logFile)) {
			setClass(logger, null, "appender-", ci.getLogEngine().appenderClassDefintion("console"));
			setClass(logger, null, "layout-", ci.getLogEngine().layoutClassDefintion("pattern"));
		}
		else {
			setClass(logger, null, "appender-", ci.getLogEngine().appenderClassDefintion("resource"));
			setClass(logger, null, "layout-", ci.getLogEngine().layoutClassDefintion("classic"));
			logger.setAttribute("appender-arguments", "path:" + logFile);
		}
		logger.setAttribute("log-level", level);
	}

	/**
	 * sets if spool is enable or not
	 * 
	 * @param spoolEnable
	 * @throws SecurityException
	 */
	public void setMailSpoolEnable(Boolean spoolEnable) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_MAIL);

		if (!hasAccess) throw new SecurityException("no access to update mail server settings");
		Element mail = _getRootElement("mail");
		mail.setAttribute("spool-enable", Caster.toString(spoolEnable, ""));
		// config.setMailSpoolEnable(spoolEnable);
	}

	/*
	 * * sets if er interval is enable or not
	 * 
	 * @param interval
	 * 
	 * @throws SecurityException / public void setMailSpoolInterval(Integer interval) throws
	 * SecurityException { checkWriteAccess(); boolean
	 * hasAccess=ConfigWebUtil.hasAccess(config,SecurityManager.TYPE_MAIL); if(!hasAccess) throw new
	 * SecurityException("no access to update mail server settings"); Element
	 * mail=_getRootElement("mail"); mail.setAttribute("spool-interval",Caster.toString(interval,""));
	 * //config.setMailSpoolInterval(interval); }
	 */

	/**
	 * sets the timeout for the spooler for one job
	 * 
	 * @param timeout
	 * @throws SecurityException
	 */
	public void setMailTimeout(Integer timeout) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_MAIL);
		if (!hasAccess) throw new SecurityException("no access to update mail server settings");
		Element mail = _getRootElement("mail");
		mail.setAttribute("timeout", Caster.toString(timeout, ""));
		// config.setMailTimeout(timeout);
	}

	/**
	 * sets the charset for the mail
	 * 
	 * @param charset
	 * @throws SecurityException
	 */
	public void setMailDefaultCharset(String charset) throws PageException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_MAIL);
		if (!hasAccess) throw new SecurityException("no access to update mail server settings");

		if (!StringUtil.isEmpty(charset)) {
			try {
				IOUtil.checkEncoding(charset);
			}
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
		}

		Element mail = _getRootElement("mail");
		mail.setAttribute("default-encoding", charset);
		// config.setMailDefaultEncoding(charset);
	}

	/**
	 * insert or update a mailserver on system
	 * 
	 * @param hostName
	 * @param username
	 * @param password
	 * @param port
	 * @param ssl
	 * @param tls
	 * @throws PageException
	 */
	public void updateMailServer(int id, String hostName, String username, String password, int port, boolean tls, boolean ssl, long lifeTimeSpan, long idleTimeSpan,
			boolean reuseConnections) throws PageException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_MAIL);
		if (!hasAccess) throw new SecurityException("no access to update mail server settings");

		Element mail = _getRootElement("mail");
		if (port < 1) port = 21;

		if (hostName == null || hostName.trim().length() == 0) throw new ExpressionException("Host (SMTP) cannot be an empty value");
		hostName = hostName.trim();

		Element[] children = XMLConfigWebFactory.getChildren(mail, "server");

		boolean checkId = id > 0;

		// Update
		Element server = null;
		String _hostName, _username;
		for (int i = 0; i < children.length; i++) {
			Element el = children[i];
			if (checkId) {
				if (i + 1 == id) {
					server = el;
					break;
				}
			}
			else {
				_hostName = StringUtil.emptyIfNull(el.getAttribute("smtp"));
				_username = StringUtil.emptyIfNull(el.getAttribute("username"));
				if (_hostName.equalsIgnoreCase(hostName) && _username.equals(StringUtil.emptyIfNull(username))) {
					server = el;
					break;
				}
			}
		}

		// Insert
		if (server == null) {
			server = doc.createElement("server");
			mail.appendChild(XMLCaster.toRawNode(server));
		}
		server.setAttribute("smtp", hostName);
		server.setAttribute("username", username);
		server.setAttribute("password", ConfigWebUtil.encrypt(password));
		server.setAttribute("port", Caster.toString(port));
		server.setAttribute("tls", Caster.toString(tls));
		server.setAttribute("ssl", Caster.toString(ssl));
		server.setAttribute("life", Caster.toString(lifeTimeSpan));
		server.setAttribute("idle", Caster.toString(idleTimeSpan));
		server.setAttribute("reuse-connection", Caster.toString(reuseConnections));

	}

	/**
	 * removes a mailserver from system
	 * 
	 * @param hostName
	 * @throws SecurityException
	 */
	public void removeMailServer(String hostName, String username) throws SecurityException {
		checkWriteAccess();

		Element mail = _getRootElement("mail");
		Element[] children = XMLConfigWebFactory.getChildren(mail, "server");
		String _hostName, _username;
		if (children.length > 0) {
			for (int i = 0; i < children.length; i++) {
				Element el = children[i];
				_hostName = el.getAttribute("smtp");
				_username = el.getAttribute("username");
				if (StringUtil.emptyIfNull(_hostName).equalsIgnoreCase(StringUtil.emptyIfNull(hostName))
						&& StringUtil.emptyIfNull(_username).equalsIgnoreCase(StringUtil.emptyIfNull(username))) {
					mail.removeChild(children[i]);
				}
			}
		}
	}

	public void removeLogSetting(String name) throws SecurityException {
		checkWriteAccess();
		Element logging = _getRootElement("logging");
		Element[] children = XMLConfigWebFactory.getChildren(logging, "logger");
		if (children.length > 0) {
			String _name;
			for (int i = 0; i < children.length; i++) {
				Element el = children[i];
				_name = el.getAttribute("name");

				if (_name != null && _name.equalsIgnoreCase(name)) {
					logging.removeChild(children[i]);
				}
			}
		}
	}

	static void updateMapping(ConfigImpl config, String virtual, String physical, String archive, String primary, short inspect, boolean toplevel, int listenerMode,
			int listenerType, boolean readonly, boolean reload) throws SAXException, IOException, PageException, BundleException {
		XMLConfigAdmin admin = new XMLConfigAdmin(config, null);
		admin._updateMapping(virtual, physical, archive, primary, inspect, toplevel, listenerMode, listenerType, readonly);
		admin._store();
		if (reload) admin._reload();
	}

	static void updateComponentMapping(ConfigImpl config, String virtual, String physical, String archive, String primary, short inspect, boolean reload)
			throws SAXException, IOException, PageException, BundleException {
		XMLConfigAdmin admin = new XMLConfigAdmin(config, null);
		admin._updateComponentMapping(virtual, physical, archive, primary, inspect);
		admin._store();
		if (reload) admin._reload();
	}

	static void updateCustomTagMapping(ConfigImpl config, String virtual, String physical, String archive, String primary, short inspect, boolean reload)
			throws SAXException, IOException, PageException, BundleException {
		XMLConfigAdmin admin = new XMLConfigAdmin(config, null);
		admin._updateCustomTag(virtual, physical, archive, primary, inspect);
		admin._store();
		if (reload) admin._reload();
	}

	/**
	 * insert or update a mapping on system
	 * 
	 * @param virtual
	 * @param physical
	 * @param archive
	 * @param primary
	 * @param trusted
	 * @param toplevel
	 * @throws ExpressionException
	 * @throws SecurityException
	 */
	public void updateMapping(String virtual, String physical, String archive, String primary, short inspect, boolean toplevel, int listenerMode, int listenerType,
			boolean readOnly) throws ExpressionException, SecurityException {
		checkWriteAccess();
		_updateMapping(virtual, physical, archive, primary, inspect, toplevel, listenerMode, listenerType, readOnly);
	}

	private void _updateMapping(String virtual, String physical, String archive, String primary, short inspect, boolean toplevel, int listenerMode, int listenerType,
			boolean readOnly) throws ExpressionException, SecurityException {

		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_MAPPING);
		virtual = virtual.trim();
		if (physical == null) physical = "";
		else physical = physical.trim();
		if (archive == null) archive = "";
		else archive = archive.trim();
		primary = primary.trim();
		if (!hasAccess) throw new SecurityException("no access to update mappings");

		// check virtual
		if (virtual == null || virtual.length() == 0) throw new ExpressionException("virtual path cannot be an empty value");
		virtual = virtual.replace('\\', '/');

		if (!virtual.equals("/") && virtual.endsWith("/")) virtual = virtual.substring(0, virtual.length() - 1);

		if (virtual.charAt(0) != '/') throw new ExpressionException("virtual path must start with [/]");
		boolean isArchive = primary.equalsIgnoreCase("archive");

		if ((physical.length() + archive.length()) == 0) throw new ExpressionException("physical or archive must have a value");

		if (isArchive && archive.length() == 0) isArchive = false;

		if (!isArchive && archive.length() > 0 && physical.length() == 0) isArchive = true;

		Element mappings = _getRootElement("mappings");

		// do we already have a record for it?
		Element[] children = XMLConfigWebFactory.getChildren(mappings, "mapping");
		Element el = null;
		for (int i = 0; i < children.length; i++) {
			String v = children[i].getAttribute("virtual");
			if (v != null) {
				if (!v.equals("/") && v.endsWith("/")) v = v.substring(0, v.length() - 1);

				if (v.equals(virtual)) {
					el = children[i];
					el.removeAttribute("trusted");
					break;
				}
			}
		}

		// create element if necessary
		boolean update = el != null;
		if (el == null) {
			el = doc.createElement("mapping");
			mappings.appendChild(el);
			el.setAttribute("virtual", virtual);
		}

		// physical
		if (physical.length() > 0) {
			el.setAttribute("physical", physical);
		}
		else if (el.hasAttribute("physical")) {
			el.removeAttribute("physical");
		}

		// archive
		if (archive.length() > 0) {
			el.setAttribute("archive", archive);
		}
		else if (el.hasAttribute("archive")) {
			el.removeAttribute("archive");
		}

		// primary
		el.setAttribute("primary", isArchive ? "archive" : "physical");

		// listener-type
		String type = ConfigWebUtil.toListenerType(listenerType, null);
		if (type != null) {
			el.setAttribute("listener-type", type);
		}
		else if (el.hasAttribute("listener-type")) {
			el.removeAttribute("listener-type");
		}

		// listener-mode
		String mode = ConfigWebUtil.toListenerMode(listenerMode, null);
		if (mode != null) {
			el.setAttribute("listener-mode", mode);
		}
		else if (el.hasAttribute("listener-mode")) {
			el.removeAttribute("listener-mode");
		}

		// others
		el.setAttribute("inspect-template", ConfigWebUtil.inspectTemplate(inspect, ""));
		el.setAttribute("toplevel", Caster.toString(toplevel));
		el.setAttribute("readonly", Caster.toString(readOnly));

		// set / to the end
		if (!update) {
			children = XMLConfigWebFactory.getChildren(mappings, "mapping");
			for (int i = 0; i < children.length; i++) {
				String v = children[i].getAttribute("virtual");

				if (v != null && v.equals("/")) {
					el = children[i];
					mappings.removeChild(el);
					mappings.appendChild(el);
					return;
				}

			}
		}

	}

	public void updateRestMapping(String virtual, String physical, boolean _default) throws ExpressionException, SecurityException {
		checkWriteAccess();
		boolean hasAccess = true;// TODO ConfigWebUtil.hasAccess(config,SecurityManager.TYPE_REST);
		virtual = virtual.trim();
		physical = physical.trim();
		if (!hasAccess) throw new SecurityException("no access to update REST mapping");

		// check virtual
		if (virtual == null || virtual.length() == 0) throw new ExpressionException("virtual path cannot be an empty value");
		virtual = virtual.replace('\\', '/');
		if (virtual.equals("/")) throw new ExpressionException("virtual path cannot be /");

		if (virtual.endsWith("/")) virtual = virtual.substring(0, virtual.length() - 1);

		if (virtual.charAt(0) != '/') virtual = "/" + virtual;

		if ((physical.length()) == 0) throw new ExpressionException("physical path cannot be an empty value");

		Element rest = _getRootElement("rest");
		Element[] children = XMLConfigWebFactory.getChildren(rest, "mapping");

		// remove existing default
		if (_default) {
			for (int i = 0; i < children.length; i++) {
				if (Caster.toBooleanValue(children[i].getAttribute("default"), false)) children[i].setAttribute("default", "false");
			}
		}

		// Update
		String v;
		Element el = null;
		for (int i = 0; i < children.length; i++) {
			v = children[i].getAttribute("virtual");
			if (v != null && v.equals(virtual)) {
				el = children[i];
			}
		}

		// Insert
		if (el == null) {
			el = doc.createElement("mapping");
			rest.appendChild(el);
		}

		el.setAttribute("virtual", virtual);
		el.setAttribute("physical", physical);
		el.setAttribute("default", Caster.toString(_default));

	}

	/**
	 * delete a mapping on system
	 * 
	 * @param virtual
	 * @throws ExpressionException
	 * @throws SecurityException
	 */
	public void removeMapping(String virtual) throws ExpressionException, SecurityException {
		checkWriteAccess();
		_removeMapping(virtual);
	}

	public void _removeMapping(String virtual) throws ExpressionException {
		// check parameters
		if (virtual == null || virtual.length() == 0) throw new ExpressionException("virtual path cannot be an empty value");
		virtual = virtual.replace('\\', '/');

		if (!virtual.equals("/") && virtual.endsWith("/")) virtual = virtual.substring(0, virtual.length() - 1);
		if (virtual.charAt(0) != '/') throw new ExpressionException("virtual path must start with [/]");

		Element mappings = _getRootElement("mappings");

		Element[] children = XMLConfigWebFactory.getChildren(mappings, "mapping");
		for (int i = 0; i < children.length; i++) {
			String v = children[i].getAttribute("virtual");
			if (v != null) {
				if (!v.equals("/") && v.endsWith("/")) v = v.substring(0, v.length() - 1);
				if (v != null && v.equals(virtual)) {
					Element el = children[i];
					mappings.removeChild(el);
				}
			}
		}
	}

	public void removeRestMapping(String virtual) throws ExpressionException, SecurityException {
		checkWriteAccess();
		// check parameters
		if (virtual == null || virtual.length() == 0) throw new ExpressionException("virtual path cannot be an empty value");
		virtual = virtual.replace('\\', '/');
		if (virtual.equals("/")) throw new ExpressionException("virtual path cannot be /");

		if (virtual.endsWith("/")) virtual = virtual.substring(0, virtual.length() - 1);
		if (virtual.charAt(0) != '/') virtual = "/" + virtual;

		Element mappings = _getRootElement("rest");

		Element[] children = XMLConfigWebFactory.getChildren(mappings, "mapping");
		for (int i = 0; i < children.length; i++) {
			String v = children[i].getAttribute("virtual");
			if (v != null) {
				if (!v.equals("/") && v.endsWith("/")) v = v.substring(0, v.length() - 1);
				if (v != null && v.equals(virtual)) {
					Element el = children[i];
					mappings.removeChild(el);
				}
			}
		}
	}

	/**
	 * delete a customtagmapping on system
	 * 
	 * @param virtual
	 * @throws SecurityException
	 */
	public void removeCustomTag(String virtual) throws SecurityException {
		checkWriteAccess();

		Element mappings = _getRootElement("custom-tag");
		Element[] children = XMLConfigWebFactory.getChildren(mappings, "mapping");
		for (int i = 0; i < children.length; i++) {
			if (virtual.equals(createVirtual(children[i]))) mappings.removeChild(children[i]);
		}
	}

	public void removeComponentMapping(String virtual) throws SecurityException {
		checkWriteAccess();

		Element mappings = _getRootElement("component");
		Element[] children = XMLConfigWebFactory.getChildren(mappings, "mapping");
		String v;
		for (int i = 0; i < children.length; i++) {
			v = createVirtual(children[i]);
			if (virtual.equals(v)) mappings.removeChild(children[i]);
		}
	}

	/**
	 * insert or update a mapping for Custom Tag
	 * 
	 * @param virtual
	 * @param physical
	 * @param archive
	 * @param primary
	 * @param trusted
	 * @throws ExpressionException
	 * @throws SecurityException
	 */
	public void updateCustomTag(String virtual, String physical, String archive, String primary, short inspect) throws ExpressionException, SecurityException {
		checkWriteAccess();
		_updateCustomTag(virtual, physical, archive, primary, inspect);
	}

	private void _updateCustomTag(String virtual, String physical, String archive, String primary, short inspect) throws ExpressionException, SecurityException {
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_CUSTOM_TAG);
		if (!hasAccess) throw new SecurityException("no access to change custom tag settings");
		if (physical == null) physical = "";
		if (archive == null) archive = "";

		// virtual="/custom-tag";
		if (StringUtil.isEmpty(virtual)) virtual = createVirtual(physical, archive);

		boolean isArchive = primary.equalsIgnoreCase("archive");
		if (isArchive && archive.length() == 0) {
			throw new ExpressionException("archive must have a value when primary has value archive");
		}
		if (!isArchive && physical.length() == 0) {
			throw new ExpressionException("physical must have a value when primary has value physical");
		}

		Element mappings = _getRootElement("custom-tag");

		// Update
		String v;
		Element[] children = XMLConfigWebFactory.getChildren(mappings, "mapping");
		for (int i = 0; i < children.length; i++) {
			Element el = children[i];
			v = createVirtual(el);
			if (v.equals(virtual)) {
				el.setAttribute("virtual", v);
				el.setAttribute("physical", physical);
				el.setAttribute("archive", archive);
				el.setAttribute("primary", primary.equalsIgnoreCase("archive") ? "archive" : "physical");
				el.setAttribute("inspect-template", ConfigWebUtil.inspectTemplate(inspect, ""));
				el.removeAttribute("trusted");
				return;
			}
		}

		// Insert
		Element el = doc.createElement("mapping");
		mappings.appendChild(el);
		if (physical.length() > 0) el.setAttribute("physical", physical);
		if (archive.length() > 0) el.setAttribute("archive", archive);
		el.setAttribute("primary", primary.equalsIgnoreCase("archive") ? "archive" : "physical");
		el.setAttribute("inspect-template", ConfigWebUtil.inspectTemplate(inspect, ""));
		el.setAttribute("virtual", virtual);
	}

	public void updateComponentMapping(String virtual, String physical, String archive, String primary, short inspect) throws ExpressionException, SecurityException {
		checkWriteAccess();
		_updateComponentMapping(virtual, physical, archive, primary, inspect);
	}

	private void _updateComponentMapping(String virtual, String physical, String archive, String primary, short inspect) throws ExpressionException {
		primary = primary.equalsIgnoreCase("archive") ? "archive" : "physical";
		if (physical == null) physical = "";
		else physical = physical.trim();

		if (archive == null) archive = "";
		else archive = archive.trim();

		boolean isArchive = primary.equalsIgnoreCase("archive");
		if (isArchive && archive.length() == 0) {
			throw new ExpressionException("archive must have a value when primary has value archive");
		}
		if (!isArchive && physical.length() == 0) {
			throw new ExpressionException("physical must have a value when primary has value physical");
		}

		Element mappings = _getRootElement("component");
		Element[] children = XMLConfigWebFactory.getChildren(mappings, "mapping");
		Element el;

		/*
		 * ignore when exists for(int i=0;i<children.length;i++) { el=children[i];
		 * if(el.getAttribute("physical").equals(physical) && el.getAttribute("archive").equals(archive) &&
		 * el.getAttribute("primary").equals(primary) &&
		 * el.getAttribute("trusted").equals(Caster.toString(trusted))){ return; } }
		 */

		// Update
		String v;
		for (int i = 0; i < children.length; i++) {
			el = children[i];
			v = createVirtual(el); // if there is no virtual definition (old records), we use the position
			if (v.equals(virtual)) {
				el.setAttribute("virtual", v); // set to make sure it exists for the future
				el.setAttribute("physical", physical);
				el.setAttribute("archive", archive);
				el.setAttribute("primary", primary.equalsIgnoreCase("archive") ? "archive" : "physical");
				el.setAttribute("inspect-template", ConfigWebUtil.inspectTemplate(inspect, ""));
				el.removeAttribute("trusted");
				return;
			}
		}

		// Insert
		el = doc.createElement("mapping");
		mappings.appendChild(el);
		if (physical.length() > 0) el.setAttribute("physical", physical);
		if (archive.length() > 0) el.setAttribute("archive", archive);
		el.setAttribute("primary", primary.equalsIgnoreCase("archive") ? "archive" : "physical");
		el.setAttribute("inspect-template", ConfigWebUtil.inspectTemplate(inspect, ""));
		el.setAttribute("virtual", virtual);
	}

	public static String createVirtual(Element el) {
		String str = el.getAttribute("virtual");
		if (!StringUtil.isEmpty(str)) return str;

		return createVirtual(el.getAttribute("physical"), el.getAttribute("archive"));
	}

	public static String createVirtual(String physical, String archive) {
		return "/" + MD5.getDigestAsString(physical + ":" + archive, "");
	}

	public void updateJar(Resource resJar) throws IOException, BundleException {
		updateJar(config, resJar, true);
	}

	public static void updateJar(Config config, Resource resJar, boolean reloadWhenClassicJar) throws IOException, BundleException {
		BundleFile bf = BundleFile.getInstance(resJar);

		// resJar is a bundle
		if (bf.isBundle()) {
			bf = installBundle(config, bf);
			OSGiUtil.loadBundle(bf);
			return;
		}

		Resource lib = ((ConfigImpl) config).getLibraryDirectory();
		if (!lib.exists()) lib.mkdir();
		Resource fileLib = lib.getRealResource(resJar.getName());

		// if there is an existing, has the file changed?
		if (fileLib.length() != resJar.length()) {
			IOUtil.closeEL(config.getClassLoader());
			ResourceUtil.copy(resJar, fileLib);
			if (reloadWhenClassicJar) ConfigWebUtil.reloadLib(config);
		}
	}

	/*
	 * important! returns null when not a bundle!
	 */
	static BundleFile installBundle(Config config, Resource resJar, String extVersion, boolean convert2bundle) throws IOException, BundleException {

		BundleFile bf = BundleFile.getInstance(resJar);

		// resJar is a bundle
		if (bf.isBundle()) {
			return installBundle(config, bf);
		}

		if (!convert2bundle) return null;

		// name
		String name = bf.getSymbolicName();
		if (StringUtil.isEmpty(name)) name = BundleBuilderFactory.createSymbolicName(resJar);

		// version
		Version version = bf.getVersion();
		if (version == null) version = OSGiUtil.toVersion(extVersion);

		LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_INFO, XMLConfigAdmin.class.getName(), "failed to load [" + resJar + "] as OSGi Bundle");
		BundleBuilderFactory bbf = new BundleBuilderFactory(resJar, name);
		bbf.setVersion(version);
		bbf.setIgnoreExistingManifest(false);
		bbf.build();

		bf = BundleFile.getInstance(resJar);
		LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_INFO, XMLConfigAdmin.class.getName(), "converted  [" + resJar + "] to an OSGi Bundle");
		return installBundle(config, bf);
	}

	private static BundleFile installBundle(Config config, BundleFile bf) throws IOException, BundleException {

		// does this bundle already exists
		BundleFile _bf = OSGiUtil.getBundleFile(bf.getSymbolicName(), bf.getVersion(), null, null, false, null);
		if (_bf != null) return _bf;

		CFMLEngine engine = CFMLEngineFactory.getInstance();
		CFMLEngineFactory factory = engine.getCFMLEngineFactory();

		// copy to jar directory
		File jar = new File(factory.getBundleDirectory(), bf.getSymbolicName() + "-" + bf.getVersion().toString() + (".jar"));

		InputStream is = bf.getInputStream();
		OutputStream os = new FileOutputStream(jar);
		try {
			IOUtil.copy(is, os, false, false);
		}
		finally {
			IOUtil.close(is, os);
		}

		return BundleFile.getInstance(jar);
	}

	/**
	 * 
	 * @param config
	 * @param is
	 * @param name
	 * @param extensionVersion if given jar is no bundle the extension version is used for the bundle
	 *            created
	 * @param closeStream
	 * @return
	 * @throws IOException
	 * @throws BundleException
	 */
	static Bundle updateBundle(Config config, InputStream is, String name, String extensionVersion, boolean closeStream, boolean isPack200) throws IOException, BundleException {
		Object obj = installBundle(config, is, name, extensionVersion, closeStream, false, isPack200);
		if (!(obj instanceof BundleFile)) throw new BundleException("input is not an OSGi Bundle.");

		BundleFile bf = (BundleFile) obj;
		return OSGiUtil.loadBundle(bf);
	}

	/**
	 * @param config
	 * @param is
	 * @param name
	 * @param extensionVersion
	 * @param closeStream
	 * @param convert2bundle
	 * @return return the Bundle File or the file in case it is not a bundle.
	 * @throws IOException
	 * @throws BundleException
	 */
	public static Object installBundle(Config config, InputStream is, String name, String extensionVersion, boolean closeStream, boolean convert2bundle, boolean isPack200)
			throws IOException, BundleException {
		Resource tmp = SystemUtil.getTempDirectory().getRealResource(isPack200 ? Pack200Util.removePack200Ext(name) : name);
		OutputStream os = tmp.getOutputStream();
		if (isPack200) Pack200Util.pack2Jar(is, os, closeStream, true);
		else IOUtil.copy(is, os, closeStream, true);

		BundleFile bf = installBundle(config, tmp, extensionVersion, convert2bundle);
		if (bf != null) {
			tmp.delete();
			return bf;
		}
		return tmp;
	}

	static void updateJar(Config config, InputStream is, String name, boolean closeStream) throws IOException, BundleException {
		Resource tmp = SystemUtil.getTempDirectory().getRealResource(name);
		try {
			IOUtil.copy(is, tmp, closeStream);
			updateJar(config, tmp, true);
		}
		finally {
			tmp.delete();
		}
	}

	/**
	 * insert or update a Java CFX Tag
	 * 
	 * @param name
	 * @param strClass
	 * @throws PageException
	 */
	public void updateJavaCFX(String name, ClassDefinition cd) throws PageException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_CFX_SETTING);

		if (!hasAccess) throw new SecurityException("no access to change cfx settings");

		if (name == null || name.length() == 0) throw new ExpressionException("class name can't be an empty value");

		renameOldstyleCFX();

		Element tags = _getRootElement("ext-tags");

		// Update
		Element[] children = XMLConfigWebFactory.getChildren(tags, "ext-tag");
		for (int i = 0; i < children.length; i++) {
			String n = children[i].getAttribute("name");

			if (n != null && n.equalsIgnoreCase(name)) {
				Element el = children[i];
				if (!"java".equalsIgnoreCase(el.getAttribute("type"))) throw new ExpressionException("there is already a c++ cfx tag with this name");
				setClass(el, CustomTag.class, "", cd);
				el.setAttribute("type", "java");
				return;
			}

		}

		// Insert
		Element el = doc.createElement("ext-tag");
		tags.appendChild(el);
		setClass(el, CustomTag.class, "", cd);
		el.setAttribute("name", name);
		el.setAttribute("type", "java");
	}

	private void renameOldstyleCFX() {

		Element tags = _getRootElement("ext-tags", false, true);
		if (tags != null) return;
		tags = _getRootElement("cfx-tags", false, true);
		if (tags == null) return;

		Element newTags = _getRootElement("ext-tags");
		Element[] children = XMLConfigWebFactory.getChildren(tags, "cfx-tag");
		String type;
		// copy
		for (int i = 0; i < children.length; i++) {
			Element el = doc.createElement("ext-tag");
			newTags.appendChild(el);
			type = children[i].getAttribute("type");
			// java
			if (type.equalsIgnoreCase("java")) {
				el.setAttribute("class", children[i].getAttribute("class"));
			}
			// c++
			else {
				el.setAttribute("server-library", children[i].getAttribute("server-library"));
				el.setAttribute("procedure", children[i].getAttribute("procedure"));
				el.setAttribute("keep-alive", children[i].getAttribute("keep-alive"));

			}
			el.setAttribute("name", children[i].getAttribute("name"));
			el.setAttribute("type", children[i].getAttribute("type"));
		}

		// remove old
		for (int i = 0; i < children.length; i++) {
			tags.removeChild(children[i]);
		}
		tags.getParentNode().removeChild(tags);
	}

	public static boolean fixLFI(Document doc) {
		return "lucee-configuration".equals(doc.getDocumentElement().getNodeName());

	}

	/**
	 * make sure every context has a salt
	 */
	public static boolean fixSaltAndPW(Document doc, Config config) {
		Element root = doc.getDocumentElement();

		// salt
		String salt = root.getAttribute("salt");
		boolean rtn = false;
		if (StringUtil.isEmpty(salt, true) || !Decision.isUUId(salt)) {
			// create salt
			root.setAttribute("salt", salt = CreateUUID.invoke());
			rtn = true;
		}

		// no password yet
		if (config instanceof ConfigServer && !root.hasAttribute("hspw") && !root.hasAttribute("pw") && !root.hasAttribute("password")) {
			ConfigServer cs = (ConfigServer) config;
			Resource pwFile = cs.getConfigDir().getRealResource("password.txt");
			if (pwFile.isFile()) {
				try {
					String pw = IOUtil.toString(pwFile, (Charset) null);
					if (!StringUtil.isEmpty(pw, true)) {
						pw = pw.trim();
						String hspw = new PasswordImpl(Password.ORIGIN_UNKNOW, pw, salt).getPassword();
						root.setAttribute("hspw", hspw);
						pwFile.delete();
						rtn = true;
					}
				}
				catch (IOException e) {
					LogUtil.logGlobal(cs, "application", e);
				}
			}
			else {
				LogUtil.log(config, Log.LEVEL_ERROR, "application", "no password set and no password file found at [" + pwFile + "]");
			}
		}
		return rtn;
	}

	// MUST remove
	public static boolean fixPSQ(Document doc) {

		Element datasources = XMLConfigWebFactory.getChildByName(doc.getDocumentElement(), "data-sources", false, true);
		if (datasources != null && datasources.hasAttribute("preserve-single-quote")) {
			Boolean b = Caster.toBoolean(datasources.getAttribute("preserve-single-quote"), null);
			if (b != null) datasources.setAttribute("psq", Caster.toString(!b.booleanValue()));
			datasources.removeAttribute("preserve-single-quote");
			return true;
		}
		return false;
	}

	/**
	 * the following code remove all logging definitions spread over the complete xml and adds them to
	 * the new "logging" tag
	 * 
	 * @param doc
	 * @return
	 */
	public static boolean fixLogging(ConfigServerImpl cs, ConfigImpl config, Document doc) {

		// if version is bigger than 4.2 there is nothing to do
		Element luceeConfiguration = doc.getDocumentElement();
		String strVersion = luceeConfiguration.getAttribute("version");
		double version = Caster.toDoubleValue(strVersion, 1.0d);
		config.setVersion(version);

		if (version >= 4.3D) return false;

		// datasource
		Element src = XMLConfigWebFactory.getChildByName(doc.getDocumentElement(), "datasource");
		fixLogging(cs, doc, src, "datasource", false, "{lucee-config}/logs/datasource.log");

		setVersion(doc, ConfigWebUtil.getEngine(config).getInfo().getVersion());

		if (version >= 4.2D) return true;

		// mapping
		src = XMLConfigWebFactory.getChildByName(doc.getDocumentElement(), "mappings");
		fixLogging(cs, doc, src, "mapping", false, "{lucee-config}/logs/mapping.log");

		// rest
		src = XMLConfigWebFactory.getChildByName(doc.getDocumentElement(), "rest");
		fixLogging(cs, doc, src, "rest", false, "{lucee-config}/logs/rest.log");

		// gateway
		src = XMLConfigWebFactory.getChildByName(doc.getDocumentElement(), "gateways");
		fixLogging(cs, doc, src, "gateway", false, "{lucee-config}/logs/gateway.log");

		// remote clients
		src = XMLConfigWebFactory.getChildByName(doc.getDocumentElement(), "remote-clients");
		fixLogging(cs, doc, src, "remoteclient", false, "{lucee-config}/logs/remoteclient.log");

		// orm
		src = XMLConfigWebFactory.getChildByName(doc.getDocumentElement(), "orm");
		fixLogging(cs, doc, src, "orm", false, "{lucee-config}/logs/orm.log");

		// mail
		src = XMLConfigWebFactory.getChildByName(doc.getDocumentElement(), "mail");
		fixLogging(cs, doc, src, "mail", false, "{lucee-config}/logs/mail.log");

		// search
		src = XMLConfigWebFactory.getChildByName(doc.getDocumentElement(), "search");
		fixLogging(cs, doc, src, "search", false, "{lucee-config}/logs/search.log");

		// scheduler
		src = XMLConfigWebFactory.getChildByName(doc.getDocumentElement(), "scheduler");
		fixLogging(cs, doc, src, "scheduler", false, "{lucee-config}/logs/scheduler.log");

		// scope
		src = XMLConfigWebFactory.getChildByName(doc.getDocumentElement(), "scope");
		fixLogging(cs, doc, src, "scope", false, "{lucee-config}/logs/scope.log");

		// application
		Element app = src = XMLConfigWebFactory.getChildByName(doc.getDocumentElement(), "application");
		fixLogging(cs, doc, src, "application", "application-log", "application-log-level", false, "{lucee-config}/logs/application.log");

		// exception
		fixLogging(cs, doc, app, "exception", "exception-log", "exception-log-level", false, "{lucee-config}/logs/exception.log");

		// trace
		fixLogging(cs, doc, app, "trace", "trace-log", "trace-log-level", false, "{lucee-config}/logs/trace.log");

		// thread
		fixLogging(cs, doc, app, "thread", "thread-log", "thread-log-level", false, "{lucee-config}/logs/thread.log");

		// deploy
		fixLogging(cs, doc, app, "deploy", "deploy-log", "deploy-log-level", false, "{lucee-config}/logs/deploy.log");

		// requesttimeout
		fixLogging(cs, doc, app, "requesttimeout", "requesttimeout-log", "requesttimeout-log-level", false, "{lucee-config}/logs/requesttimeout.log");

		setVersion(doc, ConfigWebUtil.getEngine(config).getInfo().getVersion());

		return true;
	}

	private static boolean fixLogging(ConfigServerImpl cs, Document doc, Element src, String name, boolean deleteSourceAttributes, String defaultValue) {
		return fixLogging(cs, doc, src, name, "log", "log-level", deleteSourceAttributes, defaultValue);
	}

	private static boolean fixLogging(ConfigServerImpl cs, Document doc, Element src, String name, String logName, String levelName, boolean deleteSourceAttributes,
			String defaultValue) {

		String path, level;
		// Mapping logging

		path = src.getAttribute(logName);
		level = src.getAttribute(levelName);

		if (StringUtil.isEmpty(path) && !StringUtil.isEmpty(defaultValue) && (cs == null || cs.getLog(name) == null)) {
			// ignore defaultValue, when there is a setting in server context
			path = defaultValue;
		}
		if (!StringUtil.isEmpty(path)) {
			if (deleteSourceAttributes) src.removeAttribute(logName);
			if (deleteSourceAttributes) src.removeAttribute(levelName);

			Element logging = XMLConfigWebFactory.getChildByName(doc.getDocumentElement(), "logging");

			// first of all we have to make sure this is not already existing, if it does we ignore the old
			// settings
			Element[] children = XMLUtil.getChildElementsAsArray(logging);
			for (int i = 0; i < children.length; i++) {
				if (children[i].getTagName().equals("logger") && name.equalsIgnoreCase(children[i].getAttribute("name"))) {
					return false;
				}
			}

			LogUtil.log(ThreadLocalPageContext.getConfig(cs), Log.LEVEL_INFO, XMLConfigAdmin.class.getName(), "move " + name + " logging");
			Element logger = doc.createElement("logger");
			logger.setAttribute("name", name);
			if ("console".equalsIgnoreCase(path)) {
				try {
					setClass(logger, null, "appender-", cs.getLogEngine().appenderClassDefintion("console"));
					setClass(logger, null, "layout-", cs.getLogEngine().layoutClassDefintion("pattern"));
				}
				catch (PageException e) {}
			}
			else {
				try {
					setClass(logger, null, "appender-", cs.getLogEngine().appenderClassDefintion("resource"));
					setClass(logger, null, "layout-", cs.getLogEngine().layoutClassDefintion("classic"));
				}
				catch (PageException e) {}

				logger.setAttribute("appender-arguments", "path:" + path);
			}

			if (!StringUtil.isEmpty(level, true)) logger.setAttribute("level", level.trim());

			logging.appendChild(logger);

			return true;
		}
		return false;
	}

	public static boolean fixS3(Document doc) {
		Element resources = XMLConfigWebFactory.getChildByName(doc.getDocumentElement(), "resources", false, true);

		Element[] providers = XMLConfigWebFactory.getChildren(resources, "resource-provider");

		// replace extension class with core class
		boolean fixed = false;
		if (providers != null) {
			for (int i = 0; i < providers.length; i++) {
				if ("s3".equalsIgnoreCase(providers[i].getAttribute("scheme"))) {
					if ("lucee.extension.io.resource.type.s3.S3ResourceProvider".equalsIgnoreCase(providers[i].getAttribute("class"))
							|| "lucee.commons.io.res.type.s3.S3ResourceProvider".equalsIgnoreCase(providers[i].getAttribute("class"))) {
						resources.removeChild(providers[i]);
						fixed = true;
					}
				}
			}
		}
		return fixed;
	}

	public static boolean fixComponentMappings(ConfigImpl config, Document doc) {
		if (!(config instanceof ConfigServer)) return false;

		Element parent = XMLConfigWebFactory.getChildByName(doc.getDocumentElement(), "component", false, true);
		Element[] mappings = XMLConfigWebFactory.getChildren(parent, "mapping");

		int count = 0;
		for (Element mapping: mappings) {
			if ("/default-server".equalsIgnoreCase(mapping.getAttribute("virtual"))) {
				count++;
				if (count > 1) {
					parent.removeChild(mapping);
				}
			}
		}

		if (count > 0) return false;

		// ADD MAPPING
		Element mapping = doc.createElement("mapping");
		parent.appendChild(mapping);
		mapping.setAttribute("virtual", "/default-server");
		mapping.setAttribute("physical", "{lucee-server}/components/");
		mapping.setAttribute("primary", "physical");
		mapping.setAttribute("inspect-template", "never");
		mapping.setAttribute("readonly", "true");
		return true;
	}

	public void verifyCFX(String name) throws PageException {
		CFXTagPool pool = config.getCFXTagPool();
		CustomTag ct = null;
		try {
			ct = pool.getCustomTag(name);
		}
		catch (CFXTagException e) {
			throw Caster.toPageException(e);
		}
		finally {
			if (ct != null) pool.releaseCustomTag(ct);
		}

	}

	public void verifyJavaCFX(String name, ClassDefinition cd) throws PageException {
		try {
			Class clazz = cd.getClazz();
			if (!Reflector.isInstaneOf(clazz, CustomTag.class, false))
				throw new ExpressionException("class [" + cd + "] must implement interface [" + CustomTag.class.getName() + "]");
		}
		catch (ClassException e) {
			throw Caster.toPageException(e);
		}
		catch (BundleException e) {
			throw Caster.toPageException(e);
		}

		if (StringUtil.startsWithIgnoreCase(name, "cfx_")) name = name.substring(4);
		if (StringUtil.isEmpty(name)) throw new ExpressionException("class name can't be an empty value");
	}

	/**
	 * remove a CFX Tag
	 * 
	 * @param name
	 * @throws ExpressionException
	 * @throws SecurityException
	 */
	public void removeCFX(String name) throws ExpressionException, SecurityException {
		checkWriteAccess();
		// check parameters
		if (name == null || name.length() == 0) throw new ExpressionException("name for CFX Tag can be an empty value");

		renameOldstyleCFX();

		Element mappings = _getRootElement("ext-tags");

		Element[] children = XMLConfigWebFactory.getChildren(mappings, "ext-tag");
		for (int i = 0; i < children.length; i++) {
			String n = children[i].getAttribute("name");
			if (n != null && n.equalsIgnoreCase(name)) {
				mappings.removeChild(children[i]);
			}
		}
	}

	/**
	 * update or insert new database connection
	 * 
	 * @param name
	 * @param clazzName
	 * @param dsn
	 * @param username
	 * @param password
	 * @param host
	 * @param database
	 * @param port
	 * @param connectionLimit
	 * @param connectionTimeout
	 * @param blob
	 * @param clob
	 * @param allow
	 * @param storage
	 * @param custom
	 * @throws PageException
	 */
	public void updateDataSource(String id, String name, String newName, ClassDefinition cd, String dsn, String username, String password, String host, String database, int port,
			int connectionLimit, int idleTimeout, int liveTimeout, long metaCacheTimeout, boolean blob, boolean clob, int allow, boolean validate, boolean storage, String timezone,
			Struct custom, String dbdriver, ParamSyntax paramSyntax, boolean literalTimestampWithTSOffset, boolean alwaysSetTimeout, boolean requestExclusive,
			boolean alwaysResetConnections) throws PageException {

		checkWriteAccess();
		SecurityManager sm = config.getSecurityManager();
		short access = sm.getAccess(SecurityManager.TYPE_DATASOURCE);
		boolean hasAccess = true;
		boolean hasInsertAccess = true;
		int maxLength = 0;

		if (access == SecurityManager.VALUE_YES) hasAccess = true;
		else if (access == SecurityManager.VALUE_NO) hasAccess = false;
		else if (access >= SecurityManager.VALUE_1 && access <= SecurityManager.VALUE_10) {
			int existingLength = getDatasourceLength(config);
			maxLength = access - SecurityManager.NUMBER_OFFSET;
			hasInsertAccess = maxLength > existingLength;
			// print.ln("maxLength:"+maxLength);
			// print.ln("existingLength:"+existingLength);
		}
		// print.ln("hasAccess:"+hasAccess);
		// print.ln("hasInsertAccess:"+hasInsertAccess);

		// boolean hasAccess=ConfigWebUtil.hasAccess(config,SecurityManager.TYPE_DATASOURCE);
		if (!hasAccess) throw new SecurityException("no access to update datsource connections");

		// check parameters
		if (name == null || name.length() == 0) throw new ExpressionException("name can't be an empty value");

		Element datasources = _getRootElement("data-sources");

		// Update
		Element[] children = XMLConfigWebFactory.getChildren(datasources, "data-source");
		for (int i = 0; i < children.length; i++) {
			String n = children[i].getAttribute("name");

			if (n.equalsIgnoreCase(name)) {
				Element el = children[i];
				if (password.equalsIgnoreCase("****************")) password = el.getAttribute("password");

				if (!StringUtil.isEmpty(newName) && !newName.equals(name)) el.setAttribute("name", newName);
				setClass(el, null, "", cd);

				if (!StringUtil.isEmpty(id)) el.setAttribute("id", id);
				else if (el.hasAttribute("id")) el.removeAttribute("id");

				el.setAttribute("dsn", dsn);
				el.setAttribute("username", username);
				el.setAttribute("password", ConfigWebUtil.encrypt(password));

				el.setAttribute("host", host);
				if (!StringUtil.isEmpty(timezone)) el.setAttribute("timezone", timezone);
				else if (el.hasAttribute("timezone")) el.removeAttribute("timezone");
				el.setAttribute("database", database);
				el.setAttribute("port", Caster.toString(port));
				el.setAttribute("connectionLimit", Caster.toString(connectionLimit));
				el.setAttribute("connectionTimeout", Caster.toString(idleTimeout));
				el.setAttribute("liveTimeout", Caster.toString(liveTimeout));
				el.setAttribute("metaCacheTimeout", Caster.toString(metaCacheTimeout));
				el.setAttribute("blob", Caster.toString(blob));
				el.setAttribute("clob", Caster.toString(clob));
				el.setAttribute("allow", Caster.toString(allow));
				el.setAttribute("validate", Caster.toString(validate));
				el.setAttribute("storage", Caster.toString(storage));
				el.setAttribute("custom", toStringURLStyle(custom));

				if (!StringUtil.isEmpty(dbdriver)) el.setAttribute("dbdriver", Caster.toString(dbdriver));

				// Param Syntax
				el.setAttribute("param-delimiter", (paramSyntax.delimiter));
				el.setAttribute("param-leading-delimiter", (paramSyntax.leadingDelimiter));
				el.setAttribute("param-separator", (paramSyntax.separator));

				if (literalTimestampWithTSOffset) el.setAttribute("literal-timestamp-with-tsoffset", "true");
				else if (el.hasAttribute("literal-timestamp-with-tsoffset")) el.removeAttribute("literal-timestamp-with-tsoffset");

				if (alwaysSetTimeout) el.setAttribute("always-set-timeout", "true");
				else if (el.hasAttribute("always-set-timeout")) el.removeAttribute("always-set-timeout");

				if (requestExclusive) el.setAttribute("request-exclusive", "true");
				else if (el.hasAttribute("request-exclusive")) el.removeAttribute("request-exclusive");

				if (alwaysResetConnections) el.setAttribute("always-reset-connections", "true");
				else if (el.hasAttribute("always-reset-connections")) el.removeAttribute("always-reset-connections");

				return;
			}
		}

		if (!hasInsertAccess) throw new SecurityException("no access to add datasource connections, the maximum count of [" + maxLength + "] datasources is reached");

		// Insert
		Element el = doc.createElement("data-source");
		datasources.appendChild(el);
		if (!StringUtil.isEmpty(newName)) el.setAttribute("name", newName);
		else el.setAttribute("name", name);
		setClass(el, null, "", cd);
		el.setAttribute("dsn", dsn);

		if (!StringUtil.isEmpty(id)) el.setAttribute("id", id);
		else if (el.hasAttribute("id")) el.removeAttribute("id");

		if (username.length() > 0) el.setAttribute("username", username);
		if (password.length() > 0) el.setAttribute("password", ConfigWebUtil.encrypt(password));

		el.setAttribute("host", host);
		if (!StringUtil.isEmpty(timezone)) el.setAttribute("timezone", timezone);
		el.setAttribute("database", database);
		if (port > -1) el.setAttribute("port", Caster.toString(port));
		if (connectionLimit > -1) el.setAttribute("connectionLimit", Caster.toString(connectionLimit));
		if (idleTimeout > -1) el.setAttribute("connectionTimeout", Caster.toString(idleTimeout));
		if (liveTimeout > -1) el.setAttribute("liveTimeout", Caster.toString(liveTimeout));
		if (metaCacheTimeout > -1) el.setAttribute("metaCacheTimeout", Caster.toString(metaCacheTimeout));

		el.setAttribute("blob", Caster.toString(blob));
		el.setAttribute("clob", Caster.toString(clob));
		el.setAttribute("validate", Caster.toString(validate));
		el.setAttribute("storage", Caster.toString(storage));
		if (allow > -1) el.setAttribute("allow", Caster.toString(allow));
		el.setAttribute("custom", toStringURLStyle(custom));

		if (!StringUtil.isEmpty(dbdriver)) el.setAttribute("dbdriver", Caster.toString(dbdriver));

		// Param Syntax
		el.setAttribute("param-delimiter", (paramSyntax.delimiter));
		el.setAttribute("param-leading-delimiter", (paramSyntax.leadingDelimiter));
		el.setAttribute("param-separator", (paramSyntax.separator));

		if (literalTimestampWithTSOffset) el.setAttribute("literal-timestamp-with-tsoffset", "true");
		if (alwaysSetTimeout) el.setAttribute("always-set-timeout", "true");
		if (requestExclusive) el.setAttribute("request-exclusive", "true");
		if (alwaysResetConnections) el.setAttribute("always-reset-connections", "true");

	}

	static void removeJDBCDriver(ConfigImpl config, ClassDefinition cd, boolean reload) throws IOException, SAXException, PageException, BundleException {
		XMLConfigAdmin admin = new XMLConfigAdmin(config, null);
		admin._removeJDBCDriver(cd);
		admin._store(); // store is necessary, otherwise it get lost

		if (reload) admin._reload();
	}

	private void _removeJDBCDriver(ClassDefinition cd) throws PageException {

		if (!cd.isBundle()) throw new ApplicationException("missing bundle name");

		Element parent = _getRootElement("jdbc");

		// Remove
		Element[] children = XMLConfigWebFactory.getChildren(parent, "driver");
		for (int i = 0; i < children.length; i++) {
			String n = children[i].getAttribute("class");
			if (n.equalsIgnoreCase(cd.getClassName())) {
				parent.removeChild(children[i]);
				break;
			}
		}

		// now unload (maybe not necessary)
		if (cd.isBundle()) {
			Bundle bl = OSGiUtil.getBundleLoaded(cd.getName(), cd.getVersion(), null);
			if (bl != null) {
				try {
					OSGiUtil.uninstall(bl);
				}
				catch (BundleException e) {}
			}
		}
	}

	private void _removeStartupHook(ClassDefinition cd) throws PageException {

		if (!cd.isBundle()) throw new ApplicationException("missing bundle name");

		Element parent = _getRootElement("startup");

		// Remove
		Element[] children = XMLConfigWebFactory.getChildren(parent, "hook");
		for (int i = 0; i < children.length; i++) {
			String n = children[i].getAttribute("class");
			if (n.equalsIgnoreCase(cd.getClassName())) {
				parent.removeChild(children[i]);
				break;
			}
		}

		// now unload (maybe not necessary)
		if (cd.isBundle()) {
			unloadStartupIfNecessary(config, cd, true);
			Bundle bl = OSGiUtil.getBundleLoaded(cd.getName(), cd.getVersion(), null);
			if (bl != null) {
				try {
					OSGiUtil.uninstall(bl);
				}
				catch (BundleException e) {}
			}
		}
	}

	private void unloadStartupIfNecessary(ConfigImpl config, ClassDefinition<?> cd, boolean force) {
		Startup startup = config.getStartups().get(cd.getClassName());
		if (startup == null) return;
		if (startup.cd.equals(cd) && !force) return;

		try {
			Method fin = Reflector.getMethod(startup.instance.getClass(), "finalize", new Class[0], null);
			if (fin != null) {
				fin.invoke(startup.instance, new Object[0]);
			}
			config.getStartups().remove(cd.getClassName());
		}
		catch (Exception e) {}
	}

	/*
	 * public static void updateJDBCDriver(ConfigImpl config, String label, ClassDefinition cd, boolean
	 * reload) throws IOException, SAXException, PageException, BundleException { ConfigWebAdmin admin =
	 * new ConfigWebAdmin(config, null); admin._updateJDBCDriver(label,cd); admin._store(); // store is
	 * necessary, otherwise it get lost if(reload)admin._reload(); }
	 */

	public void updateJDBCDriver(String label, String id, ClassDefinition cd) throws PageException {
		checkWriteAccess();
		_updateJDBCDriver(label, id, cd);
	}

	private void _updateJDBCDriver(String label, String id, ClassDefinition cd) throws PageException {

		// check if label exists
		if (StringUtil.isEmpty(label)) throw new ApplicationException("missing label for jdbc driver [" + cd.getClassName() + "]");
		// check if it is a bundle
		if (!cd.isBundle()) throw new ApplicationException("missing bundle name for [" + label + "]");

		Element parent = _getRootElement("jdbc");

		// Update
		Element child = null;
		Element[] children = XMLConfigWebFactory.getChildren(parent, "driver");
		for (int i = 0; i < children.length; i++) {
			String n = children[i].getAttribute("class");
			if (n.equalsIgnoreCase(cd.getClassName())) {
				child = children[i];
				break;
			}
		}

		// Insert
		if (child == null) {
			child = doc.createElement("driver");
			parent.appendChild(child);
		}

		child.setAttribute("label", label);
		if (!StringUtil.isEmpty(id)) child.setAttribute("id", id);
		else child.removeAttribute("id");
		// make sure the class exists
		setClass(child, null, "", cd);

		// now unload again, JDBC driver can be loaded when necessary
		if (cd.isBundle()) {
			Bundle bl = OSGiUtil.getBundleLoaded(cd.getName(), cd.getVersion(), null);
			if (bl != null) {
				try {
					OSGiUtil.uninstall(bl);
				}
				catch (BundleException e) {}
			}
		}
	}

	private void _updateStartupHook(ClassDefinition cd) throws PageException {
		unloadStartupIfNecessary(config, cd, false);
		// check if it is a bundle
		if (!cd.isBundle()) throw new ApplicationException("missing bundle info");

		Element parent = _getRootElement("startup");

		// Update
		Element child = null;
		Element[] children = XMLConfigWebFactory.getChildren(parent, "hook");
		for (int i = 0; i < children.length; i++) {
			String n = children[i].getAttribute("class");
			if (n.equalsIgnoreCase(cd.getClassName())) {
				child = children[i];
				break;
			}
		}

		// Insert
		if (child == null) {
			child = doc.createElement("hook");
			parent.appendChild(child);
		}

		// make sure the class exists
		setClass(child, null, "", cd);

		// now unload again, JDBC driver can be loaded when necessary
		if (cd.isBundle()) {
			Bundle bl = OSGiUtil.getBundleLoaded(cd.getName(), cd.getVersion(), null);
			if (bl != null) {
				try {
					OSGiUtil.uninstall(bl);
				}
				catch (BundleException e) {}
			}
		}
	}

	public void updateGatewayEntry(String id, ClassDefinition cd, String componentPath, String listenerCfcPath, int startupMode, Struct custom, boolean readOnly)
			throws PageException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_GATEWAY);
		if (!hasAccess) throw new SecurityException("no access to update gateway entry");

		_updateGatewayEntry(id, cd, componentPath, listenerCfcPath, startupMode, custom, readOnly);
	}

	void _updateGatewayEntry(String id, ClassDefinition cd, String componentPath, String listenerCfcPath, int startupMode, Struct custom, boolean readOnly) throws PageException {

		// check parameters
		id = id.trim();
		if (StringUtil.isEmpty(id)) throw new ExpressionException("id can't be an empty value");

		if ((cd == null || StringUtil.isEmpty(cd.getClassName())) && StringUtil.isEmpty(componentPath)) throw new ExpressionException("you must define className or componentPath");

		Element parent = _getRootElement("gateways");

		// Update
		Element[] children = XMLConfigWebFactory.getChildren(parent, "gateway");
		for (int i = 0; i < children.length; i++) {
			String n = children[i].getAttribute("id");
			Element el = children[i];
			if (n.equalsIgnoreCase(id)) {
				setClass(el, null, "", cd);
				el.setAttribute("cfc-path", componentPath);
				el.setAttribute("listener-cfc-path", listenerCfcPath);
				el.setAttribute("startup-mode", GatewayEntryImpl.toStartup(startupMode, "automatic"));
				el.setAttribute("custom", toStringURLStyle(custom));
				el.setAttribute("read-only", Caster.toString(readOnly));
				return;
			}

		}
		// Insert
		Element el = doc.createElement("gateway");
		parent.appendChild(el);
		el.setAttribute("id", id);
		el.setAttribute("cfc-path", componentPath);
		el.setAttribute("listener-cfc-path", listenerCfcPath);
		el.setAttribute("startup-mode", GatewayEntryImpl.toStartup(startupMode, "automatic"));
		setClass(el, null, "", cd);
		el.setAttribute("custom", toStringURLStyle(custom));
		el.setAttribute("read-only", Caster.toString(readOnly));

	}

	static void removeSearchEngine(ConfigImpl config, boolean reload) throws IOException, SAXException, PageException, BundleException {
		XMLConfigAdmin admin = new XMLConfigAdmin(config, null);
		admin._removeSearchEngine();
		admin._store();
		if (reload) admin._reload();
	}

	private void _removeSearchEngine() {
		Element orm = _getRootElement("search");
		removeClass(orm, "engine-");
	}

	private void _removeAMFEngine() {
		Element flex = _getRootElement("flex");
		removeClass(flex, "");
		flex.removeAttribute("configuration");
		flex.removeAttribute("caster");

		// old arguments
		flex.removeAttribute("config");
		flex.removeAttribute("caster-class");
		flex.removeAttribute("caster-class-arguments");
	}

	/*
	 * public static void updateSearchEngine(ConfigImpl config, ClassDefinition cd, boolean reload)
	 * throws IOException, SAXException, PageException, BundleException { ConfigWebAdmin admin = new
	 * ConfigWebAdmin(config, null); admin._updateSearchEngine(cd); admin._store();
	 * if(reload)admin._reload(); }
	 */

	public void updateSearchEngine(ClassDefinition cd) throws PageException {
		checkWriteAccess();
		_updateSearchEngine(cd);

	}

	private void _updateSearchEngine(ClassDefinition cd) throws PageException {
		Element orm = _getRootElement("search");
		setClass(orm, SearchEngine.class, "engine-", cd);
	}

	private void _updateAMFEngine(ClassDefinition cd, String caster, String config) throws PageException {
		Element flex = _getRootElement("flex");
		setClass(flex, AMFEngine.class, "", cd);
		if (caster != null) flex.setAttribute("caster", caster);
		if (config != null) flex.setAttribute("configuration", config);
		// old arguments
		flex.removeAttribute("config");
		flex.removeAttribute("caster-class");
		flex.removeAttribute("caster-class-arguments");
	}

	public void removeSearchEngine() throws SecurityException {
		checkWriteAccess();

		Element orm = _getRootElement("search");
		removeClass(orm, "engine-");

	}

	static void removeORMEngine(ConfigImpl config, boolean reload) throws IOException, SAXException, PageException, BundleException {
		XMLConfigAdmin admin = new XMLConfigAdmin(config, null);
		admin._removeORMEngine();
		admin._store();
		if (reload) admin._reload();
	}

	private void _removeORMEngine() {
		Element orm = _getRootElement("orm");
		removeClass(orm, "engine-");
		removeClass(orm, "");// in the beginning we had no prefix
	}

	private void _removeWebserviceHandler() {
		Element orm = _getRootElement("webservice");
		removeClass(orm, "");
	}

	public void removeORMEngine() throws SecurityException {
		checkWriteAccess();
		_removeORMEngine();
	}

	public void updateORMEngine(ClassDefinition cd) throws PageException {
		checkWriteAccess();
		_updateORMEngine(cd);

	}

	private void _updateORMEngine(ClassDefinition cd) throws PageException {
		Element orm = _getRootElement("orm");
		removeClass(orm, "");// in the beginning we had no prefix
		setClass(orm, ORMEngine.class, "engine-", cd);
	}

	private void _updateWebserviceHandler(ClassDefinition cd) throws PageException {
		Element orm = _getRootElement("webservice");
		setClass(orm, null, "", cd);
	}

	public void updateCacheConnection(String name, ClassDefinition cd, int _default, Struct custom, boolean readOnly, boolean storage) throws PageException {

		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_CACHE);
		if (!hasAccess) throw new SecurityException("no access to update cache connection");

		// check parameters
		name = name.trim();
		if (StringUtil.isEmpty(name)) throw new ExpressionException("name can't be an empty value");
		// else if(name.equals("template") || name.equals("object"))
		// throw new ExpressionException("name ["+name+"] is not allowed for a cache connection, the
		// following names are reserved words [object,template]");

		try {
			Class clazz;
			if (cd.getClassName() != null && cd.getClassName().endsWith(".EHCacheLite"))
				clazz = ClassUtil.loadClass(config.getClassLoader(), "org.lucee.extension.cache.eh.EHCache");
			else clazz = ClassUtil.loadClass(config.getClassLoader(), cd.getClassName());

			if (!Reflector.isInstaneOf(clazz, Cache.class, false)) throw new ExpressionException("class [" + clazz.getName() + "] is not of type [" + Cache.class.getName() + "]");
		}
		catch (ClassException e) {
			throw new ExpressionException(e.getMessage());
		}

		Element parent = _getRootElement("cache");

		if (name.equalsIgnoreCase(parent.getAttribute("default-template"))) parent.removeAttribute("default-template");
		if (name.equalsIgnoreCase(parent.getAttribute("default-object"))) parent.removeAttribute("default-object");
		if (name.equalsIgnoreCase(parent.getAttribute("default-query"))) parent.removeAttribute("default-query");
		if (name.equalsIgnoreCase(parent.getAttribute("default-resource"))) parent.removeAttribute("default-resource");
		if (name.equalsIgnoreCase(parent.getAttribute("default-function"))) parent.removeAttribute("default-function");
		if (name.equalsIgnoreCase(parent.getAttribute("default-include"))) parent.removeAttribute("default-include");

		if (_default == ConfigImpl.CACHE_TYPE_OBJECT) {
			parent.setAttribute("default-object", name);
		}
		else if (_default == ConfigImpl.CACHE_TYPE_TEMPLATE) {
			parent.setAttribute("default-template", name);
		}
		else if (_default == ConfigImpl.CACHE_TYPE_QUERY) {
			parent.setAttribute("default-query", name);
		}
		else if (_default == ConfigImpl.CACHE_TYPE_RESOURCE) {
			parent.setAttribute("default-resource", name);
		}
		else if (_default == ConfigImpl.CACHE_TYPE_FUNCTION) {
			parent.setAttribute("default-function", name);
		}
		else if (_default == ConfigImpl.CACHE_TYPE_INCLUDE) {
			parent.setAttribute("default-include", name);
		}
		else if (_default == ConfigImpl.CACHE_TYPE_HTTP) {
			parent.setAttribute("default-http", name);
		}
		else if (_default == ConfigImpl.CACHE_TYPE_FILE) {
			parent.setAttribute("default-file", name);
		}
		else if (_default == ConfigImpl.CACHE_TYPE_WEBSERVICE) {
			parent.setAttribute("default-webservice", name);
		}

		// Update
		// boolean isUpdate=false;
		Element[] children = XMLConfigWebFactory.getChildren(parent, "connection");
		for (int i = 0; i < children.length; i++) {
			String n = children[i].getAttribute("name");
			Element el = children[i];
			if (n.equalsIgnoreCase(name)) {
				setClass(el, null, "", cd);
				el.setAttribute("custom", toStringURLStyle(custom));
				el.setAttribute("read-only", Caster.toString(readOnly));
				el.setAttribute("storage", Caster.toString(storage));
				return;
			}

		}

		// Insert
		Element el = doc.createElement("connection");
		parent.appendChild(el);
		el.setAttribute("name", name);
		setClass(el, null, "", cd);
		el.setAttribute("custom", toStringURLStyle(custom));
		el.setAttribute("read-only", Caster.toString(readOnly));
		el.setAttribute("storage", Caster.toString(storage));

	}

	public void removeCacheDefaultConnection(int type) throws PageException {
		checkWriteAccess();

		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_CACHE);
		if (!hasAccess) throw new SecurityException("no access to update cache connections");

		Element parent = _getRootElement("cache");
		if (type == ConfigImpl.CACHE_TYPE_OBJECT) {
			parent.removeAttribute("default-object");
		}
		else if (type == ConfigImpl.CACHE_TYPE_TEMPLATE) {
			parent.removeAttribute("default-template");
		}
		else if (type == ConfigImpl.CACHE_TYPE_QUERY) {
			parent.removeAttribute("default-query");
		}
		else if (type == ConfigImpl.CACHE_TYPE_RESOURCE) {
			parent.removeAttribute("default-resource");
		}
		else if (type == ConfigImpl.CACHE_TYPE_FUNCTION) {
			parent.removeAttribute("default-function");
		}
		else if (type == ConfigImpl.CACHE_TYPE_INCLUDE) {
			parent.removeAttribute("default-include");
		}
		else if (type == ConfigImpl.CACHE_TYPE_HTTP) {
			parent.removeAttribute("default-http");
		}
		else if (type == ConfigImpl.CACHE_TYPE_FILE) {
			parent.removeAttribute("default-file");
		}
		else if (type == ConfigImpl.CACHE_TYPE_WEBSERVICE) {
			parent.removeAttribute("default-webservice");
		}
	}

	public void updateCacheDefaultConnection(int type, String name) throws PageException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_CACHE);

		if (!hasAccess) throw new SecurityException("no access to update cache default connections");

		Element parent = _getRootElement("cache");
		if (type == ConfigImpl.CACHE_TYPE_OBJECT) {
			parent.setAttribute("default-object", name);
		}
		else if (type == ConfigImpl.CACHE_TYPE_TEMPLATE) {
			parent.setAttribute("default-template", name);
		}
		else if (type == ConfigImpl.CACHE_TYPE_QUERY) {
			parent.setAttribute("default-query", name);
		}
		else if (type == ConfigImpl.CACHE_TYPE_RESOURCE) {
			parent.setAttribute("default-resource", name);
		}
		else if (type == ConfigImpl.CACHE_TYPE_FUNCTION) {
			parent.setAttribute("default-function", name);
		}
		else if (type == ConfigImpl.CACHE_TYPE_INCLUDE) {
			parent.setAttribute("default-include", name);
		}
		else if (type == ConfigImpl.CACHE_TYPE_HTTP) {
			parent.setAttribute("default-http", name);
		}
		else if (type == ConfigImpl.CACHE_TYPE_FILE) {
			parent.setAttribute("default-file", name);
		}
		else if (type == ConfigImpl.CACHE_TYPE_WEBSERVICE) {
			parent.setAttribute("default-webservice", name);
		}
	}

	public void removeResourceProvider(String scheme) throws PageException {
		checkWriteAccess();
		SecurityManager sm = config.getSecurityManager();
		short access = sm.getAccess(SecurityManager.TYPE_FILE);
		boolean hasAccess = access == SecurityManager.VALUE_YES;

		if (!hasAccess) throw new SecurityException("no access to remove resource provider");

		_removeResourceProvider(scheme);
	}

	public void _removeResourceProvider(String scheme) throws PageException {

		Element parent = _getRootElement("resources");

		// remove
		Element[] children = XMLConfigWebFactory.getChildren(parent, "resource-provider");
		for (int i = 0; i < children.length; i++) {
			String elScheme = children[i].getAttribute("scheme");
			if (elScheme.equalsIgnoreCase(scheme)) {
				parent.removeChild(children[i]);
				break;
			}
		}
	}

	public void updateResourceProvider(String scheme, ClassDefinition cd, Struct arguments) throws PageException {
		updateResourceProvider(scheme, cd, toStringCSSStyle(arguments));
	}

	public void _updateResourceProvider(String scheme, ClassDefinition cd, Struct arguments) throws PageException {
		_updateResourceProvider(scheme, cd, toStringCSSStyle(arguments));
	}

	public void updateResourceProvider(String scheme, ClassDefinition cd, String arguments) throws PageException {
		checkWriteAccess();
		SecurityManager sm = config.getSecurityManager();
		short access = sm.getAccess(SecurityManager.TYPE_FILE);
		boolean hasAccess = access == SecurityManager.VALUE_YES;

		if (!hasAccess) throw new SecurityException("no access to update resources");
		_updateResourceProvider(scheme, cd, arguments);
	}

	public void _updateResourceProvider(String scheme, ClassDefinition cd, String arguments) throws PageException {

		// check parameters
		if (StringUtil.isEmpty(scheme)) throw new ExpressionException("scheme can't be an empty value");

		Element parent = _getRootElement("resources");

		// Update
		Element[] children = XMLConfigWebFactory.getChildren(parent, "resource-provider");
		for (int i = 0; i < children.length; i++) {
			// String cn=children[i].getAttribute("class");
			String elScheme = children[i].getAttribute("scheme");
			if (elScheme.equalsIgnoreCase(scheme)) {
				Element el = children[i];
				setClass(el, null, "", cd);
				el.setAttribute("scheme", scheme);
				el.setAttribute("arguments", arguments);
				return;
			}
		}

		// Insert
		Element el = doc.createElement("resource-provider");
		parent.appendChild(el);
		el.setAttribute("scheme", scheme);
		el.setAttribute("arguments", arguments);
		setClass(el, null, "", cd);
	}

	public void updateDefaultResourceProvider(ClassDefinition cd, String arguments) throws PageException {
		checkWriteAccess();
		SecurityManager sm = config.getSecurityManager();
		short access = sm.getAccess(SecurityManager.TYPE_FILE);
		boolean hasAccess = access == SecurityManager.VALUE_YES;

		if (!hasAccess) throw new SecurityException("no access to update resources");

		Element parent = _getRootElement("resources");

		// Update
		Element[] children = XMLConfigWebFactory.getChildren(parent, "default-resource-provider");
		for (int i = 0; i < children.length; i++) {
			Element el = children[i];
			el.setAttribute("arguments", arguments);
			return;
		}

		// Insert
		Element el = doc.createElement("default-resource-provider");
		parent.appendChild(el);
		el.setAttribute("arguments", arguments);
		setClass(el, null, "", cd);
	}

	private int getDatasourceLength(ConfigImpl config) {
		Map ds = config.getDataSourcesAsMap();
		Iterator it = ds.keySet().iterator();
		int len = 0;

		while (it.hasNext()) {
			if (!((DataSource) ds.get(it.next())).isReadOnly()) len++;
		}
		return len;
	}

	private static String toStringURLStyle(Struct sct) {
		if (sct == null) return "";
		Iterator<Entry<Key, Object>> it = sct.entryIterator();
		Entry<Key, Object> e;
		StringBuilder rtn = new StringBuilder();
		while (it.hasNext()) {
			e = it.next();
			if (rtn.length() > 0) rtn.append('&');
			rtn.append(URLEncoder.encode(e.getKey().getString()));
			rtn.append('=');
			rtn.append(URLEncoder.encode(Caster.toString(e.getValue(), "")));
		}
		return rtn.toString();
	}

	private static String toStringCSSStyle(Struct sct) {
		// Collection.Key[] keys = sct.keys();
		StringBuilder rtn = new StringBuilder();
		Iterator<Entry<Key, Object>> it = sct.entryIterator();
		Entry<Key, Object> e;

		while (it.hasNext()) {
			e = it.next();
			if (rtn.length() > 0) rtn.append(';');
			rtn.append(encode(e.getKey().getString()));
			rtn.append(':');
			rtn.append(encode(Caster.toString(e.getValue(), "")));
		}
		return rtn.toString();
	}

	private static String encode(String str) {
		try {
			return URLEncodedFormat.invoke(str, "UTF-8", false);
		}
		catch (PageException e) {
			return URLEncoder.encode(str);
		}
	}

	public Query getResourceProviders() throws PageException {
		checkReadAccess();
		// check parameters
		Element parent = _getRootElement("resources");
		Element[] elProviders = XMLConfigWebFactory.getChildren(parent, "resource-provider");
		Element[] elDefaultProviders = XMLConfigWebFactory.getChildren(parent, "default-resource-provider");
		ResourceProvider[] providers = config.getResourceProviders();
		ResourceProvider defaultProvider = config.getDefaultResourceProvider();

		Query qry = new QueryImpl(new String[] { "support", "scheme", "caseSensitive", "default", "class", "bundleName", "bundleVersion", "arguments" },
				elProviders.length + elDefaultProviders.length, "resourceproviders");
		int row = 1;
		for (int i = 0; i < elDefaultProviders.length; i++) {
			getResourceProviders(new ResourceProvider[] { defaultProvider }, qry, elDefaultProviders[i], row++, Boolean.TRUE);
		}
		for (int i = 0; i < elProviders.length; i++) {
			getResourceProviders(providers, qry, elProviders[i], row++, Boolean.FALSE);
		}
		return qry;
	}

	private void getResourceProviders(ResourceProvider[] providers, Query qry, Element p, int row, Boolean def) throws PageException {
		Array support = new ArrayImpl();
		String cn = p.getAttribute("class");
		String name = p.getAttribute("bundle-name");
		String version = p.getAttribute("bundle-version");
		ClassDefinition cd = new ClassDefinitionImpl(cn, name, version, ThreadLocalPageContext.getConfig().getIdentification());

		qry.setAt("scheme", row, p.getAttribute("scheme"));
		qry.setAt("arguments", row, p.getAttribute("arguments"));

		qry.setAt("class", row, cd.getClassName());
		qry.setAt("bundleName", row, cd.getName());
		qry.setAt("bundleVersion", row, cd.getVersionAsString());
		for (int i = 0; i < providers.length; i++) {
			if (providers[i].getClass().getName().equals(cd.getClassName())) {
				if (providers[i].isAttributesSupported()) support.append("attributes");
				if (providers[i].isModeSupported()) support.append("mode");
				qry.setAt("support", row, ListUtil.arrayToList(support, ","));
				qry.setAt("scheme", row, providers[i].getScheme());
				qry.setAt("caseSensitive", row, Caster.toBoolean(providers[i].isCaseSensitive()));
				qry.setAt("default", row, def);
				break;
			}
		}
	}

	public void removeJDBCDriver(String className) throws ExpressionException, SecurityException {
		checkWriteAccess();
		// check parameters
		if (StringUtil.isEmpty(className)) throw new ExpressionException("class name for jdbc driver cannot be empty");

		Element parent = _getRootElement("jdbc");

		Element[] children = XMLConfigWebFactory.getChildren(parent, "driver");
		for (int i = 0; i < children.length; i++) {
			String n = children[i].getAttribute("class");
			if (n != null && n.equalsIgnoreCase(className)) {
				parent.removeChild(children[i]);
			}
		}
	}

	/**
	 * remove a DataSource Connection
	 * 
	 * @param name
	 * @throws ExpressionException
	 * @throws SecurityException
	 */
	public void removeDataSource(String name) throws ExpressionException, SecurityException {
		checkWriteAccess();
		// check parameters
		if (name == null || name.length() == 0) throw new ExpressionException("name for Datasource Connection can be an empty value");

		Element datasources = _getRootElement("data-sources");

		Element[] children = XMLConfigWebFactory.getChildren(datasources, "data-source");
		for (int i = 0; i < children.length; i++) {
			String n = children[i].getAttribute("name");
			if (n != null && n.equalsIgnoreCase(name)) {
				datasources.removeChild(children[i]);
			}
		}
	}

	public void removeCacheConnection(String name) throws ExpressionException, SecurityException {
		checkWriteAccess();

		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_CACHE);
		if (!hasAccess) throw new SecurityException("no access to remove cache connection");

		// check parameters
		if (StringUtil.isEmpty(name)) throw new ExpressionException("name for Cache Connection can not be an empty value");

		Element parent = _getRootElement("cache");

		// remove default flag
		if (name.equalsIgnoreCase(parent.getAttribute("default-object"))) parent.removeAttribute("default-object");
		if (name.equalsIgnoreCase(parent.getAttribute("default-template"))) parent.removeAttribute("default-template");
		if (name.equalsIgnoreCase(parent.getAttribute("default-query"))) parent.removeAttribute("default-query");
		if (name.equalsIgnoreCase(parent.getAttribute("default-resource"))) parent.removeAttribute("default-resource");

		// remove element
		Element[] children = XMLConfigWebFactory.getChildren(parent, "connection");
		for (int i = 0; i < children.length; i++) {
			String n = children[i].getAttribute("name");
			if (n != null && n.equalsIgnoreCase(name)) {
				Map<String, CacheConnection> conns = config.getCacheConnections();
				CacheConnection cc = conns.get(n.toLowerCase());
				if (cc != null) {
					CacheUtil.releaseEL(cc);
					// CacheUtil.removeEL( config instanceof ConfigWeb ? (ConfigWeb) config : null, cc );
				}

				parent.removeChild(children[i]);
			}
		}

	}

	public boolean cacheConnectionExists(String name) throws ExpressionException, SecurityException {

		checkReadAccess();

		if (!ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_CACHE)) throw new SecurityException("no access to check cache connection");

		if (name == null || name.isEmpty()) throw new ExpressionException("name for Cache Connection can not be an empty value");

		Element parent = _getRootElement("cache");

		Element[] children = XMLConfigWebFactory.getChildren(parent, "connection");

		for (int i = 0; i < children.length; i++) {

			String n = children[i].getAttribute("name");

			if (n != null && n.equalsIgnoreCase(name)) return true;
		}

		return false;
	}

	public void removeGatewayEntry(String name) throws PageException {
		checkWriteAccess();

		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_GATEWAY);
		if (!hasAccess) throw new SecurityException("no access to remove gateway entry");

		_removeGatewayEntry(name);
		_removeAMFEngine();
	}

	protected void _removeGatewayEntry(String name) throws PageException {
		if (StringUtil.isEmpty(name)) throw new ExpressionException("name for Gateway Id can be an empty value");

		Element parent = _getRootElement("gateways");

		// remove element
		Element[] children = XMLConfigWebFactory.getChildren(parent, "gateway");
		for (int i = 0; i < children.length; i++) {
			String n = children[i].getAttribute("id");
			if (n != null && n.equalsIgnoreCase(name)) {

				if (config instanceof ConfigWeb) {
					_removeGatewayEntry((ConfigWebImpl) config, n);
				}
				else {
					ConfigWeb[] cws = ((ConfigServerImpl) config).getConfigWebs();
					for (ConfigWeb cw: cws) {
						_removeGatewayEntry((ConfigWebImpl) cw, name);
					}
				}
				parent.removeChild(children[i]);
			}
		}
	}

	private void _removeGatewayEntry(ConfigWebImpl cw, String name) {
		GatewayEngineImpl engine = cw.getGatewayEngine();
		Map<String, GatewayEntry> conns = engine.getEntries();
		GatewayEntry ge = conns.get(name);
		if (ge != null) {
			engine.remove(ge);
		}
	}

	public void removeRemoteClient(String url) throws ExpressionException, SecurityException {
		checkWriteAccess();

		// SNSN

		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_REMOTE);
		if (!hasAccess) throw new SecurityException("no access to remove remote client settings");

		// check parameters
		if (StringUtil.isEmpty(url)) throw new ExpressionException("url for Remote Client can be an empty value");

		Element clients = _getRootElement("remote-clients");

		Element[] children = XMLConfigWebFactory.getChildren(clients, "remote-client");
		for (int i = 0; i < children.length; i++) {
			String n = children[i].getAttribute("url");
			if (n != null && n.equalsIgnoreCase(url)) {
				clients.removeChild(children[i]);
			}
		}
	}

	/**
	 * update PSQ State
	 * 
	 * @param psq Preserver Single Quote
	 * @throws SecurityException
	 */
	public void updatePSQ(Boolean psq) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_DATASOURCE);

		if (!hasAccess) throw new SecurityException("no access to update datsource connections");

		Element datasources = _getRootElement("data-sources");
		datasources.setAttribute("psq", Caster.toString(psq, ""));
		if (datasources.hasAttribute("preserve-single-quote")) datasources.removeAttribute("preserve-single-quote");
	}

	public void updateInspectTemplate(String str) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update");

		Element datasources = _getRootElement("java");
		datasources.setAttribute("inspect-template", str);

	}

	public void updateTypeChecking(Boolean typeChecking) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update");

		Element datasources = _getRootElement("application");
		if (typeChecking == null) datasources.removeAttribute("type-checking");
		else datasources.setAttribute("type-checking", Caster.toString(typeChecking.booleanValue()));

	}

	public void updateCachedAfterTimeRange(TimeSpan ts) throws SecurityException, ApplicationException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update");

		Element el = _getRootElement("application");
		if (ts == null) el.removeAttribute("cached-after");
		else {
			if (ts.getMillis() < 0) throw new ApplicationException("value cannot be a negative number");
			el.setAttribute("cached-after", ts.getDay() + "," + ts.getHour() + "," + ts.getMinute() + "," + ts.getSecond());
		}
	}

	/**
	 * sets the scope cascading type
	 * 
	 * @param type (ServletConfigImpl.SCOPE_XYZ)
	 * @throws SecurityException
	 */
	public void updateScopeCascadingType(String type) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		Element scope = _getRootElement("scope");
		if (type.equalsIgnoreCase("strict")) scope.setAttribute("cascading", "strict");
		else if (type.equalsIgnoreCase("small")) scope.setAttribute("cascading", "small");
		else if (type.equalsIgnoreCase("standard")) scope.setAttribute("cascading", "standard");
		else scope.setAttribute("cascading", "standard");

	}

	/**
	 * sets the scope cascading type
	 * 
	 * @param type (ServletConfigImpl.SCOPE_XYZ)
	 * @throws SecurityException
	 */
	public void updateScopeCascadingType(short type) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		// lucee.print.ln("********........type:"+type);
		Element scope = _getRootElement("scope");
		if (type == ConfigWeb.SCOPE_STRICT) scope.setAttribute("cascading", "strict");
		else if (type == ConfigWeb.SCOPE_SMALL) scope.setAttribute("cascading", "small");
		else if (type == ConfigWeb.SCOPE_STANDARD) scope.setAttribute("cascading", "standard");

	}

	/**
	 * sets if allowed implicid query call
	 * 
	 * @param allow
	 * @throws SecurityException
	 */
	public void updateAllowImplicidQueryCall(Boolean allow) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		Element scope = _getRootElement("scope");
		scope.setAttribute("cascade-to-resultset", Caster.toString(allow, ""));

	}

	public void updateMergeFormAndUrl(Boolean merge) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		Element scope = _getRootElement("scope");
		scope.setAttribute("merge-url-form", Caster.toString(merge, ""));

	}

	/**
	 * updates request timeout value
	 * 
	 * @param span
	 * @throws SecurityException
	 * @throws ApplicationException
	 */
	public void updateRequestTimeout(TimeSpan span) throws SecurityException, ApplicationException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		Element scope = _getRootElement("scope");

		Element application = _getRootElement("application");
		if (span != null) {
			if (span.getMillis() <= 0) throw new ApplicationException("value must be a positive number");
			application.setAttribute("requesttimeout", span.getDay() + "," + span.getHour() + "," + span.getMinute() + "," + span.getSecond());
		}
		else application.removeAttribute("requesttimeout");

		// remove deprecated attribute
		if (scope.hasAttribute("requesttimeout")) scope.removeAttribute("requesttimeout");
	}

	/**
	 * updates session timeout value
	 * 
	 * @param span
	 * @throws SecurityException
	 */
	public void updateSessionTimeout(TimeSpan span) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		Element scope = _getRootElement("scope");
		if (span != null) scope.setAttribute("sessiontimeout", span.getDay() + "," + span.getHour() + "," + span.getMinute() + "," + span.getSecond());
		else scope.removeAttribute("sessiontimeout");
	}

	public void updateClientStorage(String storage) throws SecurityException, ApplicationException {
		updateStorage("client", storage);
	}

	public void updateSessionStorage(String storage) throws SecurityException, ApplicationException {
		updateStorage("session", storage);
	}

	private void updateStorage(String storageName, String storage) throws SecurityException, ApplicationException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");
		storage = validateStorage(storage);

		Element scope = _getRootElement("scope");
		if (!StringUtil.isEmpty(storage, true)) scope.setAttribute(storageName + "storage", storage);
		else scope.removeAttribute(storageName + "storage");
	}

	private String validateStorage(String storage) throws ApplicationException {
		storage = storage.trim().toLowerCase();

		// empty
		if (StringUtil.isEmpty(storage, true)) return "";

		// standard storages
		if ("cookie".equals(storage) || "memory".equals(storage) || "file".equals(storage)) return storage;

		// aliases
		if ("ram".equals(storage)) return "memory";
		if ("registry".equals(storage)) return "file";

		// datasource
		DataSource ds = config.getDataSource(storage, null);
		if (ds != null) {
			if (ds.isStorage()) return storage;
			throw new ApplicationException("datasource [" + storage + "] is not enabled to be used as session/client storage");
		}

		// cache
		CacheConnection cc = CacheUtil.getCacheConnection(ThreadLocalPageContext.get(config), storage, null);
		if (cc != null) {
			if (cc.isStorage()) return storage;
			throw new ApplicationException("cache [" + storage + "] is not enabled to be used as session/client storage");
		}

		String sdx = StringUtil.soundex(storage);

		// check if a datasource has a similar name
		DataSource[] sources = config.getDataSources();
		for (int i = 0; i < sources.length; i++) {
			if (StringUtil.soundex(sources[i].getName()).equals(sdx))
				throw new ApplicationException("no matching storage for [" + storage + "] found, did you mean [" + sources[i].getName() + "]");
		}

		// check if a cache has a similar name
		Iterator<String> it = config.getCacheConnections().keySet().iterator();
		String name;
		while (it.hasNext()) {
			name = it.next();
			if (StringUtil.soundex(name).equals(sdx)) throw new ApplicationException("no matching storage for [" + storage + "] found, did you mean [" + name + "]");
		}

		throw new ApplicationException("no matching storage for [" + storage + "] found");
	}

	/**
	 * updates session timeout value
	 * 
	 * @param span
	 * @throws SecurityException
	 */
	public void updateClientTimeout(TimeSpan span) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		Element scope = _getRootElement("scope");
		if (span != null) scope.setAttribute("clienttimeout", span.getDay() + "," + span.getHour() + "," + span.getMinute() + "," + span.getSecond());
		else scope.removeAttribute("clienttimeout");

		// deprecated
		if (scope.hasAttribute("client-max-age")) scope.removeAttribute("client-max-age");

	}

	public void updateCFMLWriterType(String writerType) throws SecurityException, ApplicationException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		Element scope = _getRootElement("setting");
		writerType = writerType.trim();

		// remove
		if (StringUtil.isEmpty(writerType)) {
			if (scope.hasAttribute("cfml-writer")) scope.removeAttribute("cfml-writer");
			return;
		}

		// update
		if (!"white-space".equalsIgnoreCase(writerType) && !"white-space-pref".equalsIgnoreCase(writerType) && !"regular".equalsIgnoreCase(writerType))
			throw new ApplicationException("invalid writer type definition [" + writerType + "], valid types are [white-space, white-space-pref, regular]");

		scope.setAttribute("cfml-writer", writerType.toLowerCase());
	}

	/*
	 * public void updateSuppressWhitespace(Boolean value) throws SecurityException {
	 * checkWriteAccess(); boolean
	 * hasAccess=ConfigWebUtil.hasAccess(config,SecurityManager.TYPE_SETTING);
	 * 
	 * if(!hasAccess) throw new SecurityException("no access to update scope setting");
	 * 
	 * Element scope=_getRootElement("setting");
	 * scope.setAttribute("suppress-whitespace",Caster.toString(value,"")); }
	 */

	public void updateSuppressContent(Boolean value) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		Element scope = _getRootElement("setting");
		scope.setAttribute("suppress-content", Caster.toString(value, ""));
	}

	public void updateShowVersion(Boolean value) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		Element scope = _getRootElement("setting");
		scope.setAttribute("show-version", Caster.toString(value, ""));
	}

	public void updateAllowCompression(Boolean value) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		Element scope = _getRootElement("setting");
		scope.setAttribute("allow-compression", Caster.toString(value, ""));
	}

	public void updateContentLength(Boolean value) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		Element scope = _getRootElement("setting");
		scope.setAttribute("content-length", Caster.toString(value, ""));
	}

	public void updateBufferOutput(Boolean value) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		Element scope = _getRootElement("setting");
		scope.setAttribute("buffering-output", Caster.toString(value, ""));
		if (scope.hasAttribute("buffer-output")) scope.removeAttribute("buffer-output");
	}

	/**
	 * updates request timeout value
	 * 
	 * @param span
	 * @throws SecurityException
	 */
	public void updateApplicationTimeout(TimeSpan span) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		Element scope = _getRootElement("scope");
		if (span != null) scope.setAttribute("applicationtimeout", span.getDay() + "," + span.getHour() + "," + span.getMinute() + "," + span.getSecond());
		else scope.removeAttribute("applicationtimeout");
	}

	public void updateApplicationListener(String type, String mode) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update listener type");

		Element scope = _getRootElement("application");
		scope.setAttribute("listener-type", type.toLowerCase().trim());
		scope.setAttribute("listener-mode", mode.toLowerCase().trim());
	}

	public void updateCachedWithin(int type, Object value) throws SecurityException, ApplicationException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update cachedwithin setting");

		String t = AppListenerUtil.toCachedWithinType(type, "");
		if (t == null) throw new ApplicationException("invalid cachedwithin type definition");
		String v = Caster.toString(value, null);
		Element app = _getRootElement("application");
		if (v != null) app.setAttribute("cached-within-" + t, v);
		else app.removeAttribute("cached-within-" + t);
	}

	public void updateProxy(boolean enabled, String server, int port, String username, String password) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update listener type");

		Element proxy = _getRootElement("proxy");
		proxy.setAttribute("enabled", Caster.toString(enabled));
		if (!StringUtil.isEmpty(server)) proxy.setAttribute("server", server);
		if (port > 0) proxy.setAttribute("port", Caster.toString(port));
		if (!StringUtil.isEmpty(username)) proxy.setAttribute("username", username);
		if (!StringUtil.isEmpty(password)) proxy.setAttribute("password", password);
	}

	/*
	 * public void removeProxy() throws SecurityException { boolean
	 * hasAccess=ConfigWebUtil.hasAccess(config,SecurityManager.TYPE_SETTING); if(!hasAccess) throw new
	 * SecurityException("no access to remove proxy settings");
	 * 
	 * Element proxy=_getRootElement("proxy"); proxy.removeAttribute("server");
	 * proxy.removeAttribute("port"); proxy.removeAttribute("username");
	 * proxy.removeAttribute("password"); }
	 */

	/**
	 * enable or desable session management
	 * 
	 * @param sessionManagement
	 * @throws SecurityException
	 */
	public void updateSessionManagement(Boolean sessionManagement) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		Element scope = _getRootElement("scope");
		scope.setAttribute("sessionmanagement", Caster.toString(sessionManagement, ""));
	}

	/**
	 * enable or desable client management
	 * 
	 * @param clientManagement
	 * @throws SecurityException
	 */
	public void updateClientManagement(Boolean clientManagement) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		Element scope = _getRootElement("scope");
		scope.setAttribute("clientmanagement", Caster.toString(clientManagement, ""));
	}

	/**
	 * set if client cookies are enabled or not
	 * 
	 * @param clientCookies
	 * @throws SecurityException
	 */
	public void updateClientCookies(Boolean clientCookies) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		Element scope = _getRootElement("scope");
		scope.setAttribute("setclientcookies", Caster.toString(clientCookies, ""));
	}

	/**
	 * set if it's develop mode or not
	 * 
	 * @param developmode
	 * @throws SecurityException
	 */
	public void updateMode(Boolean developmode) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		Element mode = _getRootElement("mode");
		mode.setAttribute("develop", Caster.toString(developmode, ""));
	}

	/**
	 * set if domain cookies are enabled or not
	 * 
	 * @param domainCookies
	 * @throws SecurityException
	 */
	public void updateDomaincookies(Boolean domainCookies) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		Element scope = _getRootElement("scope");
		scope.setAttribute("setdomaincookies", Caster.toString(domainCookies, ""));
	}

	/**
	 * update the locale
	 * 
	 * @param locale
	 * @throws SecurityException
	 */
	public void updateLocale(String locale) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update regional setting");

		Element scope = _getRootElement("regional");
		scope.setAttribute("locale", locale.trim());
	}

	public void updateMonitorEnabled(boolean updateMonitorEnabled) throws SecurityException {
		checkWriteAccess();
		_updateMonitorEnabled(updateMonitorEnabled);
	}

	void _updateMonitorEnabled(boolean updateMonitorEnabled) {
		Element scope = _getRootElement("monitoring");
		scope.setAttribute("enabled", Caster.toString(updateMonitorEnabled));
	}

	public void updateScriptProtect(String strScriptProtect) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update script protect");

		Element scope = _getRootElement("application");
		scope.setAttribute("script-protect", strScriptProtect.trim());
	}

	public void updateAllowURLRequestTimeout(Boolean allowURLRequestTimeout) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update AllowURLRequestTimeout");

		Element scope = _getRootElement("application");
		scope.setAttribute("allow-url-requesttimeout", Caster.toString(allowURLRequestTimeout, ""));
	}

	public void updateScriptProtect(int scriptProtect) throws SecurityException {
		updateScriptProtect(AppListenerUtil.translateScriptProtect(scriptProtect));
	}

	/**
	 * update the timeZone
	 * 
	 * @param timeZone
	 * @throws SecurityException
	 */
	public void updateTimeZone(String timeZone) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update regional setting");

		Element regional = _getRootElement("regional");
		regional.setAttribute("timezone", timeZone.trim());

	}

	/**
	 * update the timeServer
	 * 
	 * @param timeServer
	 * @param useTimeServer
	 * @throws PageException
	 */
	public void updateTimeServer(String timeServer, Boolean useTimeServer) throws PageException {
		checkWriteAccess();
		if (useTimeServer != null && useTimeServer.booleanValue() && !StringUtil.isEmpty(timeServer, true)) {
			try {
				new NtpClient(timeServer).getOffset();
			}
			catch (IOException e) {
				try {
					new NtpClient(timeServer).getOffset();
				}
				catch (IOException ee) {
					throw Caster.toPageException(ee);
				}
			}
		}

		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update regional setting");

		Element scope = _getRootElement("regional");
		scope.setAttribute("timeserver", timeServer.trim());
		if (useTimeServer != null) scope.setAttribute("use-timeserver", Caster.toString(useTimeServer));
		else scope.removeAttribute("use-timeserver");
	}

	/**
	 * update the baseComponent
	 * 
	 * @param baseComponent
	 * @throws SecurityException
	 */
	public void updateBaseComponent(String baseComponentCFML, String baseComponentLucee) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update component setting");
		// config.resetBaseComponentPage();
		Element scope = _getRootElement("component");
		// if(baseComponent.trim().length()>0)
		scope.removeAttribute("base");
		scope.setAttribute("base-cfml", baseComponentCFML);
		scope.setAttribute("base-lucee", baseComponentLucee);
	}

	public void updateComponentDeepSearch(Boolean deepSearch) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update component setting");
		// config.resetBaseComponentPage();
		Element scope = _getRootElement("component");
		// if(baseComponent.trim().length()>0)
		if (deepSearch != null) scope.setAttribute("deep-search", Caster.toString(deepSearch.booleanValue()));

		else {
			if (scope.hasAttribute("deep-search")) scope.removeAttribute("deep-search");
		}

	}

	public void updateComponentDefaultImport(String componentDefaultImport) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update component setting");
		// config.resetBaseComponentPage();
		Element scope = _getRootElement("component");
		// if(baseComponent.trim().length()>0)
		scope.setAttribute("component-default-import", componentDefaultImport);
	}

	/**
	 * update the Component Data Member default access type
	 * 
	 * @param strAccess
	 * @throws SecurityException
	 * @throws ExpressionException
	 */
	public void updateComponentDataMemberDefaultAccess(String strAccess) throws SecurityException, ApplicationException {
		checkWriteAccess();

		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update component setting");

		Element scope = _getRootElement("component");

		if (StringUtil.isEmpty(strAccess)) {
			scope.setAttribute("data-member-default-access", "");
		}
		else {
			scope.setAttribute("data-member-default-access", ComponentUtil.toStringAccess(ComponentUtil.toIntAccess(strAccess)));
		}
	}

	/**
	 * update the Component Data Member default access type
	 * 
	 * @param triggerDataMember
	 * @throws SecurityException
	 */
	public void updateTriggerDataMember(Boolean triggerDataMember) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update trigger-data-member");

		Element scope = _getRootElement("component");
		scope.setAttribute("trigger-data-member", Caster.toString(triggerDataMember, ""));
	}

	public void updateComponentUseShadow(Boolean useShadow) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update use-shadow");

		Element scope = _getRootElement("component");
		scope.setAttribute("use-shadow", Caster.toString(useShadow, ""));
	}

	public void updateComponentLocalSearch(Boolean componentLocalSearch) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update component Local Search");

		Element scope = _getRootElement("component");
		scope.setAttribute("local-search", Caster.toString(componentLocalSearch, ""));
	}

	public void updateComponentPathCache(Boolean componentPathCache) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update component Cache Path");

		Element scope = _getRootElement("component");
		if (!Caster.toBooleanValue(componentPathCache, false)) config.clearComponentCache();
		scope.setAttribute("use-cache-path", Caster.toString(componentPathCache, ""));
	}

	public void updateCTPathCache(Boolean ctPathCache) throws SecurityException {
		checkWriteAccess();
		if (!ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_CUSTOM_TAG)) throw new SecurityException("no access to update custom tag setting");

		if (!Caster.toBooleanValue(ctPathCache, false)) config.clearCTCache();
		Element scope = _getRootElement("custom-tag");
		scope.setAttribute("use-cache-path", Caster.toString(ctPathCache, ""));
	}

	public void updateSecurity(String varUsage) throws SecurityException {
		checkWriteAccess();
		Element el = _getRootElement("security");

		if (el != null) {
			if (!StringUtil.isEmpty(varUsage)) el.setAttribute("variable-usage", Caster.toString(varUsage));
			else el.removeAttribute("variable-usage");
		}

	}

	/**
	 * updates if debugging or not
	 * 
	 * @param debug if value is null server setting is used
	 * @throws SecurityException
	 */
	public void updateDebug(Boolean debug, Boolean template, Boolean database, Boolean exception, Boolean tracing, Boolean dump, Boolean timer, Boolean implicitAccess,
			Boolean queryUsage) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_DEBUGGING);
		if (!hasAccess) throw new SecurityException("no access to change debugging settings");
		Element debugging = _getRootElement("debugging");

		if (debug != null) debugging.setAttribute("debug", Caster.toString(debug.booleanValue()));
		else debugging.removeAttribute("debug");

		if (database != null) debugging.setAttribute("database", Caster.toString(database.booleanValue()));
		else debugging.removeAttribute("database");

		if (template != null) debugging.setAttribute("templenabled", Caster.toString(template.booleanValue()));
		else debugging.removeAttribute("templenabled");

		if (exception != null) debugging.setAttribute("exception", Caster.toString(exception.booleanValue()));
		else debugging.removeAttribute("exception");

		if (tracing != null) debugging.setAttribute("tracing", Caster.toString(tracing.booleanValue()));
		else debugging.removeAttribute("tracing");

		if (dump != null) debugging.setAttribute("dump", Caster.toString(dump.booleanValue()));
		else debugging.removeAttribute("dump");

		if (timer != null) debugging.setAttribute("timer", Caster.toString(timer.booleanValue()));
		else debugging.removeAttribute("timer");

		if (implicitAccess != null) debugging.setAttribute("implicit-access", Caster.toString(implicitAccess.booleanValue()));
		else debugging.removeAttribute("implicit-access");

		if (queryUsage != null) debugging.setAttribute("query-usage", Caster.toString(queryUsage.booleanValue()));
		else debugging.removeAttribute("query-usage");

	}

	/**
	 * updates the DebugTemplate
	 * 
	 * @param template
	 * @throws SecurityException
	 */
	public void updateDebugTemplate(String template) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to change debugging settings");

		Element debugging = _getRootElement("debugging");
		// if(template.trim().length()>0)
		debugging.setAttribute("template", template);
	}

	/**
	 * updates the ErrorTemplate
	 * 
	 * @param template
	 * @throws SecurityException
	 */
	public void updateErrorTemplate(int statusCode, String template) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to change error settings");

		Element error = _getRootElement("error");
		// if(template.trim().length()>0)
		error.setAttribute("template-" + statusCode, template);
	}

	public void updateErrorStatusCode(Boolean doStatusCode) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to change error settings");

		Element error = _getRootElement("error");
		error.setAttribute("status-code", Caster.toString(doStatusCode, ""));
	}

	public void updateRegexType(String type) throws PageException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to change regex settings");

		Element regex = _getRootElement("regex");
		if (StringUtil.isEmpty(type)) regex.removeAttribute("type");
		else regex.setAttribute("type", RegexFactory.toType(RegexFactory.toType(type), "perl"));
	}

	/**
	 * updates the DebugTemplate
	 * 
	 * @param template
	 * @throws SecurityException
	 */
	public void updateComponentDumpTemplate(String template) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update component setting");

		Element component = _getRootElement("component");
		// if(template.trim().length()>0)
		component.setAttribute("dump-template", template);
	}

	/*
	 * * updates the if memory usage will be logged or not
	 * 
	 * @param logMemoryUsage
	 * 
	 * @throws SecurityException / public void updateLogMemoryUsage(boolean logMemoryUsage) throws
	 * SecurityException { boolean
	 * hasAccess=ConfigWebUtil.hasAccess(config,SecurityManager.TYPE_DEBUGGING); if(!hasAccess) throw
	 * new SecurityException("no access to change debugging settings");
	 * 
	 * Element debugging=_getRootElement("debugging");
	 * debugging.setAttribute("log-memory-usage",Caster.toString(logMemoryUsage)); }
	 */

	/*
	 * * updates the Memory Logger
	 * 
	 * @param memoryLogger
	 * 
	 * @throws SecurityException / public void updateMemoryLogger(String memoryLogger) throws
	 * SecurityException { boolean
	 * hasAccess=ConfigWebUtil.hasAccess(config,SecurityManager.TYPE_DEBUGGING); if(!hasAccess) throw
	 * new SecurityException("no access to change debugging settings");
	 * 
	 * Element debugging=_getRootElement("debugging");
	 * if(memoryLogger.trim().length()>0)debugging.setAttribute("memory-log",memoryLogger); }
	 */

	private Element _getRootElement(String name) {
		Element el = XMLConfigWebFactory.getChildByName(doc.getDocumentElement(), name);
		if (el == null) {
			el = doc.createElement(name);
			doc.getDocumentElement().appendChild(el);
		}
		return el;
	}

	private Element _getRootElement(String name, boolean insertBefore, boolean doNotCreate) {
		return XMLConfigWebFactory.getChildByName(doc.getDocumentElement(), name, insertBefore, doNotCreate);
	}

	/**
	 * @param setting
	 * @param file
	 * @param directJavaAccess
	 * @param mail
	 * @param datasource
	 * @param mapping
	 * @param customTag
	 * @param cfxSetting
	 * @param cfxUsage
	 * @param debugging
	 * @param search
	 * @param scheduledTasks
	 * @param tagExecute
	 * @param tagImport
	 * @param tagObject
	 * @param tagRegistry
	 * @throws SecurityException
	 */
	public void updateDefaultSecurity(short setting, short file, Resource[] fileAccess, short directJavaAccess, short mail, short datasource, short mapping, short remote,
			short customTag, short cfxSetting, short cfxUsage, short debugging, short search, short scheduledTasks, short tagExecute, short tagImport, short tagObject,
			short tagRegistry, short cache, short gateway, short orm, short accessRead, short accessWrite) throws SecurityException {
		checkWriteAccess();
		if (!(config instanceof ConfigServer)) throw new SecurityException("can't change security settings from this context");

		Element security = _getRootElement("security");
		updateSecurityFileAccess(security, fileAccess, file);
		security.setAttribute("setting", SecurityManagerImpl.toStringAccessValue(setting));
		security.setAttribute("file", SecurityManagerImpl.toStringAccessValue(file));
		security.setAttribute("direct_java_access", SecurityManagerImpl.toStringAccessValue(directJavaAccess));
		security.setAttribute("mail", SecurityManagerImpl.toStringAccessValue(mail));
		security.setAttribute("datasource", SecurityManagerImpl.toStringAccessValue(datasource));
		security.setAttribute("mapping", SecurityManagerImpl.toStringAccessValue(mapping));
		security.setAttribute("remote", SecurityManagerImpl.toStringAccessValue(remote));
		security.setAttribute("custom_tag", SecurityManagerImpl.toStringAccessValue(customTag));
		security.setAttribute("cfx_setting", SecurityManagerImpl.toStringAccessValue(cfxSetting));
		security.setAttribute("cfx_usage", SecurityManagerImpl.toStringAccessValue(cfxUsage));
		security.setAttribute("debugging", SecurityManagerImpl.toStringAccessValue(debugging));
		security.setAttribute("search", SecurityManagerImpl.toStringAccessValue(search));
		security.setAttribute("scheduled_task", SecurityManagerImpl.toStringAccessValue(scheduledTasks));

		security.setAttribute("tag_execute", SecurityManagerImpl.toStringAccessValue(tagExecute));
		security.setAttribute("tag_import", SecurityManagerImpl.toStringAccessValue(tagImport));
		security.setAttribute("tag_object", SecurityManagerImpl.toStringAccessValue(tagObject));
		security.setAttribute("tag_registry", SecurityManagerImpl.toStringAccessValue(tagRegistry));
		security.setAttribute("cache", SecurityManagerImpl.toStringAccessValue(cache));
		security.setAttribute("gateway", SecurityManagerImpl.toStringAccessValue(gateway));
		security.setAttribute("orm", SecurityManagerImpl.toStringAccessValue(orm));

		security.setAttribute("access_read", SecurityManagerImpl.toStringAccessRWValue(accessRead));
		security.setAttribute("access_write", SecurityManagerImpl.toStringAccessRWValue(accessWrite));

	}

	private void removeSecurityFileAccess(Element parent) {
		Element[] children = XMLConfigWebFactory.getChildren(parent, "file-access");

		// remove existing
		if (!ArrayUtil.isEmpty(children)) {
			for (int i = children.length - 1; i >= 0; i--) {
				parent.removeChild(children[i]);
			}
		}
	}

	private void updateSecurityFileAccess(Element parent, Resource[] fileAccess, short file) {
		removeSecurityFileAccess(parent);

		// insert
		if (!ArrayUtil.isEmpty(fileAccess) && file != SecurityManager.VALUE_ALL) {
			Element fa;
			for (int i = 0; i < fileAccess.length; i++) {
				fa = doc.createElement("file-access");
				fa.setAttribute("path", fileAccess[i].getAbsolutePath());
				parent.appendChild(fa);
			}
		}

	}

	/**
	 * update a security manager that match the given id
	 * 
	 * @param id
	 * @param setting
	 * @param file
	 * @param fileAccess
	 * @param directJavaAccess
	 * @param mail
	 * @param datasource
	 * @param mapping
	 * @param customTag
	 * @param cfxSetting
	 * @param cfxUsage
	 * @param debugging
	 * @param search
	 * @param scheduledTasks
	 * @param tagExecute
	 * @param tagImport
	 * @param tagObject
	 * @param tagRegistry
	 * @throws SecurityException
	 * @throws ApplicationException
	 */
	public void updateSecurity(String id, short setting, short file, Resource[] fileAccess, short directJavaAccess, short mail, short datasource, short mapping, short remote,
			short customTag, short cfxSetting, short cfxUsage, short debugging, short search, short scheduledTasks, short tagExecute, short tagImport, short tagObject,
			short tagRegistry, short cache, short gateway, short orm, short accessRead, short accessWrite) throws SecurityException, ApplicationException {
		checkWriteAccess();
		if (!(config instanceof ConfigServer)) throw new SecurityException("can't change security settings from this context");

		Element security = _getRootElement("security");
		Element[] children = XMLConfigWebFactory.getChildren(security, "accessor");
		Element accessor = null;
		for (int i = 0; i < children.length; i++) {
			if (id.equals(children[i].getAttribute("id"))) {
				accessor = children[i];
			}
		}
		if (accessor == null) throw new ApplicationException("there is noc Security Manager for id [" + id + "]");
		updateSecurityFileAccess(accessor, fileAccess, file);

		accessor.setAttribute("setting", SecurityManagerImpl.toStringAccessValue(setting));
		accessor.setAttribute("file", SecurityManagerImpl.toStringAccessValue(file));
		accessor.setAttribute("direct_java_access", SecurityManagerImpl.toStringAccessValue(directJavaAccess));
		accessor.setAttribute("mail", SecurityManagerImpl.toStringAccessValue(mail));
		accessor.setAttribute("datasource", SecurityManagerImpl.toStringAccessValue(datasource));
		accessor.setAttribute("mapping", SecurityManagerImpl.toStringAccessValue(mapping));
		accessor.setAttribute("remote", SecurityManagerImpl.toStringAccessValue(remote));
		accessor.setAttribute("custom_tag", SecurityManagerImpl.toStringAccessValue(customTag));
		accessor.setAttribute("cfx_setting", SecurityManagerImpl.toStringAccessValue(cfxSetting));
		accessor.setAttribute("cfx_usage", SecurityManagerImpl.toStringAccessValue(cfxUsage));
		accessor.setAttribute("debugging", SecurityManagerImpl.toStringAccessValue(debugging));
		accessor.setAttribute("search", SecurityManagerImpl.toStringAccessValue(search));
		accessor.setAttribute("scheduled_task", SecurityManagerImpl.toStringAccessValue(scheduledTasks));
		accessor.setAttribute("cache", SecurityManagerImpl.toStringAccessValue(cache));
		accessor.setAttribute("gateway", SecurityManagerImpl.toStringAccessValue(gateway));
		accessor.setAttribute("orm", SecurityManagerImpl.toStringAccessValue(orm));

		accessor.setAttribute("tag_execute", SecurityManagerImpl.toStringAccessValue(tagExecute));
		accessor.setAttribute("tag_import", SecurityManagerImpl.toStringAccessValue(tagImport));
		accessor.setAttribute("tag_object", SecurityManagerImpl.toStringAccessValue(tagObject));
		accessor.setAttribute("tag_registry", SecurityManagerImpl.toStringAccessValue(tagRegistry));

		accessor.setAttribute("access_read", SecurityManagerImpl.toStringAccessRWValue(accessRead));
		accessor.setAttribute("access_write", SecurityManagerImpl.toStringAccessRWValue(accessWrite));
	}

	/**
	 * @return returns the default password
	 * @throws SecurityException
	 */
	public Password getDefaultPassword() throws SecurityException {
		checkReadAccess();
		if (config instanceof ConfigServerImpl) {
			return ((ConfigServerImpl) config).getDefaultPassword();
		}
		throw new SecurityException("can't access default password within this context");
	}

	/**
	 * @param password
	 * @throws SecurityException
	 * @throws IOException
	 * @throws DOMException
	 */
	public void updateDefaultPassword(String password) throws SecurityException, DOMException, IOException {
		checkWriteAccess();
		((ConfigServerImpl) config).setDefaultPassword(PasswordImpl.writeToXML(doc.getDocumentElement(), password, true));
	}

	public void removeDefaultPassword() throws SecurityException {
		checkWriteAccess();
		Element root = doc.getDocumentElement();
		PasswordImpl.removeFromXML(root, true);
		((ConfigServerImpl) config).setDefaultPassword(null);
	}

	/**
	 * session type update
	 * 
	 * @param type
	 * @throws SecurityException
	 */
	public void updateSessionType(String type) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		type = type.toLowerCase().trim();

		Element scope = _getRootElement("scope");
		scope.setAttribute("session-type", type);
	}

	public void updateLocalMode(String mode) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		mode = mode.toLowerCase().trim();
		Element scope = _getRootElement("scope");
		scope.setAttribute("local-mode", mode);
	}

	public void updateRestList(Boolean list) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = true;// TODO ConfigWebUtil.hasAccess(config,SecurityManager.TYPE_REST);
		if (!hasAccess) throw new SecurityException("no access to update rest setting");

		Element rest = _getRootElement("rest");
		if (list == null) {
			if (rest.hasAttribute("list")) rest.removeAttribute("list");
		}
		else rest.setAttribute("list", Caster.toString(list.booleanValue()));
	}

	/*
	 * public void updateRestAllowChanges(Boolean allowChanges) throws SecurityException {
	 * checkWriteAccess(); boolean hasAccess=true;// TODO
	 * ConfigWebUtil.hasAccess(config,SecurityManager.TYPE_REST); if(!hasAccess) throw new
	 * SecurityException("no access to update rest setting");
	 * 
	 * 
	 * Element rest=_getRootElement("rest"); if(allowChanges==null) {
	 * if(rest.hasAttribute("allow-changes"))rest.removeAttribute("allow-changes"); } else
	 * rest.setAttribute("allow-changes", Caster.toString(allowChanges.booleanValue())); }
	 */

	/**
	 * updates update settingd for Lucee
	 * 
	 * @param type
	 * @param location
	 * @throws SecurityException
	 */
	public void updateUpdate(String type, String location) throws SecurityException {
		checkWriteAccess();

		if (!(config instanceof ConfigServer)) {
			throw new SecurityException("can't change update setting from this context, access is denied");
		}
		Element update = _getRootElement("update");
		update.setAttribute("type", type);
		try {
			location = HTTPUtil.toURL(location, HTTPUtil.ENCODED_AUTO).toString();
		}
		catch (Throwable e) {
			ExceptionUtil.rethrowIfNecessary(e);
		}
		update.setAttribute("location", location);
	}

	/**
	 * creates an individual security manager based on the default security manager
	 * 
	 * @param id
	 * @throws DOMException
	 * @throws PageException
	 */
	public void createSecurityManager(Password password, String id) throws DOMException, PageException {
		checkWriteAccess();
		ConfigServerImpl cs = (ConfigServerImpl) ConfigImpl.getConfigServer(config, password);
		SecurityManagerImpl dsm = (SecurityManagerImpl) cs.getDefaultSecurityManager().cloneSecurityManager();
		cs.setSecurityManager(id, dsm);

		Element security = _getRootElement("security");
		Element accessor = null;

		Element[] children = XMLConfigWebFactory.getChildren(security, "accessor");
		for (int i = 0; i < children.length; i++) {
			if (id.equals(children[i].getAttribute("id"))) {
				accessor = children[i];
			}
		}
		if (accessor == null) {
			accessor = doc.createElement("accessor");
			security.appendChild(accessor);
		}

		updateSecurityFileAccess(accessor, dsm.getCustomFileAccess(), dsm.getAccess(SecurityManager.TYPE_FILE));

		accessor.setAttribute("id", id);
		accessor.setAttribute("setting", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_SETTING)));
		accessor.setAttribute("file", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_FILE)));
		accessor.setAttribute("direct_java_access", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS)));
		accessor.setAttribute("mail", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_MAIL)));
		accessor.setAttribute("datasource", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_DATASOURCE)));
		accessor.setAttribute("mapping", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_MAPPING)));
		accessor.setAttribute("custom_tag", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_CUSTOM_TAG)));
		accessor.setAttribute("cfx_setting", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_CFX_SETTING)));
		accessor.setAttribute("cfx_usage", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_CFX_USAGE)));
		accessor.setAttribute("debugging", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_DEBUGGING)));
		accessor.setAttribute("cache", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManagerImpl.TYPE_CACHE)));
		accessor.setAttribute("gateway", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManagerImpl.TYPE_GATEWAY)));
		accessor.setAttribute("orm", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManagerImpl.TYPE_ORM)));

		accessor.setAttribute("tag_execute", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_TAG_EXECUTE)));
		accessor.setAttribute("tag_import", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_TAG_IMPORT)));
		accessor.setAttribute("tag_object", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_TAG_OBJECT)));
		accessor.setAttribute("tag_registry", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_TAG_REGISTRY)));

	}

	/**
	 * remove security manager matching given id
	 * 
	 * @param id
	 * @throws PageException
	 */
	public void removeSecurityManager(Password password, String id) throws PageException {
		checkWriteAccess();
		((ConfigServerImpl) ConfigImpl.getConfigServer(config, password)).removeSecurityManager(id);

		Element security = _getRootElement("security");

		Element[] children = XMLConfigWebFactory.getChildren(security, "accessor");
		for (int i = 0; i < children.length; i++) {
			if (id.equals(children[i].getAttribute("id"))) {
				security.removeChild(children[i]);
			}
		}
	}

	/**
	 * run update from cfml engine
	 * 
	 * @throws PageException
	 */
	public void runUpdate(Password password) throws PageException {
		checkWriteAccess();
		ConfigServerImpl cs = (ConfigServerImpl) ConfigImpl.getConfigServer(config, password);
		CFMLEngineFactory factory = cs.getCFMLEngine().getCFMLEngineFactory();

		synchronized (factory) {
			try {
				cleanUp(factory);
				factory.update(cs.getPassword(), cs.getIdentification());
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
		}

	}

	/**
	 * run update from cfml engine
	 * 
	 * @throws PageException
	 */
	public void removeLatestUpdate(Password password) throws PageException {
		_removeUpdate(password, true);
	}

	public void removeUpdate(Password password) throws PageException {
		_removeUpdate(password, false);
	}

	private void _removeUpdate(Password password, boolean onlyLatest) throws PageException {
		checkWriteAccess();

		ConfigServerImpl cs = (ConfigServerImpl) ConfigImpl.getConfigServer(config, password);

		try {
			CFMLEngineFactory factory = cs.getCFMLEngine().getCFMLEngineFactory();

			if (onlyLatest) {
				factory.removeLatestUpdate(cs.getPassword());
			}
			else factory.removeUpdate(cs.getPassword());

		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	public void changeVersionTo(Version version, Password password, IdentificationWeb id) throws PageException {
		checkWriteAccess();
		ConfigServerImpl cs = (ConfigServerImpl) ConfigImpl.getConfigServer(config, password);

		Log logger = cs.getLog("deploy");

		try {
			CFMLEngineFactory factory = cs.getCFMLEngine().getCFMLEngineFactory();
			cleanUp(factory);
			// do we have the core file?
			final File patchDir = factory.getPatchDirectory();
			File localPath = new File(version.toString() + ".lco");

			if (!localPath.isFile()) {
				localPath = null;
				Version v;
				final File[] patches = patchDir.listFiles(new ExtensionFilter(new String[] { ".lco" }));
				for (final File patch: patches) {
					v = CFMLEngineFactory.toVersion(patch.getName(), null);
					// not a valid file get deleted
					if (v == null) {
						patch.delete();
					}
					else {
						if (v.equals(version)) { // match!
							localPath = patch;
						}
						// delete newer files
						else if (OSGiUtil.isNewerThan(v, version)) {
							patch.delete();
						}
					}
				}
			}

			// download patch
			if (localPath == null) {

				downloadCore(factory, version, id);
			}

			logger.log(Log.LEVEL_INFO, "Update-Engine", "Installing Lucee version [" + version + "] (previous version was [" + cs.getEngine().getInfo().getVersion() + "])");

			factory.restart(password);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private void cleanUp(CFMLEngineFactory factory) throws IOException {
		final File patchDir = factory.getPatchDirectory();
		final File[] patches = patchDir.listFiles(new ExtensionFilter(new String[] { ".lco" }));
		for (final File patch: patches) {
			if (!IsZipFile.invoke(patch)) patch.delete();
		}
	}

	private File downloadCore(CFMLEngineFactory factory, Version version, Identification id) throws IOException {
		final URL updateProvider = factory.getUpdateLocation();

		final URL updateUrl = new URL(updateProvider,
				"/rest/update/provider/download/" + version.toString() + (id != null ? id.toQueryString() : "") + (id == null ? "?" : "&") + "allowRedirect=true");
		// log.debug("Admin", "download "+version+" from " + updateUrl);
		// System. out.println(updateUrl);

		// local resource
		final File patchDir = factory.getPatchDirectory();
		final File newLucee = new File(patchDir, version + (".lco"));

		int code;
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) updateUrl.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();
			code = conn.getResponseCode();
		}
		catch (final UnknownHostException e) {
			// log.error("Admin", e);
			throw e;
		}

		// the update provider is not providing a download for this
		if (code != 200) {

			int count = 0;
			final int max = 5;
			// the update provider can also provide a different (final) location for this
			while ((code == 301 || code == 302) && (count++ < max)) {
				String location = conn.getHeaderField("Location");
				// just in case we check invalid names
				if (location == null) location = conn.getHeaderField("location");
				if (location == null) location = conn.getHeaderField("LOCATION");
				if (location == null) break;
				// System. out.println("download redirected:" + location); // MUST remove

				conn.disconnect();
				URL url = new URL(location);
				try {
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.connect();
					code = conn.getResponseCode();
				}
				catch (final UnknownHostException e) {
					// log.error("Admin", e);
					throw e;
				}
			}

			// no download available!
			if (code != 200) {
				final String msg = "Lucee Core download failed (response status:" + code + ") the core for version [" + version.toString() + "] from " + updateUrl
						+ ", please download it manually and copy to [" + patchDir + "]";
				// log.debug("Admin", msg);
				conn.disconnect();
				throw new IOException(msg);
			}
		}

		// copy it to local directory
		if (newLucee.createNewFile()) {
			IOUtil.copy((InputStream) conn.getContent(), new FileOutputStream(newLucee), false, true);
			conn.disconnect();

			// when it is a loader extract the core from it
			File tmp = CFMLEngineFactory.extractCoreIfLoader(newLucee);
			if (tmp != null) {
				// System .out.println("extract core from loader"); // MUST remove
				// log.debug("Admin", "extract core from loader");

				newLucee.delete();
				tmp.renameTo(newLucee);
				tmp.delete();
				// System. out.println("exist?" + newLucee.exists()); // MUST remove

			}
		}
		else {
			conn.disconnect();
			// log.debug("Admin","File for new Version already exists, won't copy new one");
			return null;
		}
		return newLucee;
	}

	private String getCoreExtension() {
		return "lco";
	}

	private boolean isNewerThan(int left, int right) {
		return left > right;
	}

	/*
	 * private Resource getPatchDirectory(CFMLEngine engine) throws IOException { //File
	 * f=engine.getCFMLEngineFactory().getResourceRoot(); Resource res =
	 * ResourcesImpl.getFileResourceProvider().getResource(engine.getCFMLEngineFactory().getResourceRoot
	 * ().getAbsolutePath()); Resource pd = res.getRealResource("patches"); if(!pd.exists())pd.mkdirs();
	 * return pd; }
	 */

	/**
	 * run update from cfml engine
	 * 
	 * @throws PageException
	 */
	public void restart(Password password) throws PageException {
		checkWriteAccess();
		ConfigServerImpl cs = (ConfigServerImpl) ConfigImpl.getConfigServer(config, password);
		CFMLEngineFactory factory = cs.getCFMLEngine().getCFMLEngineFactory();

		synchronized (factory) {
			try {
				cleanUp(factory);
				factory.restart(cs.getPassword());
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
		}
	}

	public void restart(ConfigServerImpl cs) throws PageException {
		CFMLEngineFactory factory = cs.getCFMLEngine().getCFMLEngineFactory();

		synchronized (factory) {
			try {
				Method m = factory.getClass().getDeclaredMethod("_restart", new Class[0]);
				if (m == null) throw new ApplicationException("Cannot restart Lucee.");
				m.setAccessible(true);
				m.invoke(factory, new Object[0]);
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
		}
	}

	public void updateWebCharset(String charset) throws PageException {
		checkWriteAccess();

		Element element = _getRootElement("charset");
		if (StringUtil.isEmpty(charset)) {
			if (config instanceof ConfigWeb) element.removeAttribute("web-charset");
			else element.setAttribute("web-charset", "UTF-8");
		}
		else {
			charset = checkCharset(charset);
			element.setAttribute("web-charset", charset);
		}

		element = _getRootElement("regional");
		element.removeAttribute("default-encoding");// remove deprecated attribute

	}

	public void updateResourceCharset(String charset) throws PageException {
		checkWriteAccess();

		Element element = _getRootElement("charset");
		if (StringUtil.isEmpty(charset)) {
			element.removeAttribute("resource-charset");
		}
		else {
			charset = checkCharset(charset);
			element.setAttribute("resource-charset", charset);

		}

		// update charset

	}

	public void updateTemplateCharset(String charset) throws PageException {

		checkWriteAccess();

		Element element = _getRootElement("charset");
		if (StringUtil.isEmpty(charset, true)) {
			element.removeAttribute("template-charset");
		}
		else {
			charset = checkCharset(charset);
			element.setAttribute("template-charset", charset);
		}
	}

	private String checkCharset(String charset) throws PageException {
		charset = charset.trim();
		if ("system".equalsIgnoreCase(charset)) charset = SystemUtil.getCharset().name();
		else if ("jre".equalsIgnoreCase(charset)) charset = SystemUtil.getCharset().name();
		else if ("os".equalsIgnoreCase(charset)) charset = SystemUtil.getCharset().name();

		// check access
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) {
			throw new SecurityException("Access Denied to update regional setting");
		}

		// check encoding
		try {
			IOUtil.checkEncoding(charset);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
		return charset;
	}

	private Resource getStoragDir(Config config) {
		Resource storageDir = config.getConfigDir().getRealResource("storage");
		if (!storageDir.exists()) storageDir.mkdirs();
		return storageDir;
	}

	public void storageSet(Config config, String key, Object value) throws ConverterException, IOException, SecurityException {
		checkWriteAccess();
		Resource storageDir = getStoragDir(config);
		Resource storage = storageDir.getRealResource(key + ".wddx");

		WDDXConverter converter = new WDDXConverter(config.getTimeZone(), true, true);
		String wddx = converter.serialize(value);
		IOUtil.write(storage, wddx, "UTF-8", false);
	}

	public Object storageGet(Config config, String key) throws ConverterException, IOException, SecurityException {
		checkReadAccess();
		Resource storageDir = getStoragDir(config);
		Resource storage = storageDir.getRealResource(key + ".wddx");
		if (!storage.exists()) throw new IOException("There is no storage named [" + key + "]");
		WDDXConverter converter = new WDDXConverter(config.getTimeZone(), true, true);
		return converter.deserialize(IOUtil.toString(storage, "UTF-8"), true);
	}

	public void updateCustomTagDeepSearch(boolean customTagDeepSearch) throws SecurityException {
		checkWriteAccess();
		if (!ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_CUSTOM_TAG)) throw new SecurityException("Access Denied to update custom tag setting");

		Element element = _getRootElement("custom-tag");
		element.setAttribute("custom-tag-deep-search", Caster.toString(customTagDeepSearch));
	}

	public void resetId() throws PageException {
		checkWriteAccess();
		Resource res = config.getConfigDir().getRealResource("id");
		try {
			if (res.exists()) res.remove(false);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}

	}

	public void updateCustomTagLocalSearch(boolean customTagLocalSearch) throws SecurityException {
		checkWriteAccess();
		if (!ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_CUSTOM_TAG)) throw new SecurityException("Access Denied to update custom tag setting");

		Element element = _getRootElement("custom-tag");
		element.setAttribute("custom-tag-local-search", Caster.toString(customTagLocalSearch));
	}

	public void updateCustomTagExtensions(String extensions) throws PageException {
		checkWriteAccess();
		if (!ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_CUSTOM_TAG)) throw new SecurityException("Access Denied to update custom tag setting");

		// check
		Array arr = ListUtil.listToArrayRemoveEmpty(extensions, ',');
		ListUtil.trimItems(arr);
		// throw new ApplicationException("you must define at least one extension");

		// update charset
		Element element = _getRootElement("custom-tag");
		element.setAttribute("extensions", ListUtil.arrayToList(arr, ","));
	}

	public void updateRemoteClient(String label, String url, String type, String securityKey, String usage, String adminPassword, String serverUsername, String serverPassword,
			String proxyServer, String proxyUsername, String proxyPassword, String proxyPort) throws PageException {
		checkWriteAccess();

		// SNSN

		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_REMOTE);
		if (!hasAccess) throw new SecurityException("Access Denied to update remote client settings");

		Element clients = _getRootElement("remote-clients");

		if (StringUtil.isEmpty(url)) throw new ExpressionException("[url] cannot be empty");
		if (StringUtil.isEmpty(securityKey)) throw new ExpressionException("[securityKey] cannot be empty");
		if (StringUtil.isEmpty(adminPassword)) throw new ExpressionException("[adminPassword] can not be empty");
		url = url.trim();
		securityKey = securityKey.trim();
		adminPassword = adminPassword.trim();

		Element[] children = XMLConfigWebFactory.getChildren(clients, "remote-client");

		// Update
		for (int i = 0; i < children.length; i++) {
			Element el = children[i];
			String _url = el.getAttribute("url");
			if (_url != null && _url.equalsIgnoreCase(url)) {
				el.setAttribute("label", label);
				el.setAttribute("type", type);
				el.setAttribute("usage", usage);
				el.setAttribute("server-username", serverUsername);
				el.setAttribute("proxy-server", proxyServer);
				el.setAttribute("proxy-username", proxyUsername);
				el.setAttribute("proxy-port", proxyPort);
				el.setAttribute("security-key", ConfigWebUtil.encrypt(securityKey));
				el.setAttribute("admin-password", ConfigWebUtil.encrypt(adminPassword));
				el.setAttribute("server-password", ConfigWebUtil.encrypt(serverPassword));
				el.setAttribute("proxy-password", ConfigWebUtil.encrypt(proxyPassword));
				return;
			}
		}

		// Insert
		Element el = doc.createElement("remote-client");

		el.setAttribute("label", label);
		el.setAttribute("url", url);
		el.setAttribute("type", type);
		el.setAttribute("usage", usage);
		el.setAttribute("server-username", serverUsername);
		el.setAttribute("proxy-server", proxyServer);
		el.setAttribute("proxy-username", proxyUsername);
		el.setAttribute("proxy-port", proxyPort);
		el.setAttribute("security-key", ConfigWebUtil.encrypt(securityKey));
		el.setAttribute("admin-password", ConfigWebUtil.encrypt(adminPassword));
		el.setAttribute("server-password", ConfigWebUtil.encrypt(serverPassword));
		el.setAttribute("proxy-password", ConfigWebUtil.encrypt(proxyPassword));

		clients.appendChild(el);
	}

	public void updateMonitor(ClassDefinition cd, String type, String name, boolean logEnabled) throws PageException {
		checkWriteAccess();
		_updateMonitor(cd, type, name, logEnabled);
	}

	void _updateMonitor(ClassDefinition cd, String type, String name, boolean logEnabled) throws PageException {
		Element parent = _getRootElement("monitoring");
		stopMonitor(ConfigWebUtil.toMonitorType(type, Monitor.TYPE_INTERVAL), name);

		Element[] children = XMLConfigWebFactory.getChildren(parent, "monitor");
		Element monitor = null;
		// Update
		for (int i = 0; i < children.length; i++) {
			Element el = children[i];
			String _name = el.getAttribute("name");
			if (_name != null && _name.equalsIgnoreCase(name)) {
				monitor = el;
				break;
			}
		}

		// Insert
		if (monitor == null) {
			monitor = doc.createElement("monitor");
			parent.appendChild(monitor);
		}
		setClass(monitor, null, "", cd);
		monitor.setAttribute("type", type);
		monitor.setAttribute("name", name);
		monitor.setAttribute("log", Caster.toString(logEnabled));
	}

	private void stopMonitor(int type, String name) {
		Monitor monitor = null;
		try {
			if (Monitor.TYPE_ACTION == type) monitor = config.getActionMonitor(name);
			else if (Monitor.TYPE_REQUEST == type) monitor = config.getRequestMonitor(name);
			else if (Monitor.TYPE_REQUEST == type) monitor = config.getIntervallMonitor(name);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		IOUtil.closeEL(monitor);
	}

	static void removeCacheHandler(ConfigImpl config, String id, boolean reload) throws IOException, SAXException, PageException, BundleException {
		XMLConfigAdmin admin = new XMLConfigAdmin(config, null);
		admin._removeCacheHandler(id);
		admin._store();
		if (reload) admin._reload();
	}

	private void _removeCache(ClassDefinition cd) {
		Element parent = _getRootElement("caches");
		Element[] children = XMLConfigWebFactory.getChildren(parent, "cache");
		for (int i = 0; i < children.length; i++) {
			Element el = children[i];
			String _class = el.getAttribute("class");
			if (_class != null && _class.equalsIgnoreCase(cd.getClassName())) {
				parent.removeChild(el);
				break;
			}
		}
	}

	private void _removeCacheHandler(String id) {
		Element parent = _getRootElement("cache-handlers");
		Element[] children = XMLConfigWebFactory.getChildren(parent, "cache-handler");
		for (int i = 0; i < children.length; i++) {
			Element el = children[i];
			String _id = el.getAttribute("id");
			if (_id != null && _id.equalsIgnoreCase(id)) {
				parent.removeChild(el);
				break;
			}
		}
	}

	/*
	 * public static void updateCacheHandler(ConfigImpl config, String id, ClassDefinition cd, boolean
	 * reload) throws IOException, SAXException, PageException, BundleException { ConfigWebAdmin admin =
	 * new ConfigWebAdmin(config, null); admin._updateCacheHandler(id, cd); admin._store();
	 * if(reload)admin._reload(); }
	 */

	public void updateCacheHandler(String id, ClassDefinition cd) throws PageException {
		checkWriteAccess();
		_updateCacheHandler(id, cd);
	}

	private void _updateCache(ClassDefinition cd) throws PageException {
		Element parent = _getRootElement("caches");

		Element[] children = XMLConfigWebFactory.getChildren(parent, "cache");
		Element ch = null;
		// Update
		for (int i = 0; i < children.length; i++) {
			Element el = children[i];
			String _class = el.getAttribute("class");
			if (_class != null && _class.equalsIgnoreCase(cd.getClassName())) {
				ch = el;
				break;
			}
		}

		// Insert
		if (ch == null) {
			ch = doc.createElement("cache");
			parent.appendChild(ch);
		}
		setClass(ch, null, "", cd);
	}

	private void _updateCacheHandler(String id, ClassDefinition cd) throws PageException {
		Element parent = _getRootElement("cache-handlers");

		Element[] children = XMLConfigWebFactory.getChildren(parent, "cache-handler");
		Element ch = null;
		// Update
		for (int i = 0; i < children.length; i++) {
			Element el = children[i];
			String _id = el.getAttribute("id");
			if (_id != null && _id.equalsIgnoreCase(id)) {
				ch = el;
				break;
			}
		}

		// Insert
		if (ch == null) {
			ch = doc.createElement("cache-handler");
			parent.appendChild(ch);
		}
		ch.setAttribute("id", id);
		setClass(ch, null, "", cd);
	}

	public void updateExecutionLog(ClassDefinition cd, Struct args, boolean enabled) throws PageException {
		Element el = _getRootElement("execution-log");
		setClass(el, null, "", cd);
		el.setAttribute("arguments", toStringCSSStyle(args));
		el.setAttribute("enabled", Caster.toString(enabled));
	}

	public void removeMonitor(String type, String name) throws SecurityException {
		checkWriteAccess();
		_removeMonitor(type, name);
	}

	void _removeMonitor(String type, String name) {

		stopMonitor(ConfigWebUtil.toMonitorType(type, Monitor.TYPE_INTERVAL), name);

		Element parent = _getRootElement("monitoring");

		Element[] children = XMLConfigWebFactory.getChildren(parent, "monitor");
		// Update
		for (int i = 0; i < children.length; i++) {
			Element el = children[i];
			String _name = el.getAttribute("name");
			if (_name != null && _name.equalsIgnoreCase(name)) {
				parent.removeChild(el);
			}
		}
	}

	public void removeCacheHandler(String id) throws PageException {
		checkWriteAccess();

		Element parent = _getRootElement("cache-handlers");

		Element[] children = XMLConfigWebFactory.getChildren(parent, "cache-handler");
		// Update
		for (int i = 0; i < children.length; i++) {
			Element el = children[i];
			String _id = el.getAttribute("id");
			if (_id != null && _id.equalsIgnoreCase(id)) {
				parent.removeChild(el);
			}
		}

	}

	public void updateExtensionInfo(boolean enabled) {
		Element extensions = _getRootElement("extensions");
		extensions.setAttribute("enabled", Caster.toString(enabled));
	}

	public void updateRHExtensionProvider(String strUrl) throws MalformedURLException {
		Element extensions = _getRootElement("extensions");
		Element[] children = XMLConfigWebFactory.getChildren(extensions, "rhprovider");
		strUrl = strUrl.trim();

		URL _url = HTTPUtil.toURL(strUrl, HTTPUtil.ENCODED_NO);
		strUrl = _url.toExternalForm();

		// Update
		Element el;
		String url;
		for (int i = 0; i < children.length; i++) {
			el = children[i];
			url = el.getAttribute("url");
			if (url != null && url.trim().equalsIgnoreCase(strUrl)) {
				// el.setAttribute("cache-timeout",Caster.toString(cacheTimeout));
				return;
			}
		}

		// Insert
		el = doc.createElement("rhprovider");

		el.setAttribute("url", strUrl);
		// el.setAttribute("cache-timeout",Caster.toString(cacheTimeout));

		XMLUtil.prependChild(extensions, el);
	}

	public void updateExtensionProvider(String strUrl) {
		Element extensions = _getRootElement("extensions");
		Element[] children = XMLConfigWebFactory.getChildren(extensions, "provider");
		strUrl = strUrl.trim();

		// Update
		Element el;
		String url;
		for (int i = 0; i < children.length; i++) {
			el = children[i];
			url = el.getAttribute("url");
			if (url != null && url.trim().equalsIgnoreCase(strUrl)) {
				// el.setAttribute("cache-timeout",Caster.toString(cacheTimeout));
				return;
			}
		}

		// Insert
		el = doc.createElement("provider");

		el.setAttribute("url", strUrl);
		// el.setAttribute("cache-timeout",Caster.toString(cacheTimeout));

		XMLUtil.prependChild(extensions, el);
	}

	public void removeExtensionProvider(String strUrl) {
		Element parent = _getRootElement("extensions");
		Element[] children = XMLConfigWebFactory.getChildren(parent, "provider");
		strUrl = strUrl.trim();
		Element child;
		String url;
		for (int i = 0; i < children.length; i++) {
			child = children[i];
			url = child.getAttribute("url");
			if (url != null && url.trim().equalsIgnoreCase(strUrl)) {
				parent.removeChild(child);
				return;
			}
		}
	}

	public void removeRHExtensionProvider(String strUrl) {
		Element parent = _getRootElement("extensions");
		Element[] children = XMLConfigWebFactory.getChildren(parent, "rhprovider");
		strUrl = strUrl.trim();
		Element child;
		String url;
		for (int i = 0; i < children.length; i++) {
			child = children[i];
			url = child.getAttribute("url");
			if (url != null && url.trim().equalsIgnoreCase(strUrl)) {
				parent.removeChild(child);
				return;
			}
		}
	}

	public void updateExtension(PageContext pc, Extension extension) throws PageException {
		checkWriteAccess();

		String uid = createUid(pc, extension.getProvider(), extension.getId());

		Element extensions = _getRootElement("extensions");
		Element[] children = XMLConfigWebFactory.getChildren(extensions, "extension");

		// Update
		Element el;
		String provider, id;
		for (int i = 0; i < children.length; i++) {
			el = children[i];
			provider = el.getAttribute("provider");
			id = el.getAttribute("id");
			if (uid.equalsIgnoreCase(createUid(pc, provider, id))) {
				setExtensionAttrs(el, extension);
				return;
			}
		}

		// Insert
		el = doc.createElement("extension");

		el.setAttribute("provider", extension.getProvider());
		el.setAttribute("id", extension.getId());
		setExtensionAttrs(el, extension);
		extensions.appendChild(el);
	}

	private String createUid(PageContext pc, String provider, String id) throws PageException {
		if (Decision.isUUId(id)) {
			return Hash.invoke(pc.getConfig(), id, null, null, 1);
		}
		return Hash.invoke(pc.getConfig(), provider + id, null, null, 1);
	}

	private void setExtensionAttrs(Element el, Extension extension) {
		el.setAttribute("version", extension.getVersion());

		el.setAttribute("config", extension.getStrConfig());
		// el.setAttribute("config",new ScriptConverter().serialize(extension.getConfig()));

		el.setAttribute("category", extension.getCategory());
		el.setAttribute("description", extension.getDescription());
		el.setAttribute("image", extension.getImage());
		el.setAttribute("label", extension.getLabel());
		el.setAttribute("name", extension.getName());

		el.setAttribute("author", extension.getAuthor());
		el.setAttribute("type", extension.getType());
		el.setAttribute("codename", extension.getCodename());
		el.setAttribute("video", extension.getVideo());
		el.setAttribute("support", extension.getSupport());
		el.setAttribute("documentation", extension.getDocumentation());
		el.setAttribute("forum", extension.getForum());
		el.setAttribute("mailinglist", extension.getMailinglist());
		el.setAttribute("network", extension.getNetwork());
		el.setAttribute("created", Caster.toString(extension.getCreated(), null));

	}

	public void resetORMSetting() throws SecurityException {
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_ORM);

		if (!hasAccess) throw new SecurityException("Access Denied to update ORM Settings");

		Element orm = _getRootElement("orm");
		orm.getParentNode().removeChild(orm);
	}

	public void updateORMSetting(ORMConfiguration oc) throws SecurityException {
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_ORM);

		if (!hasAccess) throw new SecurityException("Access Denied to update ORM Settings");

		Element orm = _getRootElement("orm");
		orm.setAttribute("autogenmap", Caster.toString(oc.autogenmap(), "true"));
		orm.setAttribute("event-handler", Caster.toString(oc.eventHandler(), ""));
		orm.setAttribute("event-handling", Caster.toString(oc.eventHandling(), "false"));
		orm.setAttribute("naming-strategy", Caster.toString(oc.namingStrategy(), ""));
		orm.setAttribute("flush-at-request-end", Caster.toString(oc.flushAtRequestEnd(), "true"));
		orm.setAttribute("cache-provider", Caster.toString(oc.getCacheProvider(), ""));
		orm.setAttribute("cache-config", Caster.toString(oc.getCacheConfig(), "true"));
		orm.setAttribute("catalog", Caster.toString(oc.getCatalog(), ""));
		orm.setAttribute("db-create", ORMConfigurationImpl.dbCreateAsString(oc.getDbCreate()));
		orm.setAttribute("dialect", Caster.toString(oc.getDialect(), ""));
		orm.setAttribute("schema", Caster.toString(oc.getSchema(), ""));
		orm.setAttribute("log-sql", Caster.toString(oc.logSQL(), "false"));
		orm.setAttribute("save-mapping", Caster.toString(oc.saveMapping(), "false"));
		orm.setAttribute("secondary-cache-enable", Caster.toString(oc.secondaryCacheEnabled(), "false"));
		orm.setAttribute("use-db-for-mapping", Caster.toString(oc.useDBForMapping(), "true"));
		orm.setAttribute("orm-config", Caster.toString(oc.getOrmConfig(), ""));
		orm.setAttribute("sql-script", Caster.toString(oc.getSqlScript(), "true"));

		if (oc.isDefaultCfcLocation()) {
			orm.removeAttribute("cfc-location");
		}
		else {
			Resource[] locations = oc.getCfcLocations();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < locations.length; i++) {
				if (i != 0) sb.append(",");
				sb.append(locations[i].getAbsolutePath());
			}
			orm.setAttribute("cfc-location", sb.toString());
		}

		orm.setAttribute("sql-script", Caster.toString(oc.getSqlScript(), "true"));

	}

	public void removeRHExtension(String id) throws PageException {
		checkWriteAccess();
		if (StringUtil.isEmpty(id, true)) return;

		Element extensions = _getRootElement("extensions");
		Element[] children = XMLConfigWebFactory.getChildren(extensions, "rhextension");
		Element child;
		RHExtension rhe;
		for (int i = 0; i < children.length; i++) {
			child = children[i];
			try {
				rhe = new RHExtension(config, child);

				// ed=ExtensionDefintion.getInstance(config,child);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				continue;
			}

			if (id.equalsIgnoreCase(rhe.getId()) || id.equalsIgnoreCase(rhe.getSymbolicName())) {
				removeRHExtension(config, rhe, null, true);
				extensions.removeChild(child);
				// bundles=RHExtension.toBundleDefinitions(child.getAttribute("bundles"));
			}
		}
	}

	public static void updateArchive(ConfigImpl config, Resource arc, boolean reload) throws PageException {
		try {
			XMLConfigAdmin admin = new XMLConfigAdmin(config, null);
			admin.updateArchive(config, arc);
			admin._store();
			if (reload) admin._reload();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
	}

	public static void updateCore(ConfigServerImpl config, Resource core, boolean reload) throws PageException {
		try {
			// get patches directory
			CFMLEngine engine = ConfigWebUtil.getEngine(config);
			ConfigServerImpl cs = config;
			Version v;
			v = CFMLEngineFactory.toVersion(core.getName(), null);
			Log logger = cs.getLog("deploy");
			File f = engine.getCFMLEngineFactory().getResourceRoot();
			Resource res = ResourcesImpl.getFileResourceProvider().getResource(f.getAbsolutePath());
			Resource pd = res.getRealResource("patches");
			if (!pd.exists()) pd.mkdirs();
			Resource pf = pd.getRealResource(core.getName());

			// move to patches directory
			core.moveTo(pf);
			core = pf;
			logger.log(Log.LEVEL_INFO, "Update-Engine", "Installing Lucee [" + v + "] (previous version was [" + cs.getEngine().getInfo().getVersion() + "] )");
			//
			XMLConfigAdmin admin = new XMLConfigAdmin(config, null);
			admin.restart(config);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			DeployHandler.moveToFailedFolder(config.getDeployDirectory(), core);
			throw Caster.toPageException(t);
		}
	}

	public void updateArchive(Config config, Resource archive) throws PageException {
		Log logger = ((ConfigImpl) config).getLog("deploy");
		String type = null, virtual = null, name = null;
		boolean readOnly, topLevel, hidden, physicalFirst;
		short inspect;
		int listMode, listType;
		InputStream is = null;
		ZipFile file = null;
		try {
			file = new ZipFile(FileWrapper.toFile(archive));
			ZipEntry entry = file.getEntry("META-INF/MANIFEST.MF");

			// no manifest
			if (entry == null) {
				DeployHandler.moveToFailedFolder(config.getDeployDirectory(), archive);
				throw new ApplicationException("Cannot deploy " + Constants.NAME + " Archive [" + archive + "], file is to old, the file does not have a MANIFEST.");
			}

			is = file.getInputStream(entry);
			Manifest manifest = new Manifest(is);
			Attributes attr = manifest.getMainAttributes();

			// id = unwrap(attr.getValue("mapping-id"));
			type = StringUtil.unwrap(attr.getValue("mapping-type"));
			virtual = StringUtil.unwrap(attr.getValue("mapping-virtual-path"));
			name = ListUtil.trim(virtual, "/");
			readOnly = Caster.toBooleanValue(StringUtil.unwrap(attr.getValue("mapping-readonly")), false);
			topLevel = Caster.toBooleanValue(StringUtil.unwrap(attr.getValue("mapping-top-level")), false);

			listMode = ConfigWebUtil.toListenerMode(StringUtil.unwrap(attr.getValue("mapping-listener-mode")), -1);
			listType = ConfigWebUtil.toListenerType(StringUtil.unwrap(attr.getValue("mapping-listener-type")), -1);

			inspect = ConfigWebUtil.inspectTemplate(StringUtil.unwrap(attr.getValue("mapping-inspect")), Config.INSPECT_UNDEFINED);
			if (inspect == Config.INSPECT_UNDEFINED) {
				Boolean trusted = Caster.toBoolean(StringUtil.unwrap(attr.getValue("mapping-trusted")), null);
				if (trusted != null) {
					if (trusted.booleanValue()) inspect = Config.INSPECT_NEVER;
					else inspect = Config.INSPECT_ALWAYS;
				}
			}

			hidden = Caster.toBooleanValue(StringUtil.unwrap(attr.getValue("mapping-hidden")), false);
			physicalFirst = Caster.toBooleanValue(StringUtil.unwrap(attr.getValue("mapping-physical-first")), false);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			DeployHandler.moveToFailedFolder(config.getDeployDirectory(), archive);
			throw Caster.toPageException(t);
		}

		finally {
			try {
				IOUtil.close(is);
			}
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
			ZipUtil.close(file);
		}
		try {
			Resource trgDir = config.getConfigDir().getRealResource("archives").getRealResource(type).getRealResource(name);
			Resource trgFile = trgDir.getRealResource(archive.getName());
			trgDir.mkdirs();

			// delete existing files

			ResourceUtil.deleteContent(trgDir, null);
			ResourceUtil.moveTo(archive, trgFile, true);
			logger.log(Log.LEVEL_INFO, "archive", "Add " + type + " mapping [" + virtual + "] with archive [" + trgFile.getAbsolutePath() + "]");
			if ("regular".equalsIgnoreCase(type)) _updateMapping(virtual, null, trgFile.getAbsolutePath(), "archive", inspect, topLevel, listMode, listType, readOnly);
			else if ("cfc".equalsIgnoreCase(type)) _updateComponentMapping(virtual, null, trgFile.getAbsolutePath(), "archive", inspect);
			else if ("ct".equalsIgnoreCase(type)) _updateCustomTag(virtual, null, trgFile.getAbsolutePath(), "archive", inspect);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			DeployHandler.moveToFailedFolder(config.getDeployDirectory(), archive);
			throw Caster.toPageException(t);
		}
	}

	public static void _updateRHExtension(ConfigImpl config, Resource ext, boolean reload) throws PageException {
		try {
			XMLConfigAdmin admin = new XMLConfigAdmin(config, null);
			admin.updateRHExtension(config, ext, reload);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	public void updateRHExtension(Config config, Resource ext, boolean reload) throws PageException {
		RHExtension rhext;
		try {
			rhext = new RHExtension(config, ext, true);
			rhext.validate();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			DeployHandler.moveToFailedFolder(ext.getParentResource(), ext);
			throw Caster.toPageException(t);
		}
		updateRHExtension(config, rhext, reload);
	}

	public void updateRHExtension(Config config, RHExtension rhext, boolean reload) throws PageException {
		ConfigImpl ci = (ConfigImpl) config;
		Log logger = ci.getLog("deploy");
		String type = ci instanceof ConfigWeb ? "web" : "server";
		// load already installed previous version and uninstall the parts no longer needed

		RHExtension existingRH = getRHExtension(ci, rhext.getId(), null);
		if (existingRH != null) {
			// same version
			if (existingRH.getVersion().compareTo(rhext.getVersion()) == 0) {
				removeRHExtension(config, existingRH, rhext, false);
			}
			else removeRHExtension(config, existingRH, rhext, true);

		}
		// INSTALL
		try {

			// boolean clearTags=false,clearFunction=false;
			boolean reloadNecessary = false;

			// store to xml
			BundleDefinition[] existing = _updateExtension(ci, rhext);
			// _storeAndReload();
			// this must happen after "store"
			cleanBundles(rhext, ci, existing);// clean after populating the new ones
			// ConfigWebAdmin.updateRHExtension(ci,rhext);

			ZipInputStream zis = new ZipInputStream(IOUtil.toBufferedInputStream(rhext.getExtensionFile().getInputStream()));
			ZipEntry entry;
			String path;
			String fileName;
			boolean isPack200;
			while ((entry = zis.getNextEntry()) != null) {
				path = entry.getName();
				fileName = fileName(entry);
				isPack200 = false;
				// jars
				if (!entry.isDirectory()
						&& (startsWith(path, type, "jars") || startsWith(path, type, "jar") || startsWith(path, type, "bundles") || startsWith(path, type, "bundle")
								|| startsWith(path, type, "lib") || startsWith(path, type, "libs"))
						&& (StringUtil.endsWithIgnoreCase(path, ".jar") || (isPack200 = StringUtil.endsWithIgnoreCase(path, ".jar.pack.gz")))) {

					Object obj = XMLConfigAdmin.installBundle(config, zis, fileName, rhext.getVersion(), false, false, isPack200);
					// jar is not a bundle, only a regular jar
					if (!(obj instanceof BundleFile)) {
						Resource tmp = (Resource) obj;
						Resource tmpJar = tmp.getParentResource().getRealResource(ListUtil.last(path, "\\/"));
						tmp.moveTo(tmpJar);
						XMLConfigAdmin.updateJar(config, tmpJar, false);
					}
				}

				// flds
				if (!entry.isDirectory() && startsWith(path, type, "flds") && (StringUtil.endsWithIgnoreCase(path, ".fld") || StringUtil.endsWithIgnoreCase(path, ".fldx"))) {
					logger.log(Log.LEVEL_INFO, "extension", "Deploy fld [" + fileName + "]");
					updateFLD(zis, fileName, false);
					reloadNecessary = true;
				}
				// tlds
				if (!entry.isDirectory() && startsWith(path, type, "tlds") && (StringUtil.endsWithIgnoreCase(path, ".tld") || StringUtil.endsWithIgnoreCase(path, ".tldx"))) {
					logger.log(Log.LEVEL_INFO, "extension", "Deploy tld/tldx [" + fileName + "]");
					updateTLD(zis, fileName, false);
					reloadNecessary = true;
				}

				// tags
				if (!entry.isDirectory() && startsWith(path, type, "tags")) {
					String sub = subFolder(entry);
					logger.log(Log.LEVEL_INFO, "extension", "Deploy tag [" + sub + "]");
					updateTag(zis, sub, false);
					// clearTags=true;
					reloadNecessary = true;
				}

				// functions
				if (!entry.isDirectory() && startsWith(path, type, "functions")) {
					String sub = subFolder(entry);
					logger.log(Log.LEVEL_INFO, "extension", "Deploy function [" + sub + "]");
					updateFunction(zis, sub, false);
					// clearFunction=true;
					reloadNecessary = true;
				}

				// mappings
				if (!entry.isDirectory() && (startsWith(path, type, "archives") || startsWith(path, type, "mappings"))) {
					String sub = subFolder(entry);
					logger.log(Log.LEVEL_INFO, "extension", "deploy mapping " + sub);
					updateArchive(zis, sub, false);
					reloadNecessary = true;
					// clearFunction=true;
				}

				// event-gateway
				if (!entry.isDirectory() && (startsWith(path, type, "event-gateways") || startsWith(path, type, "eventGateways"))
						&& (StringUtil.endsWithIgnoreCase(path, "." + Constants.getCFMLComponentExtension())
								|| StringUtil.endsWithIgnoreCase(path, "." + Constants.getLuceeComponentExtension()))) {
					String sub = subFolder(entry);
					logger.log(Log.LEVEL_INFO, "extension", "Deploy event-gateway [" + sub + "]");
					updateEventGateway(zis, sub, false);
				}

				// context
				String realpath;
				if (!entry.isDirectory() && startsWith(path, type, "context") && !StringUtil.startsWith(fileName(entry), '.')) {
					realpath = path.substring(8);
					logger.log(Log.LEVEL_INFO, "extension", "Deploy context [" + realpath + "]");
					updateContext(zis, realpath, false, false);
				}
				// web contextS
				boolean first;
				if (!entry.isDirectory() && ((first = startsWith(path, type, "webcontexts")) || startsWith(path, type, "web.contexts"))
						&& !StringUtil.startsWith(fileName(entry), '.')) {
					realpath = path.substring(first ? 12 : 13);
					logger.log(Log.LEVEL_INFO, "extension", "Deploy webcontext [" + realpath + "]");
					updateWebContexts(zis, realpath, false, false);
				}
				// applications
				if (!entry.isDirectory() && (startsWith(path, type, "applications") || startsWith(path, type, "web.applications") || startsWith(path, type, "web"))
						&& !StringUtil.startsWith(fileName(entry), '.')) {
					int index;
					if (startsWith(path, type, "applications")) index = 13;
					else if (startsWith(path, type, "web.applications")) index = 17;
					else index = 4; // web

					realpath = path.substring(index);
					logger.log(Log.LEVEL_INFO, "extension", "Deploy application [" + realpath + "]");
					updateApplication(zis, realpath, false);
				}
				// configs
				if (!entry.isDirectory() && (startsWith(path, type, "config")) && !StringUtil.startsWith(fileName(entry), '.')) {
					realpath = path.substring(7);
					logger.log(Log.LEVEL_INFO, "extension", "Deploy config [" + realpath + "]");
					updateConfigs(zis, realpath, false, false);
				}
				// components
				if (!entry.isDirectory() && (startsWith(path, type, "components")) && !StringUtil.startsWith(fileName(entry), '.')) {
					realpath = path.substring(11);
					logger.log(Log.LEVEL_INFO, "extension", "Deploy component [" + realpath + "]");
					updateComponent(zis, realpath, false, false);
				}

				// plugins
				if (!entry.isDirectory() && (startsWith(path, type, "plugins")) && !StringUtil.startsWith(fileName(entry), '.')) {
					realpath = path.substring(8);
					logger.log(Log.LEVEL_INFO, "extension", "Deploy plugin [" + realpath + "]");
					updatePlugin(zis, realpath, false);
				}

				zis.closeEntry();
			}
			////////////////////////////////////////////

			// load the bundles
			if (rhext.getStartBundles()) {
				rhext.deployBundles(ci);
				BundleInfo[] bfs = rhext.getBundles();
				if (bfs != null) {
					for (BundleInfo bf: bfs) {
						OSGiUtil.loadBundleFromLocal(bf.getSymbolicName(), bf.getVersion(), null, false, null);
					}
				}
			}

			// update cache
			if (!ArrayUtil.isEmpty(rhext.getCaches())) {
				Iterator<Map<String, String>> itl = rhext.getCaches().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					if (cd != null && cd.isBundle()) {
						_updateCache(cd);
						reloadNecessary = true;
					}
					logger.info("extension", "Update cache [" + cd + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]");
				}
			}

			// update cache handler
			if (!ArrayUtil.isEmpty(rhext.getCacheHandlers())) {
				Iterator<Map<String, String>> itl = rhext.getCacheHandlers().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					String _id = map.get("id");
					if (!StringUtil.isEmpty(_id) && cd != null && cd.hasClass()) {
						_updateCacheHandler(_id, cd);
						reloadNecessary = true;
					}
					logger.info("extension", "Update cache handler [" + cd + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]");
				}
			}

			// update AMF
			if (!ArrayUtil.isEmpty(rhext.getAMFs())) {
				Iterator<Map<String, String>> itl = rhext.getAMFs().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					if (cd != null && cd.hasClass()) {
						_updateAMFEngine(cd, map.get("caster"), map.get("configuration"));
						reloadNecessary = true;
					}
					logger.info("extension", "Update AMF engine [" + cd + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]");
				}
			}

			// update Search
			if (!ArrayUtil.isEmpty(rhext.getSearchs())) {
				Iterator<Map<String, String>> itl = rhext.getSearchs().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					if (cd != null && cd.hasClass()) {
						_updateSearchEngine(cd);
						reloadNecessary = true;
					}
					logger.info("extension", "Update search engine [" + cd + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]");
				}
			}

			// update Resource
			if (!ArrayUtil.isEmpty(rhext.getResources())) {
				Iterator<Map<String, String>> itl = rhext.getResources().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					String scheme = map.get("scheme");
					if (cd != null && cd.hasClass() && !StringUtil.isEmpty(scheme)) {
						Struct args = new StructImpl();
						copyButIgnoreClassDef(map, args);
						args.remove("scheme");
						_updateResourceProvider(scheme, cd, args);
						reloadNecessary = true;
					}
					logger.info("extension", "Update resource provider [" + scheme + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]");
				}
			}

			// update orm
			if (!ArrayUtil.isEmpty(rhext.getOrms())) {
				Iterator<Map<String, String>> itl = rhext.getOrms().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);

					if (cd != null && cd.hasClass()) {
						_updateORMEngine(cd);
						reloadNecessary = true;
					}
					logger.info("extension", "Update orm engine [" + cd + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]");
				}
			}

			// update webservice
			if (!ArrayUtil.isEmpty(rhext.getWebservices())) {
				Iterator<Map<String, String>> itl = rhext.getWebservices().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);

					if (cd != null && cd.hasClass()) {
						_updateWebserviceHandler(cd);
						reloadNecessary = true;
					}
					logger.info("extension", "Update webservice handler [" + cd + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]");
				}
			}

			// update monitor
			if (!ArrayUtil.isEmpty(rhext.getMonitors())) {
				Iterator<Map<String, String>> itl = rhext.getMonitors().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					if (cd != null && cd.hasClass()) {
						_updateMonitorEnabled(true);
						_updateMonitor(cd, map.get("type"), map.get("name"), true);
						reloadNecessary = true;
					}
					logger.info("extension", "Update monitor engine [" + cd + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]");
				}
			}

			// update jdbc
			if (!ArrayUtil.isEmpty(rhext.getJdbcs())) {
				Iterator<Map<String, String>> itl = rhext.getJdbcs().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					String _label = map.get("label");
					String _id = map.get("id");
					if (cd != null && cd.isBundle()) {
						_updateJDBCDriver(_label, _id, cd);
						reloadNecessary = true;
					}
					logger.info("extension", "Update JDBC Driver [" + _label + ":" + cd + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]");
				}
			}

			// update startup hook
			if (!ArrayUtil.isEmpty(rhext.getStartupHooks())) {
				Iterator<Map<String, String>> itl = rhext.getStartupHooks().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					if (cd != null && cd.isBundle()) {
						_updateStartupHook(cd);
						reloadNecessary = true;
					}
					logger.info("extension", "Update Startup Hook [" + cd + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]");
				}
			}

			// update mapping
			if (!ArrayUtil.isEmpty(rhext.getMappings())) {
				Iterator<Map<String, String>> itl = rhext.getMappings().iterator();
				Map<String, String> map;

				String virtual, physical, archive, primary;
				short inspect;
				int lmode, ltype;
				boolean toplevel, readonly;
				while (itl.hasNext()) {
					map = itl.next();
					virtual = map.get("virtual");
					physical = map.get("physical");
					archive = map.get("archive");
					primary = map.get("primary");

					inspect = ConfigWebUtil.inspectTemplate(map.get("inspect"), Config.INSPECT_UNDEFINED);
					lmode = ConfigWebUtil.toListenerMode(map.get("listener-mode"), -1);
					ltype = ConfigWebUtil.toListenerType(map.get("listener-type"), -1);

					toplevel = Caster.toBooleanValue(map.get("toplevel"), false);
					readonly = Caster.toBooleanValue(map.get("readonly"), false);

					_updateMapping(virtual, physical, archive, primary, inspect, toplevel, lmode, ltype, readonly);
					reloadNecessary = true;

					logger.info("extension", "Update Mapping [" + virtual + "]");
				}
			}

			// update event-gateway-instance

			if (!ArrayUtil.isEmpty(rhext.getEventGatewayInstances())) {
				Iterator<Map<String, Object>> itl = rhext.getEventGatewayInstances().iterator();
				Map<String, Object> map;
				while (itl.hasNext()) {
					map = itl.next();
					// id
					String id = Caster.toString(map.get("id"), null);
					// class
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					// component path
					String cfcPath = Caster.toString(map.get("cfc-path"), null);
					if (StringUtil.isEmpty(cfcPath)) cfcPath = Caster.toString(map.get("component-path"), null);
					// listener component path
					String listenerCfcPath = Caster.toString(map.get("listener-cfc-path"), null);
					if (StringUtil.isEmpty(listenerCfcPath)) listenerCfcPath = Caster.toString(map.get("listener-component-path"), null);
					// startup mode
					String strStartupMode = Caster.toString(map.get("startup-mode"), "automatic");
					int startupMode = GatewayEntryImpl.toStartup(strStartupMode, GatewayEntryImpl.STARTUP_MODE_AUTOMATIC);
					// read only
					boolean readOnly = Caster.toBooleanValue(map.get("read-only"), false);
					// custom
					Struct custom = Caster.toStruct(map.get("custom"), null);
					/*
					 * print.e("::::::::::::::::::::::::::::::::::::::::::"); print.e("id:"+id); print.e("cd:"+cd);
					 * print.e("cfc:"+cfcPath); print.e("listener:"+listenerCfcPath);
					 * print.e("startupMode:"+startupMode); print.e(custom);
					 */

					if (!StringUtil.isEmpty(id) && (!StringUtil.isEmpty(cfcPath) || (cd != null && cd.hasClass()))) {
						_updateGatewayEntry(id, cd, cfcPath, listenerCfcPath, startupMode, custom, readOnly);
					}

					logger.info("extension", "Update event gateway entry [" + id + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]");
				}
			}

			// reload
			// if(reloadNecessary){
			reloadNecessary = true;
			if (reload && reloadNecessary) _storeAndReload();
			else _store();
			// }
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			DeployHandler.moveToFailedFolder(rhext.getExtensionFile().getParentResource(), rhext.getExtensionFile());
			try {
				XMLConfigAdmin.removeRHExtensions((ConfigImpl) config, new String[] { rhext.getId() }, false);
			}
			catch (Throwable t2) {
				ExceptionUtil.rethrowIfNecessary(t2);
			}
			throw Caster.toPageException(t);
		}
	}

	private void copyButIgnoreClassDef(Map<String, String> src, Struct trg) {
		Iterator<Entry<String, String>> it = src.entrySet().iterator();
		Entry<String, String> e;
		String name;
		while (it.hasNext()) {
			e = it.next();
			name = e.getKey();
			if ("class".equals(name) || "bundle-name".equals(name) || "bundlename".equals(name) || "bundleName".equals(name) || "bundle-version".equals(name)
					|| "bundleversion".equals(name) || "bundleVersion".equals(name))
				continue;
			trg.setEL(name, e.getValue());
		}
	}

	/**
	 * removes an installed extension from the system
	 * 
	 * @param config
	 * @param rhe extension to remove
	 * @param replacementRH the extension that will replace this extension, so do not remove parts
	 *            defined in this extension.
	 * @throws PageException
	 */
	private void removeRHExtension(Config config, RHExtension rhe, RHExtension replacementRH, boolean deleteExtension) throws PageException {
		ConfigImpl ci = ((ConfigImpl) config);
		Log logger = ci.getLog("deploy");

		// MUST check replacementRH everywhere

		try {
			// remove the bundles
			BundleDefinition[] candidatesToRemove = OSGiUtil.toBundleDefinitions(rhe.getBundles(EMPTY));
			if (replacementRH != null) {
				// spare bundles used in the new extension as well
				Map<String, BundleDefinition> notRemove = toMap(OSGiUtil.toBundleDefinitions(replacementRH.getBundles(EMPTY)));
				List<BundleDefinition> tmp = new ArrayList<OSGiUtil.BundleDefinition>();
				String key;
				for (int i = 0; i < candidatesToRemove.length; i++) {
					key = candidatesToRemove[i].getName() + "|" + candidatesToRemove[i].getVersionAsString();
					if (notRemove.containsKey(key)) continue;
					tmp.add(candidatesToRemove[i]);
				}
				candidatesToRemove = tmp.toArray(new BundleDefinition[tmp.size()]);
			}
			XMLConfigAdmin.cleanBundles(rhe, ci, candidatesToRemove);

			// FLD
			removeFLDs(logger, rhe.getFlds()); // MUST check if others use one of this fld

			// TLD
			removeTLDs(logger, rhe.getTlds()); // MUST check if others use one of this tld

			// Tag
			removeTags(logger, rhe.getTags());

			// Functions
			removeFunctions(logger, rhe.getFunctions());

			// Event Gateway
			removeEventGateways(logger, rhe.getEventGateways());

			// context
			removeContext(config, false, logger, rhe.getContexts()); // MUST check if others use one of this

			// web contextS
			removeWebContexts(config, false, logger, rhe.getWebContexts()); // MUST check if others use one of this

			// applications
			removeApplications(config, logger, rhe.getApplications()); // MUST check if others use one of this

			// plugins
			removePlugins(config, logger, rhe.getPlugins()); // MUST check if others use one of this

			// remove cache handler
			if (!ArrayUtil.isEmpty(rhe.getCacheHandlers())) {
				Iterator<Map<String, String>> itl = rhe.getCacheHandlers().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					String _id = map.get("id");

					if (!StringUtil.isEmpty(_id) && cd != null && cd.hasClass()) {
						_removeCacheHandler(_id);
						// reload=true;
					}
					logger.info("extension", "Remove cache handler [" + cd + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]");
				}
			}

			// remove cache
			if (!ArrayUtil.isEmpty(rhe.getCaches())) {
				Iterator<Map<String, String>> itl = rhe.getCaches().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					if (cd != null && cd.isBundle()) {
						_removeCache(cd);
						// reload=true;
					}
					logger.info("extension", "Remove cache handler [" + cd + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]");
				}
			}

			// remove Search
			if (!ArrayUtil.isEmpty(rhe.getSearchs())) {
				Iterator<Map<String, String>> itl = rhe.getSearchs().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					if (cd != null && cd.hasClass()) {
						_removeSearchEngine();
						// reload=true;
					}
					logger.info("extension", "Remove search engine [" + cd + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]");
				}
			}

			// remove resource
			if (!ArrayUtil.isEmpty(rhe.getResources())) {
				Iterator<Map<String, String>> itl = rhe.getResources().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					String scheme = map.get("scheme");
					if (cd != null && cd.hasClass()) {
						_removeResourceProvider(scheme);
					}
					logger.info("extension", "Remove resource [" + cd + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]");
				}
			}

			// remove AMF
			if (!ArrayUtil.isEmpty(rhe.getAMFs())) {
				Iterator<Map<String, String>> itl = rhe.getAMFs().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					if (cd != null && cd.hasClass()) {
						_removeAMFEngine();
						// reload=true;
					}
					logger.info("extension", "Remove search engine [" + cd + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]");
				}
			}

			// remove orm
			if (!ArrayUtil.isEmpty(rhe.getOrms())) {
				Iterator<Map<String, String>> itl = rhe.getOrms().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);

					if (cd != null && cd.hasClass()) {
						_removeORMEngine();
						// reload=true;
					}
					logger.info("extension", "Remove orm engine [" + cd + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]");
				}
			}

			// remove webservice
			if (!ArrayUtil.isEmpty(rhe.getWebservices())) {
				Iterator<Map<String, String>> itl = rhe.getWebservices().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);

					if (cd != null && cd.hasClass()) {
						_removeWebserviceHandler();
						// reload=true;
					}
					logger.info("extension", "Remove webservice handler [" + cd + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]");
				}
			}

			// remove monitor
			if (!ArrayUtil.isEmpty(rhe.getMonitors())) {
				Iterator<Map<String, String>> itl = rhe.getMonitors().iterator();
				Map<String, String> map;
				String name;
				while (itl.hasNext()) {
					map = itl.next();

					// ClassDefinition cd = RHExtension.toClassDefinition(config,map);

					// if(cd.hasClass()) {
					_removeMonitor(map.get("type"), name = map.get("name"));
					// reload=true;
					// }
					logger.info("extension", "Remove monitor [" + name + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]");
				}
			}

			// remove jdbc
			if (!ArrayUtil.isEmpty(rhe.getJdbcs())) {
				Iterator<Map<String, String>> itl = rhe.getJdbcs().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					if (cd != null && cd.isBundle()) {
						_removeJDBCDriver(cd);
					}
					logger.info("extension", "Remove JDBC Driver [" + cd + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]");
				}
			}

			// remove startup hook
			if (!ArrayUtil.isEmpty(rhe.getStartupHooks())) {
				Iterator<Map<String, String>> itl = rhe.getStartupHooks().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					if (cd != null && cd.isBundle()) {
						_removeStartupHook(cd);
					}
					logger.info("extension", "Remove Startup Hook [" + cd + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]");
				}
			}

			// remove mapping
			if (!ArrayUtil.isEmpty(rhe.getMappings())) {
				Iterator<Map<String, String>> itl = rhe.getMappings().iterator();
				Map<String, String> map;
				String virtual;
				while (itl.hasNext()) {
					map = itl.next();
					virtual = map.get("virtual");
					_removeMapping(virtual);
					logger.info("extension", "remove Mapping [" + virtual + "]");
				}
			}

			// remove event-gateway-instance
			if (!ArrayUtil.isEmpty(rhe.getEventGatewayInstances())) {
				Iterator<Map<String, Object>> itl = rhe.getEventGatewayInstances().iterator();
				Map<String, Object> map;
				String id;
				while (itl.hasNext()) {
					map = itl.next();
					id = Caster.toString(map.get("id"), null);
					if (!StringUtil.isEmpty(id)) {
						_removeGatewayEntry(id);
						logger.info("extension", "remove event gateway entry [" + id + "]");
					}
				}
			}

			// Loop Files
			ZipInputStream zis = new ZipInputStream(IOUtil.toBufferedInputStream(rhe.getExtensionFile().getInputStream()));
			String type = ci instanceof ConfigWeb ? "web" : "server";
			try {
				ZipEntry entry;
				String path;
				String fileName;
				Resource tmp;
				while ((entry = zis.getNextEntry()) != null) {
					path = entry.getName();
					fileName = fileName(entry);

					// archives
					if (!entry.isDirectory() && (startsWith(path, type, "archives") || startsWith(path, type, "mappings"))) {
						String sub = subFolder(entry);
						logger.log(Log.LEVEL_INFO, "extension", "Remove archive [" + sub + "] registered as a mapping");
						tmp = SystemUtil.getTempFile(".lar", false);
						IOUtil.copy(zis, tmp, false);
						removeArchive(tmp);
					}
					zis.closeEntry();
				}
			}
			finally {
				IOUtil.close(zis);
			}

			// now we can delete the extension
			if (deleteExtension) rhe.getExtensionFile().delete();

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			// failed to uninstall, so we install it again
			try {
				updateRHExtension(config, rhe.getExtensionFile(), true);
				// RHExtension.install(config, rhe.getExtensionFile());
			}
			catch (Throwable t2) {
				ExceptionUtil.rethrowIfNecessary(t2);
			}
			throw Caster.toPageException(t);
		}

	}

	private Map<String, BundleDefinition> toMap(BundleDefinition[] bundleDefinitions) {
		Map<String, BundleDefinition> rtn = new HashMap<String, OSGiUtil.BundleDefinition>();
		for (int i = 0; i < bundleDefinitions.length; i++) {
			rtn.put(bundleDefinitions[i].getName() + "|" + bundleDefinitions[i].getVersionAsString(), bundleDefinitions[i]);
		}
		return rtn;
	}

	private static boolean startsWith(String path, String type, String name) {
		return StringUtil.startsWithIgnoreCase(path, name + "/") || StringUtil.startsWithIgnoreCase(path, type + "/" + name + "/");
	}

	private static String fileName(ZipEntry entry) {
		String name = entry.getName();
		int index = name.lastIndexOf('/');
		if (index == -1) return name;
		return name.substring(index + 1);
	}

	private static String subFolder(ZipEntry entry) {
		String name = entry.getName();
		int index = name.indexOf('/');
		if (index == -1) return name;
		return name.substring(index + 1);
	}

	public void removeExtension(String provider, String id) throws SecurityException {
		checkWriteAccess();

		Element extensions = _getRootElement("extensions");
		Element[] children = XMLConfigWebFactory.getChildren(extensions, "extension");
		Element child;
		String _provider, _id;
		for (int i = 0; i < children.length; i++) {
			child = children[i];
			_provider = child.getAttribute("provider");
			_id = child.getAttribute("id");
			if (_provider != null && _provider.equalsIgnoreCase(provider) && _id != null && _id.equalsIgnoreCase(id)) {
				extensions.removeChild(child);
			}
		}
	}

	public void verifyExtensionProvider(String strUrl) throws PageException {
		HTTPResponse method = null;
		try {
			URL url = HTTPUtil.toURL(strUrl + "?wsdl", HTTPUtil.ENCODED_AUTO);
			method = HTTPEngine.get(url, null, null, 2000, true, null, null, null, null);
		}
		catch (MalformedURLException e) {
			throw new ApplicationException("Url definition [" + strUrl + "] is invalid");
		}
		catch (IOException e) {
			throw new ApplicationException("Can't invoke [" + strUrl + "]", e.getMessage());
		}

		if (method.getStatusCode() != 200) {
			int code = method.getStatusCode();
			String text = method.getStatusText();
			String msg = code + " " + text;
			throw new HTTPException(msg, null, code, text, method.getURL());
		}
		// Object o =
		CreateObject.doWebService(null, strUrl + "?wsdl");
		HTTPEngine.closeEL(method);
	}

	public void updateTLD(Resource resTld) throws IOException {
		updateLD(config.getTldFile(), resTld);
	}

	public void updateFLD(Resource resFld) throws IOException {
		updateLD(config.getFldFile(), resFld);
	}

	private void updateLD(Resource dir, Resource res) throws IOException {
		if (!dir.exists()) dir.createDirectory(true);

		Resource file = dir.getRealResource(res.getName());
		if (file.length() != res.length()) {
			ResourceUtil.copy(res, file);
		}
	}

	void updateTLD(InputStream is, String name, boolean closeStream) throws IOException {
		write(config.getTldFile(), is, name, closeStream);
	}

	void updateFLD(InputStream is, String name, boolean closeStream) throws IOException {
		write(config.getFldFile(), is, name, closeStream);
	}

	void updateTag(InputStream is, String name, boolean closeStream) throws IOException {
		write(config.getDefaultTagMapping().getPhysical(), is, name, closeStream);
	}

	void updateFunction(InputStream is, String name, boolean closeStream) throws IOException {
		write(config.getDefaultFunctionMapping().getPhysical(), is, name, closeStream);
	}

	void updateEventGateway(InputStream is, String name, boolean closeStream) throws IOException {
		write(config.getEventGatewayDirectory(), is, name, closeStream);
	}

	void updateArchive(InputStream is, String name, boolean closeStream) throws IOException, PageException {
		Resource res = write(SystemUtil.getTempDirectory(), is, name, closeStream);
		// Resource res = write(DeployHandler.getDeployDirectory(config),is,name,closeStream);
		updateArchive(config, res);
	}

	private static Resource write(Resource dir, InputStream is, String name, boolean closeStream) throws IOException {
		if (!dir.exists()) dir.createDirectory(true);
		Resource file = dir.getRealResource(name);
		Resource p = file.getParentResource();
		if (!p.exists()) p.createDirectory(true);
		IOUtil.copy(is, file.getOutputStream(), closeStream, true);
		return file;
	}

	public void removeTLD(String name) throws IOException {
		removeFromDirectory(config.getTldFile(), name);
	}

	public void removeTLDs(Log logger, String[] names) throws IOException {
		if (ArrayUtil.isEmpty(names)) return;
		Resource file = config.getTldFile();
		for (int i = 0; i < names.length; i++) {
			logger.log(Log.LEVEL_INFO, "extension", "Remove TLD file " + names[i]);
			removeFromDirectory(file, names[i]);
		}
	}

	public void removeEventGateways(Log logger, String[] relpath) throws IOException {
		if (ArrayUtil.isEmpty(relpath)) return;
		Resource dir = config.getEventGatewayDirectory();// get Event gateway Directory
		for (int i = 0; i < relpath.length; i++) {
			logger.log(Log.LEVEL_INFO, "extension", "Remove Event Gateway " + relpath[i]);
			removeFromDirectory(dir, relpath[i]);
		}
	}

	public void removeFunctions(Log logger, String[] relpath) throws IOException {
		if (ArrayUtil.isEmpty(relpath)) return;
		Resource file = config.getDefaultFunctionMapping().getPhysical();
		for (int i = 0; i < relpath.length; i++) {
			logger.log(Log.LEVEL_INFO, "extension", "Remove Function " + relpath[i]);
			removeFromDirectory(file, relpath[i]);
		}
	}

	public void removeArchive(Resource archive) throws IOException, PageException {
		Log logger = config.getLog("deploy");
		String virtual = null, type = null;
		InputStream is = null;
		ZipFile file = null;
		try {
			file = new ZipFile(FileWrapper.toFile(archive));
			ZipEntry entry = file.getEntry("META-INF/MANIFEST.MF");

			// no manifest
			if (entry == null) throw new ApplicationException("Cannot remove " + Constants.NAME + " Archive [" + archive + "], file is to old, the file does not have a MANIFEST.");

			is = file.getInputStream(entry);
			Manifest manifest = new Manifest(is);
			Attributes attr = manifest.getMainAttributes();
			virtual = StringUtil.unwrap(attr.getValue("mapping-virtual-path"));
			type = StringUtil.unwrap(attr.getValue("mapping-type"));
			logger.info("archive", "Remove " + type + " mapping [" + virtual + "]");

			if ("regular".equalsIgnoreCase(type)) removeMapping(virtual);
			else if ("cfc".equalsIgnoreCase(type)) removeComponentMapping(virtual);
			else if ("ct".equalsIgnoreCase(type)) removeCustomTag(virtual);
			else throw new ApplicationException("Invalid type [" + type + "], valid types are [regular, cfc, ct]");
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
		finally {
			IOUtil.close(is);
			ZipUtil.close(file);
		}
	}

	public void removeTags(Log logger, String[] relpath) throws IOException {
		if (ArrayUtil.isEmpty(relpath)) return;
		Resource file = config.getDefaultTagMapping().getPhysical();
		for (int i = 0; i < relpath.length; i++) {
			logger.log(Log.LEVEL_INFO, "extension", "Remove Tag [" + relpath[i] + "]");
			removeFromDirectory(file, relpath[i]);
		}
	}

	public void removeFLDs(Log logger, String[] names) throws IOException {
		if (ArrayUtil.isEmpty(names)) return;

		Resource file = config.getFldFile();
		for (int i = 0; i < names.length; i++) {
			logger.log(Log.LEVEL_INFO, "extension", "Remove FLD file [" + names[i] + "]");
			removeFromDirectory(file, names[i]);
		}
	}

	public void removeFLD(String name) throws IOException {
		removeFromDirectory(config.getFldFile(), name);
	}

	private void removeFromDirectory(Resource dir, String relpath) throws IOException {
		if (dir.isDirectory()) {
			Resource file = dir.getRealResource(relpath);
			if (file.isFile()) file.remove(false);
		}
	}

	public void updateRemoteClientUsage(String code, String displayname) {
		Struct usage = config.getRemoteClientUsage();
		usage.setEL(code, displayname);

		Element extensions = _getRootElement("remote-clients");
		extensions.setAttribute("usage", toStringURLStyle(usage));

	}

	public void updateClusterClass(ClassDefinition cd) throws PageException {
		if (cd.getClassName() == null) cd = new ClassDefinitionImpl(ClusterNotSupported.class.getName(), null, null, null);

		Class clazz = null;
		try {
			clazz = cd.getClazz();
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		if (!Reflector.isInstaneOf(clazz, Cluster.class, false) && !Reflector.isInstaneOf(clazz, ClusterRemote.class, false)) throw new ApplicationException(
				"Class [" + clazz.getName() + "] does not implement interface [" + Cluster.class.getName() + "] or [" + ClusterRemote.class.getName() + "]");

		Element scope = _getRootElement("scope");
		setClass(scope, null, "cluster-", cd);
		ScopeContext.clearClusterScope();
	}

	public void updateVideoExecuterClass(ClassDefinition cd) throws PageException {

		if (cd.getClassName() == null) cd = new ClassDefinitionImpl(VideoExecuterNotSupported.class.getName());

		Element app = _getRootElement("video");
		setClass(app, VideoExecuter.class, "video-executer-", cd);
	}

	public void updateAdminSyncClass(ClassDefinition cd) throws PageException {

		if (cd.getClassName() == null) cd = new ClassDefinitionImpl(AdminSyncNotSupported.class.getName());

		Element app = _getRootElement("application");
		setClass(app, AdminSync.class, "admin-sync-", cd);
	}

	public void removeRemoteClientUsage(String code) {
		Struct usage = config.getRemoteClientUsage();
		usage.removeEL(KeyImpl.getInstance(code));

		Element extensions = _getRootElement("remote-clients");
		extensions.setAttribute("usage", toStringURLStyle(usage));

	}

	class MyResourceNameFilter implements ResourceNameFilter {
		private String name;

		public MyResourceNameFilter(String name) {
			this.name = name;
		}

		@Override
		public boolean accept(Resource parent, String name) {
			return name.equals(this.name);
		}
	}

	public void updateSerial(String serial) throws PageException {

		checkWriteAccess();
		if (!(config instanceof ConfigServer)) {
			throw new SecurityException("Can't change serial number from this context, access is denied");
		}

		Element root = doc.getDocumentElement();
		if (!StringUtil.isEmpty(serial)) {
			serial = serial.trim();
			if (!new SerialNumber(serial).isValid(serial)) throw new SecurityException("Serial number is invalid");
			root.setAttribute("serial-number", serial);
		}
		else {
			try {
				root.removeAttribute("serial-number");
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}
		try {
			root.removeAttribute("serial");
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	public boolean updateLabel(String hash, String label) {
		// check
		if (StringUtil.isEmpty(hash, true)) return false;
		if (StringUtil.isEmpty(label, true)) return false;

		hash = hash.trim();
		label = label.trim();

		Element labels = _getRootElement("labels");

		// Update
		Element[] children = XMLConfigWebFactory.getChildren(labels, "label");
		for (int i = 0; i < children.length; i++) {
			String h = children[i].getAttribute("id");
			if (h != null) {
				if (h.equals(hash)) {
					Element el = children[i];
					if (label.equals(el.getAttribute("name"))) return false;
					el.setAttribute("name", label);
					return true;
				}
			}
		}

		// Insert
		Element el = doc.createElement("label");
		labels.appendChild(el);
		el.setAttribute("id", hash);
		el.setAttribute("name", label);

		return true;
	}

	public void updateDebugSetting(int maxLogs) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_DEBUGGING);
		if (!hasAccess) throw new SecurityException("Access denied to change debugging settings");

		Element debugging = _getRootElement("debugging");
		if (maxLogs == -1) debugging.removeAttribute("max-records-logged");
		else debugging.setAttribute("max-records-logged", Caster.toString(maxLogs));
	}

	public void updateDebugEntry(String type, String iprange, String label, String path, String fullname, Struct custom) throws SecurityException, IOException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_DEBUGGING);
		if (!hasAccess) throw new SecurityException("Access denied to change debugging settings");

		// leave this, this method throws an exception when ip range is not valid
		IPRange.getInstance(iprange);

		String id = MD5.getDigestAsString(label.trim().toLowerCase());
		type = type.trim();
		iprange = iprange.trim();
		label = label.trim();

		Element debugging = _getRootElement("debugging");

		// Update
		Element[] children = XMLConfigWebFactory.getChildren(debugging, "debug-entry");
		Element el = null;
		for (int i = 0; i < children.length; i++) {
			String _id = children[i].getAttribute("id");
			if (_id != null) {
				if (_id.equals(id)) {
					el = children[i];
					break;
				}
			}
		}

		// Insert
		if (el == null) {
			el = doc.createElement("debug-entry");
			debugging.appendChild(el);
			el.setAttribute("id", id);
		}

		el.setAttribute("type", type);
		el.setAttribute("iprange", iprange);
		el.setAttribute("label", label);
		el.setAttribute("path", path);
		el.setAttribute("fullname", fullname);
		el.setAttribute("custom", toStringURLStyle(custom));
	}

	public void removeDebugEntry(String id) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_DEBUGGING);
		if (!hasAccess) throw new SecurityException("Access denied to change debugging settings");

		Element debugging = _getRootElement("debugging");
		Element[] children = XMLConfigWebFactory.getChildren(debugging, "debug-entry");
		String _id;
		if (children.length > 0) {
			for (int i = 0; i < children.length; i++) {
				Element el = children[i];
				_id = el.getAttribute("id");
				if (_id != null && _id.equalsIgnoreCase(id)) {
					debugging.removeChild(children[i]);
				}
			}
		}
	}

	public void updateLoginSettings(boolean captcha, boolean rememberMe, int delay) {
		Element login = _getRootElement("login");
		login.setAttribute("captcha", Caster.toString(captcha));
		login.setAttribute("rememberme", Caster.toString(rememberMe));
		login.setAttribute("delay", Caster.toString(delay));
	}

	public void updateLogSettings(String name, int level, ClassDefinition appenderCD, Struct appenderArgs, ClassDefinition layoutCD, Struct layoutArgs) throws PageException {
		checkWriteAccess();
		// TODO
		// boolean hasAccess=ConfigWebUtil.hasAccess(config,SecurityManagerImpl.TYPE_GATEWAY);
		// if(!hasAccess) throw new SecurityException("no access to update gateway entry");

		// check parameters
		name = name.trim();
		if (StringUtil.isEmpty(name)) throw new ApplicationException("Log file name cannot be empty");

		if (appenderCD == null || !appenderCD.hasClass()) throw new ExpressionException("Appender class is required");
		if (layoutCD == null || !layoutCD.hasClass()) throw new ExpressionException("Layout class is required");

		try {
			appenderCD.getClazz();
			layoutCD.getClazz();
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		Element parent = _getRootElement("logging");

		// Update
		Element[] children = XMLConfigWebFactory.getChildren(parent, "logger");
		Element el = null;
		for (int i = 0; i < children.length; i++) {
			String n = children[i].getAttribute("name");
			if (name.equalsIgnoreCase(n)) {
				el = children[i];
				break;
			}
		}
		// Insert
		if (el == null) {
			el = doc.createElement("logger");
			parent.appendChild(el);
			el.setAttribute("name", name);
		}

		el.setAttribute("level", LogUtil.levelToString(level, ""));
		setClass(el, null, "appender-", appenderCD);
		el.setAttribute("appender-arguments", toStringCSSStyle(appenderArgs));
		setClass(el, null, "layout-", layoutCD);
		el.setAttribute("layout-arguments", toStringCSSStyle(layoutArgs));

		if (el.hasAttribute("appender")) el.removeAttribute("appender");
		if (el.hasAttribute("layout")) el.removeAttribute("layout");
	}

	public void updateCompilerSettings(Boolean dotNotationUpperCase, Boolean suppressWSBeforeArg, Boolean nullSupport, Boolean handleUnQuotedAttrValueAsString,
			Integer externalizeStringGTE) throws PageException {

		Element element = _getRootElement("compiler");

		checkWriteAccess();
		if (dotNotationUpperCase == null) {
			if (element.hasAttribute("dot-notation-upper-case")) element.removeAttribute("dot-notation-upper-case");
		}
		else {
			element.setAttribute("dot-notation-upper-case", Caster.toString(dotNotationUpperCase));
		}

		// remove old settings
		if (element.hasAttribute("supress-ws-before-arg")) element.removeAttribute("supress-ws-before-arg");

		if (suppressWSBeforeArg == null) {
			if (element.hasAttribute("suppress-ws-before-arg")) element.removeAttribute("suppress-ws-before-arg");
		}
		else {
			element.setAttribute("suppress-ws-before-arg", Caster.toString(suppressWSBeforeArg));
		}

		// full null support
		if (nullSupport == null) {
			if (element.hasAttribute("full-null-support")) element.removeAttribute("full-null-support");
		}
		else {
			element.setAttribute("full-null-support", Caster.toString(nullSupport));
		}

		// externalize-string-gte
		if (externalizeStringGTE == null) {
			if (element.hasAttribute("externalize-string-gte")) element.removeAttribute("externalize-string-gte");
		}
		else {
			element.setAttribute("externalize-string-gte", Caster.toString(externalizeStringGTE));
		}

		// handle Unquoted Attribute Values As String
		if (handleUnQuotedAttrValueAsString == null) {
			if (element.hasAttribute("handle-unquoted-attribute-value-as-string")) element.removeAttribute("handle-unquoted-attribute-value-as-string");
		}
		else {
			element.setAttribute("handle-unquoted-attribute-value-as-string", Caster.toString(handleUnQuotedAttrValueAsString));
		}

	}

	Resource[] updateWebContexts(InputStream is, String realpath, boolean closeStream, boolean store) throws PageException, IOException, SAXException, BundleException {
		List<Resource> filesDeployed = new ArrayList<Resource>();

		if (config instanceof ConfigWeb) {
			XMLConfigAdmin._updateContextClassic(config, is, realpath, closeStream, filesDeployed);
		}
		else XMLConfigAdmin._updateWebContexts(config, is, realpath, closeStream, filesDeployed, store);

		return filesDeployed.toArray(new Resource[filesDeployed.size()]);
	}

	private static void _updateWebContexts(Config config, InputStream is, String realpath, boolean closeStream, List<Resource> filesDeployed, boolean store)
			throws PageException, IOException, SAXException, BundleException {
		if (!(config instanceof ConfigServer)) throw new ApplicationException("Invalid context, you can only call this method from server context");
		ConfigServer cs = (ConfigServer) config;

		Resource wcd = cs.getConfigDir().getRealResource("web-context-deployment");
		Resource trg = wcd.getRealResource(realpath);
		if (trg.exists()) trg.remove(true);
		Resource p = trg.getParentResource();
		if (!p.isDirectory()) p.createDirectory(true);
		IOUtil.copy(is, trg.getOutputStream(false), closeStream, true);
		filesDeployed.add(trg);
		if (store) _storeAndReload((ConfigImpl) config);
	}

	/*
	 * static Resource[] updateContext(ConfigImpl config,InputStream is,String realpath, boolean
	 * closeStream, boolean store) throws PageException, IOException, SAXException, BundleException {
	 * List<Resource> filesDeployed=new ArrayList<Resource>(); ConfigWebAdmin._updateContext(config, is,
	 * realpath, closeStream, filesDeployed,store); return filesDeployed.toArray(new
	 * Resource[filesDeployed.size()]); }
	 */

	Resource[] updateConfigs(InputStream is, String realpath, boolean closeStream, boolean store) throws PageException, IOException, SAXException, BundleException {
		List<Resource> filesDeployed = new ArrayList<Resource>();
		_updateConfigs(config, is, realpath, closeStream, filesDeployed, store);
		return filesDeployed.toArray(new Resource[filesDeployed.size()]);
	}

	private static void _updateConfigs(Config config, InputStream is, String realpath, boolean closeStream, List<Resource> filesDeployed, boolean store)
			throws PageException, IOException, SAXException, BundleException {
		Resource configs = config.getConfigDir(); // MUST get that dynamically
		Resource trg = configs.getRealResource(realpath);
		if (trg.exists()) trg.remove(true);
		Resource p = trg.getParentResource();
		if (!p.isDirectory()) p.createDirectory(true);
		IOUtil.copy(is, trg.getOutputStream(false), closeStream, true);
		filesDeployed.add(trg);
		if (store) _storeAndReload((ConfigImpl) config);
	}

	Resource[] updateComponent(InputStream is, String realpath, boolean closeStream, boolean store) throws PageException, IOException, SAXException, BundleException {
		List<Resource> filesDeployed = new ArrayList<Resource>();
		_updateComponent(config, is, realpath, closeStream, filesDeployed, store);
		return filesDeployed.toArray(new Resource[filesDeployed.size()]);
	}

	private static void _updateComponent(Config config, InputStream is, String realpath, boolean closeStream, List<Resource> filesDeployed, boolean store)
			throws PageException, IOException, SAXException, BundleException {
		Resource comps = config.getConfigDir().getRealResource("components"); // MUST get that dynamically
		Resource trg = comps.getRealResource(realpath);
		if (trg.exists()) trg.remove(true);
		Resource p = trg.getParentResource();
		if (!p.isDirectory()) p.createDirectory(true);
		IOUtil.copy(is, trg.getOutputStream(false), closeStream, true);
		filesDeployed.add(trg);
		if (store) _storeAndReload((ConfigImpl) config);
	}

	Resource[] updateContext(InputStream is, String realpath, boolean closeStream, boolean store) throws PageException, IOException, SAXException, BundleException {
		List<Resource> filesDeployed = new ArrayList<Resource>();
		_updateContext(config, is, realpath, closeStream, filesDeployed, store);
		return filesDeployed.toArray(new Resource[filesDeployed.size()]);
	}

	private static void _updateContext(Config config, InputStream is, String realpath, boolean closeStream, List<Resource> filesDeployed, boolean store)
			throws PageException, IOException, SAXException, BundleException {
		Resource trg = config.getConfigDir().getRealResource("context").getRealResource(realpath);
		if (trg.exists()) trg.remove(true);
		Resource p = trg.getParentResource();
		if (!p.isDirectory()) p.createDirectory(true);
		IOUtil.copy(is, trg.getOutputStream(false), closeStream, true);
		filesDeployed.add(trg);
		if (store) _storeAndReload((ConfigImpl) config);
	}

	@Deprecated
	static Resource[] updateContextClassic(ConfigImpl config, InputStream is, String realpath, boolean closeStream)
			throws PageException, IOException, SAXException, BundleException {
		List<Resource> filesDeployed = new ArrayList<Resource>();
		XMLConfigAdmin._updateContextClassic(config, is, realpath, closeStream, filesDeployed);
		return filesDeployed.toArray(new Resource[filesDeployed.size()]);
	}

	@Deprecated
	private static void _updateContextClassic(Config config, InputStream is, String realpath, boolean closeStream, List<Resource> filesDeployed)
			throws PageException, IOException, SAXException, BundleException {
		if (config instanceof ConfigServer) {
			ConfigWeb[] webs = ((ConfigServer) config).getConfigWebs();
			if (webs.length == 0) return;
			if (webs.length == 1) {
				_updateContextClassic(webs[0], is, realpath, closeStream, filesDeployed);
				return;
			}
			try {
				byte[] barr = IOUtil.toBytes(is);
				for (int i = 0; i < webs.length; i++) {
					_updateContextClassic(webs[i], new ByteArrayInputStream(barr), realpath, true, filesDeployed);
				}
			}
			finally {
				if (closeStream) IOUtil.close(is);
			}
			return;
		}

		// ConfigWeb
		Resource trg = config.getConfigDir().getRealResource("context").getRealResource(realpath);
		if (trg.exists()) trg.remove(true);
		Resource p = trg.getParentResource();
		if (!p.isDirectory()) p.createDirectory(true);
		IOUtil.copy(is, trg.getOutputStream(false), closeStream, true);
		filesDeployed.add(trg);
		_storeAndReload((ConfigImpl) config);
	}

	public boolean removeConfigs(Config config, boolean store, String... realpathes) throws PageException, IOException, SAXException, BundleException {
		if (ArrayUtil.isEmpty(realpathes)) return false;
		boolean force = false;
		for (int i = 0; i < realpathes.length; i++) {
			if (_removeConfigs(config, realpathes[i], store)) force = true;
		}
		return force;
	}

	private boolean _removeConfigs(Config config, String realpath, boolean _store) throws PageException, IOException, SAXException, BundleException {

		Resource context = config.getConfigDir(); // MUST get dyn
		Resource trg = context.getRealResource(realpath);
		if (trg.exists()) {
			trg.remove(true);
			if (_store) XMLConfigAdmin._storeAndReload((ConfigImpl) config);
			ResourceUtil.removeEmptyFolders(context, null);
			return true;
		}
		return false;
	}

	public boolean removeComponents(Config config, boolean store, String... realpathes) throws PageException, IOException, SAXException, BundleException {
		if (ArrayUtil.isEmpty(realpathes)) return false;
		boolean force = false;
		for (int i = 0; i < realpathes.length; i++) {
			if (_removeComponent(config, realpathes[i], store)) force = true;
		}
		return force;
	}

	private boolean _removeComponent(Config config, String realpath, boolean _store) throws PageException, IOException, SAXException, BundleException {

		Resource context = config.getConfigDir().getRealResource("components"); // MUST get dyn
		Resource trg = context.getRealResource(realpath);
		if (trg.exists()) {
			trg.remove(true);
			if (_store) XMLConfigAdmin._storeAndReload((ConfigImpl) config);
			ResourceUtil.removeEmptyFolders(context, null);
			return true;
		}
		return false;
	}

	public boolean removeContext(Config config, boolean store, Log logger, String... realpathes) throws PageException, IOException, SAXException, BundleException {
		if (ArrayUtil.isEmpty(realpathes)) return false;
		boolean force = false;
		for (int i = 0; i < realpathes.length; i++) {
			logger.log(Log.LEVEL_INFO, "extension", "remove " + realpathes[i]);
			if (_removeContext(config, realpathes[i], store)) force = true;
		}
		return force;
	}

	private boolean _removeContext(Config config, String realpath, boolean _store) throws PageException, IOException, SAXException, BundleException {

		Resource context = config.getConfigDir().getRealResource("context");
		Resource trg = context.getRealResource(realpath);
		if (trg.exists()) {
			trg.remove(true);
			if (_store) XMLConfigAdmin._storeAndReload((ConfigImpl) config);
			ResourceUtil.removeEmptyFolders(context, null);
			return true;
		}
		return false;
	}

	public boolean removeWebContexts(Config config, boolean store, Log logger, String... realpathes) throws PageException, IOException, SAXException, BundleException {
		if (ArrayUtil.isEmpty(realpathes)) return false;

		if (config instanceof ConfigWeb) {
			return removeContext(config, store, logger, realpathes);
		}

		boolean force = false;
		for (int i = 0; i < realpathes.length; i++) {
			logger.log(Log.LEVEL_INFO, "extension", "Remove Context [" + realpathes[i] + "]");
			if (_removeWebContexts(config, realpathes[i], store)) force = true;
		}
		return force;
	}

	private boolean _removeWebContexts(Config config, String realpath, boolean _store) throws PageException, IOException, SAXException, BundleException {

		if (config instanceof ConfigServer) {
			ConfigServer cs = ((ConfigServer) config);

			// remove files from deploy folder
			Resource deploy = cs.getConfigDir().getRealResource("web-context-deployment");
			Resource trg = deploy.getRealResource(realpath);

			if (trg.exists()) {
				trg.remove(true);
				ResourceUtil.removeEmptyFolders(deploy, null);
			}

			// remove files from lucee web context
			boolean store = false;
			ConfigWeb[] webs = cs.getConfigWebs();
			for (int i = 0; i < webs.length; i++) {
				if (_removeContext(webs[i], realpath, _store)) {
					store = true;
				}
			}
			return store;
		}
		return false;
	}

	Resource[] updateApplication(InputStream is, String realpath, boolean closeStream) throws PageException, IOException, SAXException {
		List<Resource> filesDeployed = new ArrayList<Resource>();
		Resource dir;
		// server context
		if (config instanceof ConfigServer) dir = config.getConfigDir().getRealResource("web-deployment");
		// if web context we simply deploy to that webcontext, that's all
		else dir = config.getRootDirectory();

		deployFilesFromStream(config, dir, is, realpath, closeStream, filesDeployed);

		return filesDeployed.toArray(new Resource[filesDeployed.size()]);
	}

	private static void deployFilesFromStream(Config config, Resource root, InputStream is, String realpath, boolean closeStream, List<Resource> filesDeployed)
			throws PageException, IOException, SAXException {
		// MUST this makes no sense at this point
		if (config instanceof ConfigServer) {
			ConfigWeb[] webs = ((ConfigServer) config).getConfigWebs();
			if (webs.length == 0) return;
			if (webs.length == 1) {
				deployFilesFromStream(webs[0], root, is, realpath, closeStream, filesDeployed);
				return;
			}
			try {
				byte[] barr = IOUtil.toBytes(is);
				for (int i = 0; i < webs.length; i++) {
					deployFilesFromStream(webs[i], root, new ByteArrayInputStream(barr), realpath, true, filesDeployed);
				}
			}
			finally {
				if (closeStream) IOUtil.close(is);
			}
			return;
		}

		// ConfigWeb
		Resource trg = root.getRealResource(realpath);
		if (trg.exists()) trg.remove(true);
		Resource p = trg.getParentResource();
		if (!p.isDirectory()) p.createDirectory(true);
		IOUtil.copy(is, trg.getOutputStream(false), closeStream, true);
		filesDeployed.add(trg);
	}

	private void removePlugins(Config config, Log logger, String[] realpathes) throws PageException, IOException, SAXException {
		if (ArrayUtil.isEmpty(realpathes)) return;
		for (int i = 0; i < realpathes.length; i++) {
			logger.log(Log.LEVEL_INFO, "extension", "Remove plugin [" + realpathes[i] + "]");
			removeFiles(config, ((ConfigImpl) config).getPluginDirectory(), realpathes[i]);
		}
	}

	private void removeApplications(Config config, Log logger, String[] realpathes) throws PageException, IOException, SAXException {
		if (ArrayUtil.isEmpty(realpathes)) return;
		for (int i = 0; i < realpathes.length; i++) {
			logger.log(Log.LEVEL_INFO, "extension", "Remove application [" + realpathes[i] + "]");
			removeFiles(config, config.getRootDirectory(), realpathes[i]);
		}
	}

	private void removeFiles(Config config, Resource root, String realpath) throws PageException, IOException, SAXException {
		if (config instanceof ConfigServer) {
			ConfigWeb[] webs = ((ConfigServer) config).getConfigWebs();
			for (int i = 0; i < webs.length; i++) {
				removeFiles(webs[i], root, realpath);
			}
			return;
		}

		// ConfigWeb
		Resource trg = root.getRealResource(realpath);
		if (trg.exists()) trg.remove(true);
	}

	public static void removeRHExtensions(ConfigImpl config, String[] extensionIDs, boolean removePhysical) throws IOException, PageException, SAXException, BundleException {
		XMLConfigAdmin admin = new XMLConfigAdmin(config, null);

		Map<String, BundleDefinition> oldMap = new HashMap<>();
		BundleDefinition[] bds;
		for (String extensionID: extensionIDs) {
			try {
				bds = admin._removeExtension(config, extensionID, removePhysical);
				if (bds != null) {
					for (BundleDefinition bd: bds) {
						if (bd == null) continue;// TODO why are they Null?
						oldMap.put(bd.toString(), bd);
					}
				}
			}
			catch (Exception e) {
				LogUtil.log(config, "deploy", XMLConfigAdmin.class.getName(), e);
			}
		}

		admin._storeAndReload();

		if (!oldMap.isEmpty() && config instanceof ConfigServer) {
			ConfigServer cs = (ConfigServer) config;
			ConfigWeb[] webs = cs.getConfigWebs();
			for (int i = 0; i < webs.length; i++) {
				try {
					admin._storeAndReload((ConfigImpl) webs[i]);
				}
				catch (Exception e) {
					LogUtil.log(config, "deploy", XMLConfigAdmin.class.getName(), e);
				}
			}
		}
		cleanBundles(null, config, oldMap.values().toArray(new BundleDefinition[oldMap.size()])); // clean after populating the new ones

	}

	public BundleDefinition[] _removeExtension(ConfigImpl config, String extensionID, boolean removePhysical) throws IOException, PageException, SAXException, BundleException {
		if (!Decision.isUUId(extensionID)) throw new IOException("id [" + extensionID + "] is invalid, it has to be a UUID");

		Element extensions = _getRootElement("extensions");
		Element[] children = XMLConfigWebFactory.getChildren(extensions, "rhextension");// LuceeHandledExtensions
		// Update
		Element el;
		String id;
		String[] arr;
		boolean storeChildren = false;
		BundleDefinition[] bundles;
		Log log = config.getLog("deploy");
		for (int i = 0; i < children.length; i++) {
			el = children[i];
			id = el.getAttribute("id");
			if (extensionID.equalsIgnoreCase(id)) {
				bundles = RHExtension.toBundleDefinitions(el.getAttribute("bundles")); // get existing bundles before populate new ones

				// bundles
				arr = _removeExtensionCheckOtherUsage(children, el, "bundles");
				// removeBundles(arr,removePhysical);
				// flds
				arr = _removeExtensionCheckOtherUsage(children, el, "flds");
				removeFLDs(log, arr);
				// tlds
				arr = _removeExtensionCheckOtherUsage(children, el, "tlds");
				removeTLDs(log, arr);
				// contexts
				arr = _removeExtensionCheckOtherUsage(children, el, "contexts");
				storeChildren = removeContext(config, false, log, arr);

				// webcontexts
				arr = _removeExtensionCheckOtherUsage(children, el, "webcontexts");
				storeChildren = removeWebContexts(config, false, log, arr);

				// applications
				arr = _removeExtensionCheckOtherUsage(children, el, "applications");
				removeApplications(config, log, arr);

				// components
				arr = _removeExtensionCheckOtherUsage(children, el, "components");
				removeComponents(config, false, arr);

				// configs
				arr = _removeExtensionCheckOtherUsage(children, el, "config");
				removeConfigs(config, false, arr);

				// plugins
				arr = _removeExtensionCheckOtherUsage(children, el, "plugins");
				removePlugins(config, log, arr);

				extensions.removeChild(el);

				return bundles;
			}
		}
		return null;
	}

	public static void cleanBundles(RHExtension rhe, ConfigImpl config, BundleDefinition[] candiatesToRemove) throws BundleException, ApplicationException, IOException {
		if (ArrayUtil.isEmpty(candiatesToRemove)) return;

		BundleCollection coreBundles = ConfigWebUtil.getEngine(config).getBundleCollection();

		// core master
		_cleanBundles(candiatesToRemove, coreBundles.core.getSymbolicName(), coreBundles.core.getVersion());

		// core slaves
		Iterator<Bundle> it = coreBundles.getSlaves();
		Bundle b;
		while (it.hasNext()) {
			b = it.next();
			_cleanBundles(candiatesToRemove, b.getSymbolicName(), b.getVersion());
		}

		// all extension
		Iterator<RHExtension> itt = config.getAllRHExtensions().iterator();
		RHExtension _rhe;
		while (itt.hasNext()) {
			_rhe = itt.next();
			if (rhe != null && rhe.equals(_rhe)) continue;
			BundleInfo[] bundles = _rhe.getBundles(null);
			if (bundles != null) {
				for (BundleInfo bi: bundles) {
					_cleanBundles(candiatesToRemove, bi.getSymbolicName(), bi.getVersion());
				}
			}
		}

		// now we only have BundlesDefs in the array no longer used
		for (BundleDefinition ctr: candiatesToRemove) {
			if (ctr != null) OSGiUtil.removeLocalBundleSilently(ctr.getName(), ctr.getVersion(), null, true);
		}
	}

	private static void _cleanBundles(BundleDefinition[] candiatesToRemove, String name, Version version) {
		BundleDefinition bd;
		for (int i = 0; i < candiatesToRemove.length; i++) {
			bd = candiatesToRemove[i];

			if (bd != null && name.equalsIgnoreCase(bd.getName())) {
				if (version == null) {
					if (bd.getVersion() == null) candiatesToRemove[i] = null; // remove that from array
				}
				else if (bd.getVersion() != null && version.equals(bd.getVersion())) {
					candiatesToRemove[i] = null; // remove that from array
				}
			}
		}
	}

	private String[] _removeExtensionCheckOtherUsage(Element[] children, Element curr, String type) {
		String currVal = curr.getAttribute(type);
		if (StringUtil.isEmpty(currVal)) return null;

		String otherVal;
		Element other;
		Set<String> currSet = ListUtil.toSet(ListUtil.trimItems(ListUtil.listToStringArray(currVal, ',')));
		String[] otherArr;
		for (int i = 0; i < children.length; i++) {
			other = children[i];
			if (other == curr) continue;
			otherVal = other.getAttribute(type);
			if (StringUtil.isEmpty(otherVal)) continue;
			otherArr = ListUtil.trimItems(ListUtil.listToStringArray(otherVal, ','));
			for (int y = 0; y < otherArr.length; y++) {
				currSet.remove(otherArr[y]);
			}
		}
		return currSet.toArray(new String[currSet.size()]);
	}

	/**
	 * 
	 * @param config
	 * @param ext
	 * @return the bundles used before when this was a update, if it is a new extension then null is
	 *         returned
	 * @throws IOException
	 * @throws BundleException
	 * @throws ApplicationException
	 */
	public BundleDefinition[] _updateExtension(ConfigImpl config, RHExtension ext) throws IOException, BundleException, ApplicationException {
		if (!Decision.isUUId(ext.getId())) throw new IOException("id [" + ext.getId() + "] is invalid, it has to be a UUID");
		Element extensions = _getRootElement("extensions");
		Element[] children = XMLConfigWebFactory.getChildren(extensions, "rhextension");// LuceeHandledExtensions

		// Update
		Element el;
		String id;
		BundleDefinition[] old;
		for (int i = 0; i < children.length; i++) {
			el = children[i];
			// provider=el.getAttribute("provider");
			id = el.getAttribute("id");
			if (ext.getId().equalsIgnoreCase(id)) {
				old = RHExtension.toBundleDefinitions(el.getAttribute("bundles")); // get existing bundles before populate new ones
				ext.populate(el);
				old = minus(old, OSGiUtil.toBundleDefinitions(ext.getBundles()));
				return old;
			}
		}

		// Insert
		el = doc.createElement("rhextension");
		ext.populate(el);
		extensions.appendChild(el);
		return null;
	}

	private BundleDefinition[] minus(BundleDefinition[] oldBD, BundleDefinition[] newBD) {
		List<BundleDefinition> list = new ArrayList<>();
		boolean has;
		for (BundleDefinition o: oldBD) {
			has = false;
			for (BundleDefinition n: newBD) {
				if (o.equals(n)) {
					has = true;
					break;
				}
			}
			if (!has) list.add(o);
		}
		return list.toArray(new BundleDefinition[list.size()]);
	}

	private RHExtension getRHExtension(ConfigImpl config, String id, RHExtension defaultValue) {
		Element extensions = _getRootElement("extensions");
		Element[] children = XMLConfigWebFactory.getChildren(extensions, "rhextension");// LuceeHandledExtensions

		if (children != null) for (int i = 0; i < children.length; i++) {
			if (!id.equals(children[i].getAttribute("id"))) continue;
			try {
				return new RHExtension(config, children[i]);
			}
			catch (Exception e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	/**
	 * returns the version if the extension is available
	 * 
	 * @param config
	 * @param id
	 * @return
	 * @throws PageException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static RHExtension hasRHExtensions(ConfigImpl config, ExtensionDefintion ed) throws PageException, SAXException, IOException {
		XMLConfigAdmin admin = new XMLConfigAdmin(config, null);
		return admin._hasRHExtensions(config, ed);
	}

	private RHExtension _hasRHExtensions(ConfigImpl config, ExtensionDefintion ed) throws PageException {

		Element extensions = _getRootElement("extensions");
		Element[] children = XMLConfigWebFactory.getChildren(extensions, "rhextension");// LuceeHandledExtensions
		RHExtension tmp;
		try {
			for (int i = 0; i < children.length; i++) {
				tmp = null;
				try {
					tmp = new RHExtension(config, children[i]);
				}
				catch (Exception e) {}

				if (tmp != null && ed.equals(tmp)) return tmp;
			}
			return null;
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	public void updateAuthKey(String key) throws PageException {
		checkWriteAccess();
		key = key.trim();

		// merge new key and existing
		ConfigServerImpl cs = (ConfigServerImpl) config;
		String[] keys = cs.getAuthenticationKeys();
		Set<String> set = new HashSet<String>();
		for (int i = 0; i < keys.length; i++) {
			set.add(keys[i]);
		}
		set.add(key);

		Element root = doc.getDocumentElement();
		root.setAttribute("auth-keys", authKeysAsList(set));

	}

	public void removeAuthKeys(String key) throws PageException {
		checkWriteAccess();
		key = key.trim();

		// remove key
		ConfigServerImpl cs = (ConfigServerImpl) config;
		String[] keys = cs.getAuthenticationKeys();
		Set<String> set = new HashSet<String>();
		for (int i = 0; i < keys.length; i++) {
			if (!key.equals(keys[i])) set.add(keys[i]);
		}

		Element root = doc.getDocumentElement();
		root.setAttribute("auth-keys", authKeysAsList(set));
	}

	public void updateAPIKey(String key) throws SecurityException, ApplicationException {
		checkWriteAccess();
		key = key.trim();
		if (!Decision.isGUId(key)) throw new ApplicationException("Passed API Key [" + key + "] is not valid");
		Element root = doc.getDocumentElement();
		root.setAttribute("api-key", key);

	}

	public void removeAPIKey() throws PageException {
		checkWriteAccess();
		Element root = doc.getDocumentElement();
		if (root.hasAttribute("api-key")) root.removeAttribute("api-key");
	}

	private String authKeysAsList(Set<String> set) throws PageException {
		StringBuilder sb = new StringBuilder();
		Iterator<String> it = set.iterator();
		String key;
		while (it.hasNext()) {
			key = it.next().trim();
			if (sb.length() > 0) sb.append(',');
			try {
				sb.append(URLEncoder.encode(key, "UTF-8"));
			}
			catch (UnsupportedEncodingException e) {
				throw Caster.toPageException(e);
			}
		}
		return sb.toString();
	}

	/*
	 * static Resource[] updatePlugin(ConfigImpl config,InputStream is,String realpath, boolean
	 * closeStream) throws PageException, IOException, SAXException { ConfigWebAdmin admin = new
	 * ConfigWebAdmin(config, null); List<Resource> filesDeployed=new ArrayList<Resource>();
	 * admin.deployFilesFromStream(config,config.getPluginDirectory(), is, realpath, closeStream,
	 * filesDeployed); return filesDeployed.toArray(new Resource[filesDeployed.size()]); }
	 */

	Resource[] updatePlugin(InputStream is, String realpath, boolean closeStream) throws PageException, IOException, SAXException {
		List<Resource> filesDeployed = new ArrayList<Resource>();
		deployFilesFromStream(config, config.getPluginDirectory(), is, realpath, closeStream, filesDeployed);
		return filesDeployed.toArray(new Resource[filesDeployed.size()]);
	}

	public void updatePlugin(PageContext pc, Resource src) throws PageException, IOException {
		// convert to a directory when it is a zip
		if (!src.isDirectory()) {
			if (!IsZipFile.invoke(src))
				throw new ApplicationException("Path [" + src.getAbsolutePath() + "] is invalid, it has to be a path to an existing zip file or a directory containing a plugin");
			src = ResourceUtil.toResourceExisting(pc, "zip://" + src.getAbsolutePath());
		}
		String name = ResourceUtil.getName(src.getName());
		if (!PluginFilter.doAccept(src)) throw new ApplicationException("Plugin [" + src.getAbsolutePath() + "] is invalid, missing one of the following files [Action."
				+ Constants.getCFMLComponentExtension() + " or Action." + Constants.getLuceeComponentExtension() + ",language.xml] in root, existing files are ["
				+ lucee.runtime.type.util.ListUtil.arrayToList(src.list(), ", ") + "]");

		Resource dir = config.getPluginDirectory();
		Resource trgDir = dir.getRealResource(name);
		if (trgDir.exists()) {
			trgDir.remove(true);
		}

		ResourceUtil.copyRecursive(src, trgDir);
	}

	private static void setClass(Element el, Class instanceOfClass, String prefix, ClassDefinition cd) throws PageException {
		if (cd == null || StringUtil.isEmpty(cd.getClassName())) return;

		// validate class
		try {
			Class clazz = cd.getClazz();

			if (instanceOfClass != null && !Reflector.isInstaneOf(clazz, instanceOfClass, false))
				throw new ApplicationException("Class [" + clazz.getName() + "] is not of type [" + instanceOfClass.getName() + "]");
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		el.setAttribute(prefix + "class", cd.getClassName().trim());
		if (cd.isBundle()) {
			el.setAttribute(prefix + "bundle-name", cd.getName());
			if (cd.hasVersion()) el.setAttribute(prefix + "bundle-version", cd.getVersionAsString());
		}
		else {
			if (el.hasAttribute(prefix + "bundle-name")) el.removeAttribute(prefix + "bundle-name");
			if (el.hasAttribute(prefix + "bundle-version")) el.removeAttribute(prefix + "bundle-version");
		}
	}

	private void removeClass(Element el, String prefix) {
		el.removeAttribute(prefix + "class");
		el.removeAttribute(prefix + "bundle-name");
		el.removeAttribute(prefix + "bundle-version");
	}

	public final static class PluginFilter implements ResourceFilter {
		@Override
		public boolean accept(Resource res) {
			return doAccept(res);
		}

		public static boolean doAccept(Resource res) {
			return res.isDirectory() && (res.getRealResource("/Action." + Constants.getCFMLComponentExtension()).isFile()
					|| res.getRealResource("/Action." + Constants.getLuceeComponentExtension()).isFile()) && res.getRealResource("/language.xml").isFile();
		}

	}

	public void updateQueue(Integer max, Integer timeout, Boolean enable) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("Accces Denied to update queue settings");

		Element queue = _getRootElement("queue");
		// max
		if (max == null) queue.removeAttribute("max");
		else queue.setAttribute("max", Caster.toString(max, ""));
		// total
		if (timeout == null) queue.removeAttribute("timeout");
		else queue.setAttribute("timeout", Caster.toString(timeout, ""));
		// enable
		if (enable == null) queue.removeAttribute("enable");
		else queue.setAttribute("enable", Caster.toString(enable, ""));
	}

	public void updateCGIReadonly(Boolean cgiReadonly) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("Accces Denied to update scope setting");

		Element scope = _getRootElement("scope");
		scope.setAttribute("cgi-readonly", Caster.toString(cgiReadonly, ""));
	}

	public static boolean fixExtension(Config config, Document doc) {
		Element parent = XMLConfigWebFactory.getChildByName(doc.getDocumentElement(), "extensions", false, true);
		Element[] extensions = XMLConfigWebFactory.getChildren(parent, "rhextension");

		// replace extension class with core class
		boolean fixed = false;
		if (extensions != null) {
			for (int i = 0; i < extensions.length; i++) {
				if (extensions[i].hasAttribute("start-bundles")) continue;
				// this will load the data from the .lex file
				try {

					Resource res = RHExtension.toResource(config, extensions[i], null);
					Manifest mf = (res == null) ? null : RHExtension.getManifestFromFile(config, res);
					if (mf != null) {
						RHExtension.populate(extensions[i], mf);
						fixed = true;
					}
				}
				catch (Exception e) {
					LogUtil.log(ThreadLocalPageContext.getConfig(config), XMLConfigAdmin.class.getName(), e);
				}
			}
		}
		return fixed;
	}
}
