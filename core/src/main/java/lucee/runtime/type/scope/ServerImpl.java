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
package lucee.runtime.type.scope;

import java.io.File;

import org.osgi.framework.Version;

import lucee.Info;
import lucee.commons.io.SystemUtil;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.i18n.LocaleFactory;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.ReadOnlyStruct;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.scope.util.EnvStruct;
import lucee.runtime.type.scope.util.SystemPropStruct;
import lucee.runtime.type.util.KeyConstants;

/**
 * Server Scope
 */
public final class ServerImpl extends ScopeSupport implements Server, SharedScope {

	private static final DateTimeImpl expired = new DateTimeImpl(2145913200000L, false);

	private static final Key PRODUCT_NAME = KeyImpl.getInstance("productname");
	private static final Key PRODUCT_LEVEL = KeyImpl.getInstance("productlevel");
	private static final Key PRODUCT_VERSION = KeyImpl.getInstance("productversion");
	private static final Key SERIAL_NUMBER = KeyImpl.getInstance("serialnumber");
	private static final Key EXPIRATION = KeyImpl.getInstance("expiration");
	private static final Key INSTALL_KIT = KeyImpl.getInstance("installkit");
	private static final Key ROOT_DIR = KeyImpl.getInstance("rootdir");
	private static final Key SUPPORTED_LOCALES = KeyImpl.getInstance("supportedlocales");
	private static final Key ARCH = KeyImpl.getInstance("arch");
	private static final Key MAC_ADDRESS = KeyImpl.getInstance("macAddress");
	private static final Key ARCH_MODEL = KeyImpl.getInstance("archModel");
	// private static final Key JAVA_AGENT_PATH = KeyImpl.getInstance("javaAgentPath");
	private static final Key JAVA_EXECUTION_PATH = KeyImpl.getInstance("executionPath");
	private static final Key JAVA_AGENT_SUPPORTED = KeyImpl.getInstance("javaAgentSupported");
	private static final Key LOADER_VERSION = KeyImpl.getInstance("loaderVersion");
	private static final Key LOADER_PATH = KeyImpl.getInstance("loaderPath");
	private static final Key ADDITIONAL_INFORMATION = KeyImpl.getInstance("additionalinformation");
	private static final Key BUILD_NUMBER = KeyImpl.getInstance("buildnumber");
	private static final Key RELEASE_DATE = KeyImpl.getInstance("release-date");
	private static final Key VENDOR = KeyImpl.getInstance("vendor");
	private static final Key FREE_MEMORY = KeyImpl.getInstance("freeMemory");
	private static final Key MAX_MEMORY = KeyImpl.getInstance("maxMemory");
	private static final Key TOTAL_MEMORY = KeyImpl.getInstance("totalMemory");
	private static final Key VERSION_NAME = KeyImpl.getInstance("versionName");
	private static final Key VERSION_NAME_EXPLANATION = KeyImpl.getInstance("versionNameExplanation");
	private static final Key HOST_NAME = KeyImpl.getInstance("hostname");
	private static final Key ENVIRONMENT = KeyConstants._environment;

	private static String jep;

	/*
	 * Supported CFML Application
	 * 
	 * Blog - http://www.blogcfm.org
	 * 
	 * 
	 * 
	 */
	/**
	 * constructor of the server scope
	 * 
	 * @param pc
	 */
	public ServerImpl(PageContext pc, boolean jsr223) {
		super("server", SCOPE_SERVER, Struct.TYPE_LINKED);
		reload(pc, jsr223);

	}

	@Override
	public void reload() {
		reload(ThreadLocalPageContext.get());
	}

	public void reload(PageContext pc) {

	}

	public void reload(PageContext pc, Boolean jsr223) {
		Info info = pc.getConfig().getFactory().getEngine().getInfo();
		ReadOnlyStruct coldfusion = new ReadOnlyStruct();
		coldfusion.setEL(PRODUCT_LEVEL, info.getLevel());
		// coldfusion.setEL(PRODUCT_VERSION,"11,0,07,296330");
		coldfusion.setEL(PRODUCT_VERSION, "2016,0,03,300357");
		coldfusion.setEL(SERIAL_NUMBER, "0");
		coldfusion.setEL(PRODUCT_NAME, "Lucee");

		// TODO scope server missing values
		coldfusion.setEL(KeyConstants._appserver, "");// Jrun
		coldfusion.setEL(EXPIRATION, expired);//
		coldfusion.setEL(INSTALL_KIT, "");//

		String rootdir = "";
		try {
			rootdir = ThreadLocalPageContext.getConfig(pc).getRootDirectory().getAbsolutePath();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		coldfusion.setEL(ROOT_DIR, rootdir);//

		coldfusion.setEL(SUPPORTED_LOCALES, LocaleFactory.getLocaleList());//

		coldfusion.setReadOnly(true);
		super.setEL(KeyConstants._coldfusion, coldfusion);

		ReadOnlyStruct os = new ReadOnlyStruct();
		os.setEL(KeyConstants._name, System.getProperty("os.name"));
		os.setEL(ARCH, System.getProperty("os.arch"));
		os.setEL(MAC_ADDRESS, SystemUtil.getMacAddressAsWrap());
		int arch = SystemUtil.getOSArch();
		if (arch != SystemUtil.ARCH_UNKNOW) os.setEL(ARCH_MODEL, new Double(arch));
		os.setEL(KeyConstants._version, System.getProperty("os.version"));
		os.setEL(ADDITIONAL_INFORMATION, "");
		os.setEL(BUILD_NUMBER, "");
		os.setEL(HOST_NAME, SystemUtil.getLocalHostName());

		os.setReadOnly(true);
		super.setEL(KeyConstants._os, os);

		ReadOnlyStruct lucee = new ReadOnlyStruct();
		lucee.setEL(KeyConstants._version, info.getVersion().toString());
		lucee.setEL(VERSION_NAME, info.getVersionName());
		lucee.setEL(VERSION_NAME_EXPLANATION, info.getVersionNameExplanation());
		lucee.setEL(KeyConstants._state, getStateAsString(info.getVersion()));
		lucee.setEL(RELEASE_DATE, new DateTimeImpl(info.getRealeaseTime(), false));
		lucee.setEL(LOADER_VERSION, Caster.toDouble(SystemUtil.getLoaderVersion()));
		lucee.setEL(LOADER_PATH, ClassUtil.getSourcePathForClass("lucee.loader.servlet.CFMLServlet", ""));
		lucee.setEL(ENVIRONMENT, jsr223 != null && jsr223.booleanValue() ? "jsr223" : "servlet");

		lucee.setReadOnly(true);
		super.setEL(KeyConstants._lucee, lucee);

		ReadOnlyStruct separator = new ReadOnlyStruct();
		separator.setEL(KeyConstants._path, System.getProperty("path.separator"));
		separator.setEL(KeyConstants._file, System.getProperty("file.separator"));
		separator.setEL(KeyConstants._line, System.getProperty("line.separator"));
		separator.setReadOnly(true);
		super.setEL(KeyConstants._separator, separator);

		ReadOnlyStruct java = new ReadOnlyStruct();
		java.setEL(KeyConstants._version, System.getProperty("java.version"));
		java.setEL(VENDOR, System.getProperty("java.vendor"));
		arch = SystemUtil.getJREArch();
		if (arch != SystemUtil.ARCH_UNKNOW) java.setEL(ARCH_MODEL, new Double(arch));
		Runtime rt = Runtime.getRuntime();
		java.setEL(FREE_MEMORY, new Double(rt.freeMemory()));
		java.setEL(TOTAL_MEMORY, new Double(rt.totalMemory()));
		java.setEL(MAX_MEMORY, new Double(rt.maxMemory()));
		java.setEL(JAVA_AGENT_SUPPORTED, Boolean.TRUE);

		if (jep == null) {
			String temp = System.getProperty("user.dir", "");
			if (!StringUtil.isEmpty(temp) && !temp.endsWith(File.separator)) temp = temp + File.separator;
			jep = temp;
		}
		java.setEL(JAVA_EXECUTION_PATH, jep);

		java.setReadOnly(true);
		super.setEL(KeyConstants._java, java);

		ReadOnlyStruct servlet = new ReadOnlyStruct();
		String name = "";
		try {
			name = pc.getServletContext().getServerInfo();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		servlet.setEL(KeyConstants._name, name);
		servlet.setReadOnly(true);

		super.setEL(KeyConstants._servlet, servlet);

		ReadOnlyStruct system = new ReadOnlyStruct();
		system.setEL(KeyConstants._properties, SystemPropStruct.getInstance());
		system.setEL(KeyConstants._environment, EnvStruct.getInstance());
		system.setReadOnly(true);
		super.setEL(KeyConstants._system, system);

	}

	private static String getStateAsString(Version version) {
		String q = version.getQualifier();
		int index = q.indexOf('-');
		if (index == -1) return "stable";
		return q.substring(index + 1);
	}

	@Override
	public Object set(Collection.Key key, Object value) throws PageException {
		if (isReadOnlyKey(key)) throw new ExpressionException("Key [" + key + "] in Server scope is read-only and can not be modified");
		return super.set(key, value);
	}

	@Override
	public Object setEL(Collection.Key key, Object value) {
		if (!isReadOnlyKey(key)) return super.setEL(key, value);
		return value;
	}

	@Override
	public Object get(Key key, Object defaultValue) {
		if (key.equalsIgnoreCase(KeyConstants._railo)) return super.get(KeyConstants._lucee, defaultValue);
		return super.get(key, defaultValue);
	}

	@Override
	public Object g(Key key, Object defaultValue) {
		if (key.equalsIgnoreCase(KeyConstants._railo)) return super.g(KeyConstants._lucee, defaultValue);
		return super.g(key, defaultValue);
	}

	@Override
	public Object g(Key key) throws PageException {
		if (key.equalsIgnoreCase(KeyConstants._railo)) return super.g(KeyConstants._lucee);
		return super.g(key);
	}

	@Override
	public Object get(Key key) throws PageException {
		if (key.equalsIgnoreCase(KeyConstants._railo)) return super.get(KeyConstants._lucee);
		return super.get(key);
	}

	@Override
	public Object get(PageContext pc, Key key) throws PageException {
		if (key.equalsIgnoreCase(KeyConstants._railo)) return super.get(pc, KeyConstants._lucee);
		return super.get(pc, key);
	}

	/**
	 * returns if the key is a readonly key
	 * 
	 * @param key key to check
	 * @return is readonly
	 */
	private boolean isReadOnlyKey(Collection.Key key) {

		return (key.equals(KeyConstants._java) || key.equals(KeyConstants._separator) || key.equals(KeyConstants._os) || key.equals(KeyConstants._coldfusion)
				|| key.equals(KeyConstants._lucee));
	}

	@Override
	public void touchBeforeRequest(PageContext pc) {
		// do nothing
	}

	@Override
	public void touchAfterRequest(PageContext pc) {
		// do nothing
	}
}