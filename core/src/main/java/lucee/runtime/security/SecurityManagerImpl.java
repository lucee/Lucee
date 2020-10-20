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
package lucee.runtime.security;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.type.file.FileResourceProvider;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.config.Password;
import lucee.runtime.config.PasswordImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.SecurityException;
import lucee.runtime.type.util.ArrayUtil;

/**
 * SecurityManager to control access to different services
 */
public final class SecurityManagerImpl implements Cloneable, SecurityManager {

	private static final Resource[] EMPTY_RESOURCE_ARRAY = new Resource[0];

	private short[] accesses = new short[22];
	private Resource rootDirectory;
	private Resource[] customFileAccess = EMPTY_RESOURCE_ARRAY;

	private SecurityManagerImpl() {}

	/**
	 * create a new Accessor
	 * 
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
	 * @param t
	 * @param accessRead
	 */
	public SecurityManagerImpl(short setting, short file, short directJavaAccess, short mail, short datasource, short mapping, short remote, short customTag, short cfxSetting,
			short cfxUsage, short debugging, short search, short scheduledTasks, short tagExecute, short tagImport, short tagObject, short tagRegistry, short cache, short gateway,
			short orm, short accessRead, short accessWrite) {
		accesses[TYPE_SETTING] = setting;
		accesses[TYPE_FILE] = file;
		accesses[TYPE_DIRECT_JAVA_ACCESS] = directJavaAccess;
		accesses[TYPE_MAIL] = mail;
		accesses[TYPE_DATASOURCE] = datasource;
		accesses[TYPE_MAPPING] = mapping;
		accesses[TYPE_CUSTOM_TAG] = customTag;
		accesses[TYPE_CFX_SETTING] = cfxSetting;
		accesses[TYPE_CFX_USAGE] = cfxUsage;
		accesses[TYPE_DEBUGGING] = debugging;
		accesses[TYPE_SEARCH] = search;
		accesses[TYPE_SCHEDULED_TASK] = scheduledTasks;

		accesses[TYPE_TAG_EXECUTE] = tagExecute;
		accesses[TYPE_TAG_IMPORT] = tagImport;
		accesses[TYPE_TAG_OBJECT] = tagObject;
		accesses[TYPE_TAG_REGISTRY] = tagRegistry;
		accesses[TYPE_CACHE] = cache;
		accesses[TYPE_GATEWAY] = gateway;
		accesses[TYPE_ORM] = orm;
		accesses[TYPE_ACCESS_READ] = accessRead;
		accesses[TYPE_ACCESS_WRITE] = accessWrite;
		accesses[TYPE_REMOTE] = remote;

	}

	/**
	 * @return return default accessor (no restriction)
	 */
	public static SecurityManager getOpenSecurityManager() {
		return new SecurityManagerImpl(VALUE_YES, // Setting
				VALUE_ALL, // File
				VALUE_YES, // Direct Java Access
				VALUE_YES, // Mail
				VALUE_YES, // Datasource
				VALUE_YES, // Mapping
				VALUE_YES, // Remote
				VALUE_YES, // Custom tag
				VALUE_YES, // CFX Setting
				VALUE_YES, // CFX Usage
				VALUE_YES, // Debugging
				VALUE_YES, // Search
				VALUE_YES, // Scheduled Tasks
				VALUE_YES, // Tag Execute
				VALUE_YES, // Tag Import
				VALUE_YES, // Tag Object
				VALUE_YES, // Tag Registry
				VALUE_YES, // Cache
				VALUE_YES, // Gateway
				VALUE_YES, // ORM
				ACCESS_OPEN, ACCESS_PROTECTED);

	}

	@Override
	public short getAccess(int access) {
		return accesses[access];
	}

	public void setAccess(int access, short value) {
		accesses[access] = value;
	}

	@Override
	public short getAccess(String access) throws SecurityException {
		return getAccess(toIntAccessType(access));
	}

	/**
	 * translate a string access type (cfx,file ...) to int type
	 * 
	 * @param accessType
	 * @return return access value (all,local,none ...) for given type (cfx,file ...)
	 * @throws SecurityException
	 */
	private static int toIntAccessType(String accessType) throws SecurityException {
		accessType = accessType.trim().toLowerCase();
		if (accessType.equals("setting")) return TYPE_SETTING;
		else if (accessType.equals("file")) return TYPE_FILE;
		else if (accessType.equals("direct_java_access")) return TYPE_DIRECT_JAVA_ACCESS;
		// else if(accessType.equals("search")) return TYPE_SEARCH;
		else if (accessType.equals("mail")) return TYPE_MAIL;
		// else if(accessType.equals("scheduled_task")) return TYPE_SCHEDULED_TASK;
		else if (accessType.equals("datasource")) return TYPE_DATASOURCE;
		else if (accessType.equals("mapping")) return TYPE_MAPPING;
		else if (accessType.equals("remote")) return TYPE_REMOTE;
		else if (accessType.equals("custom_tag")) return TYPE_CUSTOM_TAG;
		else if (accessType.equals("cfx_setting")) return TYPE_CFX_SETTING;
		else if (accessType.equals("cfx_usage")) return TYPE_CFX_USAGE;
		else if (accessType.equals("debugging")) return TYPE_DEBUGGING;
		else if (accessType.equals("tag_execute")) return TYPE_TAG_EXECUTE;
		else if (accessType.equals("tag_import")) return TYPE_TAG_IMPORT;
		else if (accessType.equals("tag_object")) return TYPE_TAG_OBJECT;
		else if (accessType.equals("tag_registry")) return TYPE_TAG_REGISTRY;
		else if (accessType.equals("search")) return TYPE_SEARCH;
		else if (accessType.equals("cache")) return TYPE_CACHE;
		else if (accessType.equals("gateway")) return TYPE_GATEWAY;
		else if (accessType.equals("orm")) return TYPE_ORM;
		else if (accessType.startsWith("scheduled_task")) return TYPE_SCHEDULED_TASK;
		else throw new SecurityException("invalid access type [" + accessType + "]",
				"valid access types are [setting,file,direct_java_access,mail,datasource,mapping,custom_tag,cfx_setting" + "cfx_usage,debugging]");

	}

	/**
	 * translate a string access value (all,local,none,no,yes) to int type
	 * 
	 * @param accessValue
	 * @return return int access value (VALUE_ALL,VALUE_LOCAL,VALUE_NO,VALUE_NONE,VALUE_YES)
	 * @throws SecurityException
	 */
	public static short toShortAccessValue(String accessValue) throws SecurityException {
		accessValue = accessValue.trim().toLowerCase();
		if (accessValue.equals("all")) return VALUE_ALL;
		else if (accessValue.equals("local")) return VALUE_LOCAL;
		else if (accessValue.equals("none")) return VALUE_NONE;
		else if (accessValue.equals("no")) return VALUE_NO;
		else if (accessValue.equals("yes")) return VALUE_YES;
		else if (accessValue.equals("1")) return VALUE_1;
		else if (accessValue.equals("2")) return VALUE_2;
		else if (accessValue.equals("3")) return VALUE_3;
		else if (accessValue.equals("4")) return VALUE_4;
		else if (accessValue.equals("5")) return VALUE_5;
		else if (accessValue.equals("6")) return VALUE_6;
		else if (accessValue.equals("7")) return VALUE_7;
		else if (accessValue.equals("8")) return VALUE_8;
		else if (accessValue.equals("9")) return VALUE_9;
		else if (accessValue.equals("10")) return VALUE_10;
		else throw new SecurityException("invalid access value [" + accessValue + "]", "valid access values are [all,local,no,none,yes,1,...,10]");

	}

	public static short toShortAccessRWValue(String accessValue) throws SecurityException {
		accessValue = accessValue.trim().toLowerCase();
		if (accessValue.equals("open")) return ACCESS_OPEN;
		else if (accessValue.equals("close")) return ACCESS_CLOSE;
		else if (accessValue.equals("protected")) return ACCESS_PROTECTED;
		else throw new SecurityException("invalid access value [" + accessValue + "]", "valid access values are [open,protected,close]");

	}

	/**
	 * translate a string access value (all,local,none,no,yes) to int type
	 * 
	 * @param accessValue
	 * @param defaultValue when accessValue is invlaid this value will be returned
	 * @return return int access value (VALUE_ALL,VALUE_LOCAL,VALUE_NO,VALUE_NONE,VALUE_YES)
	 */
	public static short toShortAccessValue(String accessValue, short defaultValue) {
		accessValue = accessValue.trim().toLowerCase();
		if (accessValue.equals("no")) return VALUE_NO;
		else if (accessValue.equals("yes")) return VALUE_YES;
		else if (accessValue.equals("all")) return VALUE_ALL;
		else if (accessValue.equals("local")) return VALUE_LOCAL;
		else if (accessValue.equals("none")) return VALUE_NONE;
		else if (accessValue.equals("1")) return VALUE_1;
		else if (accessValue.equals("2")) return VALUE_2;
		else if (accessValue.equals("3")) return VALUE_3;
		else if (accessValue.equals("4")) return VALUE_4;
		else if (accessValue.equals("5")) return VALUE_5;
		else if (accessValue.equals("6")) return VALUE_6;
		else if (accessValue.equals("7")) return VALUE_7;
		else if (accessValue.equals("8")) return VALUE_8;
		else if (accessValue.equals("9")) return VALUE_9;
		else if (accessValue.equals("10")) return VALUE_10;
		else if (accessValue.equals("0")) return VALUE_NO;
		else if (accessValue.equals("-1")) return VALUE_YES;
		else return defaultValue;

	}

	public static short toShortAccessRWValue(String accessValue, short defaultValue) {
		accessValue = accessValue.trim().toLowerCase();
		if (accessValue.equals("open")) return ACCESS_OPEN;
		else if (accessValue.equals("close")) return ACCESS_CLOSE;
		else if (accessValue.equals("protected")) return ACCESS_PROTECTED;
		else return defaultValue;

	}

	/**
	 * translate a short access value (all,local,none,no,yes) to String type
	 * 
	 * @param accessValue
	 * @return return int access value (VALUE_ALL,VALUE_LOCAL,VALUE_NO,VALUE_NONE,VALUE_YES)
	 * @throws SecurityException
	 */
	public static String toStringAccessValue(short accessValue) throws SecurityException {
		switch (accessValue) {
		case VALUE_NONE:
			return "none";
		// case VALUE_NO: return "no";
		case VALUE_YES:
			return "yes";
		// case VALUE_ALL: return "all";
		case VALUE_LOCAL:
			return "local";
		case VALUE_1:
			return "1";
		case VALUE_2:
			return "2";
		case VALUE_3:
			return "3";
		case VALUE_4:
			return "4";
		case VALUE_5:
			return "5";
		case VALUE_6:
			return "6";
		case VALUE_7:
			return "7";
		case VALUE_8:
			return "8";
		case VALUE_9:
			return "9";
		case VALUE_10:
			return "10";
		}
		throw new SecurityException("invalid access value", "valid access values are [all,local,no,none,yes,1,...,10]");

	}

	public static String toStringAccessRWValue(short accessValue) throws SecurityException {
		switch (accessValue) {
		case ACCESS_CLOSE:
			return "close";
		case ACCESS_OPEN:
			return "open";
		case ACCESS_PROTECTED:
			return "protected";
		}
		throw new SecurityException("invalid access value", "valid access values are [open,close,protected]");

	}

	@Override
	public void checkFileLocation(Resource res) throws SecurityException {
		checkFileLocation(null, res, null);
	}

	@Override
	public void checkFileLocation(ConfigWeb cw, Resource res, String strServerPassword) throws SecurityException {
		if (res == null || !(res.getResourceProvider() instanceof FileResourceProvider)) {
			return;
		}
		cw = (ConfigWeb) ThreadLocalPageContext.getConfig(cw);

		Password serverPassword = PasswordImpl.passwordToCompare(cw, true, strServerPassword);

		// All
		if (getAccess(TYPE_FILE) == VALUE_ALL) return;
		// Local
		if (getAccess(TYPE_FILE) == VALUE_LOCAL) {
			res = ResourceUtil.getCanonicalResourceEL(res);

			// local
			if (rootDirectory != null) if (ResourceUtil.isChildOf(res, rootDirectory)) return;
			// custom
			if (!ArrayUtil.isEmpty(customFileAccess)) {
				for (int i = 0; i < customFileAccess.length; i++) {
					if (ResourceUtil.isChildOf(res, customFileAccess[i])) return;
				}
			}
			if (isValid(cw, serverPassword) || isAdminContext()) return;
			throw new SecurityException(createExceptionMessage(res, true), "access is prohibited by security manager");
		}
		// None
		if (isValid(cw, serverPassword)) return;

		// custom
		if (!ArrayUtil.isEmpty(customFileAccess)) {
			res = ResourceUtil.getCanonicalResourceEL(res);

			for (int i = 0; i < customFileAccess.length; i++) {
				if (ResourceUtil.isChildOf(res, customFileAccess[i])) return;
			}
		}

		if (isAdminContext()) return;
		throw new SecurityException(createExceptionMessage(res, false), "access is prohibited by security manager");
	}

	private boolean isAdminContext() {
		PageContext pc = ThreadLocalPageContext.get();
		try {
			if (pc != null && "/lucee".equals(pc.getBasePageSource().getMapping().getVirtualLowerCase())) {
				return true;
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		return false;
	}

	private String createExceptionMessage(Resource res, boolean localAllowed) {

		StringBuffer sb = new StringBuffer(localAllowed && rootDirectory != null ? rootDirectory.getAbsolutePath() : "");
		if (customFileAccess != null) {
			for (int i = 0; i < customFileAccess.length; i++) {
				if (sb.length() > 0) sb.append(" | ");
				sb.append(customFileAccess[i].getAbsolutePath());
			}
		}

		StringBuffer rtn = new StringBuffer("can't access [");
		rtn.append(res.getAbsolutePath());
		rtn.append("]");
		if (sb.length() > 0) {
			rtn.append(" ");
			rtn.append((res.isDirectory() ? "directory" : "file"));
			rtn.append(" must be inside [");
			rtn.append(sb.toString());
			rtn.append("]");
		}
		return rtn.toString();

	}

	private boolean isValid(Config config, Password spw) {
		if (spw == null) {
			try {
				PageContextImpl pc = (PageContextImpl) ThreadLocalPageContext.get();
				spw = pc.getServerPassword();
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}
		config = ThreadLocalPageContext.getConfig(config);

		if (config == null || spw == null) return false;
		try {
			ConfigWebUtil.getConfigServer(config, spw);
			return true;
		}
		catch (PageException e) {
			return false;
		}
	}

	@Override
	public SecurityManager cloneSecurityManager() {
		SecurityManagerImpl sm = new SecurityManagerImpl();

		for (int i = 0; i < accesses.length; i++) {
			sm.accesses[i] = accesses[i];
		}
		if (customFileAccess != null) sm.customFileAccess = (Resource[]) ArrayUtil.clone(customFileAccess, new Resource[customFileAccess.length]);
		sm.rootDirectory = rootDirectory;
		return sm;
	}

	@Override
	public Object clone() {
		return cloneSecurityManager();
	}

	public Resource[] getCustomFileAccess() {
		if (ArrayUtil.isEmpty(customFileAccess)) return EMPTY_RESOURCE_ARRAY;
		return (Resource[]) ArrayUtil.clone(customFileAccess, new Resource[customFileAccess.length]);
	}

	public void setCustomFileAccess(Resource[] fileAccess) {
		this.customFileAccess = merge(this.customFileAccess, fileAccess);
	}

	public void setRootDirectory(Resource rootDirectory) {
		this.rootDirectory = rootDirectory;
	}

	private static Resource[] merge(Resource[] first, Resource[] second) {
		if (ArrayUtil.isEmpty(second)) return first;
		if (ArrayUtil.isEmpty(first)) return second;

		Resource[] tmp = new Resource[first.length + second.length];
		for (int i = 0; i < first.length; i++) {
			tmp[i] = first[i];
		}
		for (int i = 0; i < second.length; i++) {
			tmp[first.length + i] = second[i];
		}
		return tmp;
	}
}