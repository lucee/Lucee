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
package lucee.commons.io.log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lucee.commons.digest.HashUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.SystemOut;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

public final class LoggerAndSourceData {

	private static final String DEFAULT_PATTERN = "%d{dd.MM.yyyy HH:mm:ss,SSS} %-5p [%c] %m%n";

	private Log _log;
	// all appenders of the logger, the first one is also available with the fields below
	private final AppenderDefinition[] appenders;
	private final ClassDefinition cdAppender;
	private Object _appender;
	public final Map<String, String> appenderArgs;
	private final ClassDefinition cdLayout;
	private Object layout;
	public final Map<String, String> layoutArgs;
	private final int level;
	private final String name;
	private Config config;
	private final boolean readOnly;
	private final String id;
	private boolean dyn;

	private short source;

	public LoggerAndSourceData(Config config, String id, String name, ClassDefinition appender, Map<String, String> appenderArgs, ClassDefinition layout,
			Map<String, String> layoutArgs, int level, boolean readOnly, boolean dyn) {
		this(config, id, name, new AppenderDefinition[] { new AppenderDefinition(appender, appenderArgs, layout, layoutArgs) }, level, readOnly, dyn);
	}

	/**
	 * a logger with one or more appenders (LDEV-6792), a single appender behaves exactly like with the
	 * constructor above
	 */
	public LoggerAndSourceData(Config config, String id, String name, AppenderDefinition[] appenders, int level, boolean readOnly, boolean dyn) {
		if (appenders == null || appenders.length == 0) throw new IllegalArgumentException("a logger needs at least one appender");
		this.config = config;
		this.id = id;
		this.name = name;
		this.appenders = appenders.clone();
		// the first appender is the one the single appender methods return
		this.cdAppender = appenders[0].appender;
		this.appenderArgs = appenders[0].appenderArgs;
		this.cdLayout = appenders[0].layout;
		this.layoutArgs = appenders[0].layoutArgs;
		this.level = level;
		this.readOnly = readOnly;
		this.dyn = dyn;
		init();
	}

	public String id() {
		return id;
	}

	public LoggerAndSourceData init() {
		if (_log == null) {
			config = ThreadLocalPageContext.getConfigServer(config);
			try {
				create();
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}
		return this;
	}

	private void create() throws PageException {
		if (appenders.length == 1) {
			layout = eng().getLayout(cdLayout, layoutArgs, cdAppender, name);
			_appender = eng().getAppender(config, layout, name, cdAppender, appenderArgs);
			_log = eng().getLogger(config, _appender, name, level);
			return;
		}

		// more than one appender, the engine gets them in an AppenderList
		LogEngine eng = eng();
		List<Object> created = new ArrayList<Object>();
		try {
			Object l;
			for (AppenderDefinition ad: appenders) {
				l = eng.getLayout(ad.layout, ad.layoutArgs, ad.appender, name);
				created.add(eng.getAppender(config, l, name, ad.appender, ad.appenderArgs));
			}
			AppenderList list = new AppenderList(created);
			Log log = eng.getLogger(config, list, name, level);
			layout = null;
			_appender = list;
			_log = log;
		}
		catch (Throwable t) {
			// do not leave the appenders created so far open (for example the file of the first one)
			for (Object a: created) {
				try {
					eng.closeAppender(a);
				}
				catch (Throwable t2) {
					ExceptionUtil.rethrowIfNecessary(t2);
					t.addSuppressed(t2);
				}
			}
			ExceptionUtil.rethrowIfNecessary(t);
			throw t instanceof PageException ? (PageException) t : Caster.toPageException(t);
		}
	}

	/**
	 * @return all appenders of the logger, the first one is the one the other methods of this class
	 *         return
	 */
	public AppenderDefinition[] getAppenders() {
		return appenders.clone();
	}

	public String getName() {
		return name;
	}

	public boolean getDyn() {
		return dyn;
	}

	public ClassDefinition getAppenderClassDefinition() {
		return cdAppender;
	}

	/*
	 * public Object getAppender() { getLog();// initialize if necessary return _appender; }
	 */

	public void close() throws PageException {

		if (_log != null) {

			Object a = _appender;
			_log = null;
			layout = null;
			if (a != null) eng().closeAppender(a);// a.close();
			_appender = null;
		}
	}

	public Map<String, String> getAppenderArgs(boolean catchException) throws PageException {
		getLog(catchException);// initialize if necessary
		return appenderArgs;
	}

	protected Map<String, String> getAppenderArgsRaw() {
		return appenderArgs;
	}

	/*
	 * public Object getLayout() { getLog();// initialize if necessary return layout; }
	 */

	public ClassDefinition getLayoutClassDefinition() {
		return cdLayout;
	}

	public Map<String, String> getLayoutArgs(boolean catchException) throws PageException {
		getLog(catchException);// initialize if necessary
		return layoutArgs;
	}

	public int getLevel() {
		return level;
	}

	public boolean getReadOnly() {
		return readOnly;
	}

	public Log getLog(boolean catchException) throws PageException {
		if (_log == null) {
			config = ThreadLocalPageContext.getConfigServer(config);
			try {
				create();
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				if (!catchException) throw Caster.toPageException(t);
				SystemOut.printDate(t);
			}
		}
		return _log;
	}

	private LogEngine eng() {
		return ((ConfigPro) config).getLogEngine();
	}

	public static String id(String name, ClassDefinition appender, Map<String, String> appenderArgs, ClassDefinition layout, Map<String, String> layoutArgs, int level,
			boolean readOnly) {
		StringBuilder sb = new StringBuilder(name).append(';').append(appender).append(';');
		toString(sb, appenderArgs);
		sb.append(';').append(layout).append(';');
		toString(sb, layoutArgs);
		sb.append(';').append(level).append(';').append(readOnly);

		return HashUtil.create64BitHashAsString(sb.toString(), Character.MAX_RADIX);
	}

	/**
	 * the id of a logger with one or more appenders, with one appender the id is the same as with
	 * {@link #id(String, ClassDefinition, Map, ClassDefinition, Map, int, boolean)}
	 */
	public static String id(String name, AppenderDefinition[] appenders, int level, boolean readOnly) {
		if (appenders.length == 1) {
			AppenderDefinition ad = appenders[0];
			return id(name, ad.appender, ad.appenderArgs, ad.layout, ad.layoutArgs, level, readOnly);
		}
		StringBuilder sb = new StringBuilder(name).append(';');
		for (AppenderDefinition ad: appenders) {
			sb.append('[').append(ad).append("];");
		}
		sb.append(level).append(';').append(readOnly);
		return HashUtil.create64BitHashAsString(sb.toString(), Character.MAX_RADIX);
	}

	static void toString(StringBuilder sb, Map<String, String> map) {
		if (map == null) return;
		Iterator<Entry<String, String>> it = map.entrySet().iterator();
		Entry<String, String> e;
		while (it.hasNext()) {
			e = it.next();
			sb.append(e.getKey()).append(':').append(e.getValue()).append('|');
		}
	}

	public LoggerAndSourceData setSource(short source) {
		this.source = source;
		return this;
	}

	public short getSource() {
		return source;
	}

}