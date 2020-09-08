/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 **/
package lucee.runtime.type.scope.storage;

import lucee.commons.io.IOUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.converter.ScriptConverter;
import lucee.runtime.interpreter.CFMLExpressionInterpreter;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.scope.ScopeContext;

/**
 * client scope that store it's data in a resource
 */
public abstract class StorageScopeFile extends StorageScopeImpl {

	private static final long serialVersionUID = -7519591903822909934L;

	public static final String STORAGE_TYPE = "File";

	private static ScriptConverter serializer = new ScriptConverter();
	protected static CFMLExpressionInterpreter evaluator = new CFMLExpressionInterpreter(false);

	private Resource res;

	/**
	 * Constructor of the class
	 * 
	 * @param pc
	 * @param name
	 * @param sct
	 */
	protected StorageScopeFile(PageContext pc, Resource res, String strType, int type, Struct sct) {
		super(sct == null ? (sct = new StructImpl()) : sct, doNowIfNull(pc, Caster.toDate(sct.get(TIMECREATED, null), false, pc.getTimeZone(), null)),
				doNowIfNull(pc, Caster.toDate(sct.get(LASTVISIT, null), false, pc.getTimeZone(), null)), -1,
				type == SCOPE_CLIENT ? Caster.toIntValue(sct.get(HITCOUNT, "1"), 1) : 0, strType, type);

		this.res = res;// pc.getConfig().getClientScopeDir().getRealResource(name+"-"+pc.getCFID()+".script");

	}

	private static DateTime doNowIfNull(PageContext pc, DateTime dt) {
		if (dt == null) return new DateTimeImpl(pc.getConfig());
		return dt;
	}

	/**
	 * Constructor of the class, clone existing
	 * 
	 * @param other
	 */
	protected StorageScopeFile(StorageScopeFile other, boolean deepCopy) {
		super(other, deepCopy);
		this.res = other.res;
	}

	@Override
	public void touchBeforeRequest(PageContext pc) {
		setTimeSpan(pc);
		super.touchBeforeRequest(pc);
	}

	@Override
	public void touchAfterRequest(PageContext pc) {
		setTimeSpan(pc);
		super.touchAfterRequest(pc);
		store(pc);
	}

	@Override
	public void store(PageContext pc) {
		// if(!super.hasContent()) return;
		try {
			if (!res.exists()) ResourceUtil.createFileEL(res, true);
			IOUtil.write(res, (getTimeSpan() + System.currentTimeMillis()) + ":" + serializer.serializeStruct(sct, ignoreSet), "UTF-8", false);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	protected static Struct _loadData(PageContext pc, Resource res, Log log) {
		if (res.exists()) {
			try {
				String str = IOUtil.toString(res, "UTF-8");
				int index = str.indexOf(':');
				if (index != -1) {
					long expires = Caster.toLongValue(str.substring(0, index), -1L);
					// check is for backward compatibility, old files have no expires date inside. they do ot expire
					if (expires != -1) {
						str = str.substring(index + 1);
						/*
						 * if(checkExpires && expires<System.currentTimeMillis()){ print.o("expired("+new
						 * Date(expires)+"):"+res); return null; } else { str=str.substring(index+1);
						 * print.o("not expired("+new Date(expires)+"):"+res); print.o(str); }
						 */
					}
				}
				Struct s = (Struct) evaluator.interpret(pc, str);
				ScopeContext.debug(log, "load existing file storage [" + res + "]");
				return s;
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				ScopeContext.error(log, t);
			}
		}
		ScopeContext.debug(log, "create new file storage [" + res + "]");
		return null;
	}

	@Override
	public void unstore(PageContext pc) {
		try {
			if (!res.exists()) return;
			res.remove(true);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	protected static Resource _loadResource(ConfigWeb config, int type, String name, String cfid) {
		ConfigImpl ci = (ConfigImpl) config;
		Resource dir = type == SCOPE_CLIENT ? ci.getClientScopeDir() : ci.getSessionScopeDir();
		return dir.getRealResource(getFolderName(name, cfid, true));
	}

	/**
	 * return a folder name that match given input
	 * 
	 * @param name
	 * @param cfid
	 * @param addExtension
	 * @return
	 */
	public static String getFolderName(String name, String cfid, boolean addExtension) {
		if (addExtension) return getFolderName(name, cfid, false) + ".scpt";
		if (!StringUtil.isEmpty(name)) name = encode(name);// StringUtil.toVariableName(StringUtil.toLowerCase(name));
		else name = "__empty__";
		return name + "/" + cfid.substring(0, 2) + "/" + cfid.substring(2);
	}

	@Override
	public String getStorageType() {
		return STORAGE_TYPE;
	}

}