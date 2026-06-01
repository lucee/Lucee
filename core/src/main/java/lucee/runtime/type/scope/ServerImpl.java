/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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
import lucee.transformer.bytecode.util.ASMUtil;

/**
 * Server Scope
 */
public final class ServerImpl extends ScopeSupport implements Server, SharedScope {

	private static final DateTimeImpl expired = new DateTimeImpl(2145913200000L);

	// private static final Key JAVA_AGENT_PATH = KeyConstants._javaAgentPath;
	private static final Key RELEASE_DATE = KeyImpl.getInstance("release-date");
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
		coldfusion.setEL(KeyConstants._productlevel, info.getLevel());
		// coldfusion.setEL(KeyConstants._productversion,"11,0,07,296330");
		coldfusion.setEL(KeyConstants._productversion, "2016,0,03,300357");
		coldfusion.setEL(KeyConstants._serialnumber, "0");
		coldfusion.setEL(KeyConstants._productname, "Lucee");

		// TODO scope server missing values
		coldfusion.setEL(KeyConstants._appserver, "");// Jrun
		coldfusion.setEL(KeyConstants._expiration, expired);//
		coldfusion.setEL(KeyConstants._installkit, "");//

		String rootdir = "";
		try {

			rootdir = ThreadLocalPageContext.getConfigWeb(pc).getRootDirectory().getAbsolutePath();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		coldfusion.setEL(KeyConstants._rootdir, rootdir);//

		coldfusion.setEL(KeyConstants._supportedlocales, LocaleFactory.getLocaleList());//

		coldfusion.setReadOnly(true);
		super.setEL(KeyConstants._coldfusion, coldfusion);

		ReadOnlyStruct os = new ReadOnlyStruct();
		os.setEL(KeyConstants._name, System.getProperty("os.name"));
		os.setEL(KeyConstants._arch, System.getProperty("os.arch"));
		os.setEL(KeyConstants._macAddress, SystemUtil.getMacAddressAsWrap());
		int arch = SystemUtil.getOSArch();
		if (arch != SystemUtil.ARCH_UNKNOW) os.setEL(KeyConstants._archModel, Double.valueOf(arch));
		os.setEL(KeyConstants._version, System.getProperty("os.version"));
		os.setEL(KeyConstants._additionalinformation, "");
		os.setEL(KeyConstants._buildnumber, "");
		os.setEL(KeyConstants._hostname, SystemUtil.getLocalHostName());

		os.setReadOnly(true);
		super.setEL(KeyConstants._os, os);

		ReadOnlyStruct lucee = new ReadOnlyStruct();
		lucee.setEL(KeyConstants._version, info.getVersion().toString());
		lucee.setEL(KeyConstants._versionName, info.getVersionName());
		lucee.setEL(KeyConstants._versionNameExplanation, info.getVersionNameExplanation());
		lucee.setEL(KeyConstants._state, getStateAsString(info.getVersion()));
		lucee.setEL(RELEASE_DATE, new DateTimeImpl(info.getRealeaseTime()));
		lucee.setEL(KeyConstants._loaderVersion, Caster.toDouble(SystemUtil.getLoaderVersion()));
		lucee.setEL(KeyConstants._loaderPath, ClassUtil.getSourcePathForClass(pc, "lucee.loader.servlet.CFMLServlet", ""));
		lucee.setEL(KeyConstants._environment, jsr223 != null && jsr223.booleanValue() ? "jsr223" : "servlet");

		// singleContext admin Mode
		lucee.setEL(KeyConstants._singleContext, Boolean.TRUE);

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

		java.setEL(KeyConstants._javaCompilerVersion, ASMUtil.toStringVersion(ASMUtil.getJavaVersionForBytecodeGeneration()));
		java.setEL(KeyConstants._vendor, System.getProperty("java.vendor"));
		arch = SystemUtil.getJREArch();
		if (arch != SystemUtil.ARCH_UNKNOW) java.setEL(KeyConstants._archModel, Double.valueOf(arch));
		Runtime rt = Runtime.getRuntime();
		java.setEL(KeyConstants._freeMemory, Double.valueOf(rt.freeMemory()));
		java.setEL(KeyConstants._totalMemory, Double.valueOf(rt.totalMemory()));
		java.setEL(KeyConstants._maxMemory, Double.valueOf(rt.maxMemory()));
		java.setEL(KeyConstants._javaAgentSupported, Boolean.TRUE);

		if (jep == null) {
			String temp = System.getProperty("user.dir", "");
			if (!StringUtil.isEmpty(temp) && !temp.endsWith(File.separator)) temp = temp + File.separator;
			jep = temp;
		}
		java.setEL(KeyConstants._executionPath, jep);

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