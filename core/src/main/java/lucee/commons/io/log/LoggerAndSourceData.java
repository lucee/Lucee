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
package lucee.commons.io.log;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.xml.XMLLayout;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.log.log4j.Log4jUtil;
import lucee.commons.io.log.log4j.appender.ConsoleAppender;
import lucee.commons.io.log.log4j.appender.DatasourceAppender;
import lucee.commons.io.log.log4j.appender.RollingResourceAppender;
import lucee.commons.io.log.log4j.layout.ClassicLayout;
import lucee.commons.io.log.log4j.layout.DatasourceLayout;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.SystemOut;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;

/**
 * 
 */
public final class LoggerAndSourceData {

    private static final String DEFAULT_PATTERN = "%d{dd.MM.yyyy HH:mm:ss,SSS} %-5p [%c] %m%n";

    private Log _log;
    private final ClassDefinition cdAppender;
    private Appender _appender;
    private final Map<String, String> appenderArgs;
    private final ClassDefinition cdLayout;
    private Layout layout;
    private final Map<String, String> layoutArgs;
    private final int level;
    private final String name;
    private Config config;
    private final boolean readOnly;
    private final String id;
    private boolean dyn;

    public LoggerAndSourceData(Config config, String id, String name, ClassDefinition appender, Map<String, String> appenderArgs, ClassDefinition layout,
	    Map<String, String> layoutArgs, int level, boolean readOnly, boolean dyn) {
	// this.log=new LogAdapter(logger);
	this.config = config;
	this.id = id;
	this.name = name;
	this.cdAppender = appender;
	this.appenderArgs = appenderArgs;
	this.cdLayout = layout;
	this.layoutArgs = layoutArgs;
	this.level = level;
	this.readOnly = readOnly;
	this.dyn = dyn;
    }

    public String id() {
	return id;
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

    public Appender getAppender() {
	getLog();// initialize if necessary
	return _appender;
    }

    public void close() {
	if (_log != null) {
	    Appender a = _appender;
	    _log = null;
	    layout = null;
	    if (a != null) a.close();
	    _appender = null;
	}
    }

    public Map<String, String> getAppenderArgs() {
	getLog();// initialize if necessary
	return appenderArgs;
    }

    public Layout getLayout() {
	getLog();// initialize if necessary
	return layout;
    }

    public ClassDefinition getLayoutClassDefinition() {
	return cdLayout;
    }

    public Map<String, String> getLayoutArgs() {
	getLog();// initialize if necessary
	return layoutArgs;
    }

    public int getLevel() {
	return level;
    }

    public boolean getReadOnly() {
	return readOnly;
    }

    public Log getLog() {
	if (_log == null) {
	    config = ThreadLocalPageContext.getConfig(config);
	    layout = getLayout(cdLayout, layoutArgs, cdAppender, name);
	    _appender = getAppender(config, layout, name, cdAppender, appenderArgs);
	    _log = Log4jUtil.getLogger(config, _appender, name, level);
	}
	return _log;
    }

    private static final Layout getLayout(ClassDefinition cd, Map<String, String> layoutArgs, ClassDefinition cdAppender, String name) {
	if (layoutArgs == null) layoutArgs = new HashMap<String, String>();

	// Layout
	Layout layout = null;

	if (cd != null && cd.hasClass()) {
	    // Classic Layout
	    if (ClassicLayout.class.getName().equalsIgnoreCase(cd.getClassName())) {
		layout = new ClassicLayout();
	    }
	    // Datasource Layout
	    else if (DatasourceLayout.class.getName().equalsIgnoreCase(cd.getClassName())) {
		layout = new DatasourceLayout(name);
	    }
	    // HTML Layout
	    else if (HTMLLayout.class.getName().equalsIgnoreCase(cd.getClassName())) {
		HTMLLayout html = new HTMLLayout();
		layout = html;

		// Location Info
		Boolean locInfo = Caster.toBoolean(layoutArgs.get("locationinfo"), null);
		if (locInfo != null) html.setLocationInfo(locInfo.booleanValue());
		else locInfo = Boolean.FALSE;
		layoutArgs.put("locationinfo", locInfo.toString());

		// Title
		String title = Caster.toString(layoutArgs.get("title"), "");
		if (!StringUtil.isEmpty(title, true)) html.setTitle(title);
		layoutArgs.put("title", title);

	    }
	    // XML Layout
	    else if (XMLLayout.class.getName().equalsIgnoreCase(cd.getClassName())) {
		XMLLayout xml = new XMLLayout();
		layout = xml;

		// Location Info
		Boolean locInfo = Caster.toBoolean(layoutArgs.get("locationinfo"), null);
		if (locInfo != null) xml.setLocationInfo(locInfo.booleanValue());
		else locInfo = Boolean.FALSE;
		layoutArgs.put("locationinfo", locInfo.toString());

		// Properties
		Boolean props = Caster.toBoolean(layoutArgs.get("properties"), null);
		if (props != null) xml.setProperties(props.booleanValue());
		else props = Boolean.FALSE;
		layoutArgs.put("properties", props.toString());

	    }
	    // Pattern Layout
	    else if (PatternLayout.class.getName().equalsIgnoreCase(cd.getClassName())) {
		PatternLayout patt = new PatternLayout();
		layout = patt;

		// pattern
		String pattern = Caster.toString(layoutArgs.get("pattern"), null);
		if (!StringUtil.isEmpty(pattern, true)) patt.setConversionPattern(pattern);
		else {
		    patt.setConversionPattern(DEFAULT_PATTERN);
		    layoutArgs.put("pattern", DEFAULT_PATTERN);
		}
	    }
	    // class definition
	    else {
		Object obj = ClassUtil.loadInstance(cd.getClazz(null), null, null);
		if (obj instanceof Layout) {
		    layout = (Layout) obj;
		    Iterator<Entry<String, String>> it = layoutArgs.entrySet().iterator();
		    Entry<String, String> e;
		    while (it.hasNext()) {
			e = it.next();
			try {
			    Reflector.callSetter(obj, e.getKey(), e.getValue());
			}
			catch (PageException e1) {
			    SystemOut.printDate(e1);// TODO log
			}
		    }

		}
	    }
	}
	if (layout != null) return layout;

	if (cdAppender != null && DatasourceAppender.class.getName().equals(cdAppender.getClassName())) {
	    return new DatasourceLayout(name);
	}
	return new ClassicLayout();
    }

    private static final Appender getAppender(Config config, Layout layout, String name, ClassDefinition cd, Map<String, String> appenderArgs) {
	if (appenderArgs == null) appenderArgs = new HashMap<String, String>();
	// Appender
	Appender appender = null;
	if (cd != null && cd.hasClass()) {
	    // Console Appender
	    if (ConsoleAppender.class.getName().equalsIgnoreCase(cd.getClassName())) {
		// stream-type
		boolean doError = false;
		String st = Caster.toString(appenderArgs.get("streamtype"), null);
		if (!StringUtil.isEmpty(st, true)) {
		    st = st.trim().toLowerCase();
		    if (st.equals("err") || st.equals("error")) doError = true;
		}
		appenderArgs.put("streamtype", doError ? "error" : "output");

		// get print writer
		PrintWriter pw;
		if (doError) {
		    if (config.getErrWriter() == null) pw = new PrintWriter(System.err);
		    else pw = config.getErrWriter();
		}
		else {
		    if (config.getOutWriter() == null) pw = new PrintWriter(System.out);
		    else pw = config.getOutWriter();
		}
		appender = new ConsoleAppender(pw, layout);
	    }
	    else if (DatasourceAppender.class.getName().equalsIgnoreCase(cd.getClassName())) {

		// datasource
		String dsn = Caster.toString(appenderArgs.get("datasource"), null);
		if (StringUtil.isEmpty(dsn, true)) dsn = Caster.toString(appenderArgs.get("datasourceName"), null);
		if (!StringUtil.isEmpty(dsn, true)) dsn = dsn.trim();
		appenderArgs.put("datasource", dsn);

		// username
		String user = Caster.toString(appenderArgs.get("username"), null);
		if (StringUtil.isEmpty(user, true)) user = Caster.toString(appenderArgs.get("user"), null);
		if (!StringUtil.isEmpty(user, true)) user = user.trim();
		else user = null;
		appenderArgs.put("username", user);

		// password
		String pass = Caster.toString(appenderArgs.get("password"), null);
		if (StringUtil.isEmpty(pass, true)) pass = Caster.toString(appenderArgs.get("pass"), null);
		if (!StringUtil.isEmpty(pass, true)) pass = pass.trim();
		else pass = null;
		appenderArgs.put("password", pass);

		// table
		String table = Caster.toString(appenderArgs.get("table"), null);
		if (!StringUtil.isEmpty(table, true)) table = table.trim();
		else table = "LOGS";
		appenderArgs.put("table", table);

		// custom
		String custom = Caster.toString(appenderArgs.get("custom"), null);
		if (!StringUtil.isEmpty(custom, true)) custom = custom.trim();
		else custom = null;
		appenderArgs.put("custom", custom);

		try {
		    appender = new DatasourceAppender(config, layout, dsn, user, pass, table, custom);
		}
		catch (PageException e) {
		    SystemOut.printDate(e);
		    appender = null;
		}
	    }
	    else if (RollingResourceAppender.class.getName().equalsIgnoreCase(cd.getClassName())) {

		// path
		Resource res = null;
		String path = Caster.toString(appenderArgs.get("path"), null);
		if (!StringUtil.isEmpty(path, true)) {
		    path = path.trim();
		    path = ConfigWebUtil.translateOldPath(path);
		    res = ConfigWebUtil.getFile(config, config.getConfigDir(), path, ResourceUtil.TYPE_FILE);
		    if (res.isDirectory()) {
			res = res.getRealResource(name + ".log");
		    }
		}
		if (res == null) {
		    res = ConfigWebUtil.getFile(config, config.getConfigDir(), "logs/" + name + ".log", ResourceUtil.TYPE_FILE);
		}

		// charset
		Charset charset = CharsetUtil.toCharset(Caster.toString(appenderArgs.get("charset"), null), null);
		if (charset == null) {
		    charset = config.getResourceCharset();
		    appenderArgs.put("charset", charset.name());
		}

		// maxfiles
		int maxfiles = Caster.toIntValue(appenderArgs.get("maxfiles"), 10);
		appenderArgs.put("maxfiles", Caster.toString(maxfiles));

		// maxfileSize
		long maxfilesize = Caster.toLongValue(appenderArgs.get("maxfilesize"), 1024 * 1024 * 10);
		appenderArgs.put("maxfilesize", Caster.toString(maxfilesize));

		// timeout
		int timeout = Caster.toIntValue(appenderArgs.get("timeout"), 60); // timeout in seconds
		appenderArgs.put("timeout", Caster.toString(timeout));

		try {
		    appender = new RollingResourceAppender(layout, res, charset, true, maxfilesize, maxfiles, timeout, null);
		}
		catch (IOException e) {
		    SystemOut.printDate(e);
		}
	    }
	    // class definition
	    else {
		Object obj = ClassUtil.loadInstance(cd.getClazz(null), null, null);
		if (obj instanceof Appender) {
		    appender = (Appender) obj;
		    AppenderSkeleton as = obj instanceof AppenderSkeleton ? (AppenderSkeleton) obj : null;
		    Iterator<Entry<String, String>> it = appenderArgs.entrySet().iterator();
		    Entry<String, String> e;
		    String n;
		    while (it.hasNext()) {
			e = it.next();
			n = e.getKey();
			if (as != null) {
			    if ("threshold".equalsIgnoreCase(n)) {
				Level level = Level.toLevel(e.getValue(), null);
				if (level != null) {
				    as.setThreshold(level);
				    continue;
				}
			    }
			}

			try {
			    Reflector.callSetter(obj, e.getKey(), e.getValue());
			}
			catch (PageException e1) {
			    SystemOut.printDate(e1); // TODO log
			}
		    }
		}
	    }
	}
	if (appender instanceof AppenderSkeleton) {
	    ((AppenderSkeleton) appender).activateOptions();
	}
	else if (appender == null) {
	    PrintWriter pw;
	    if (config.getOutWriter() == null) pw = new PrintWriter(System.out);
	    else pw = config.getOutWriter();
	    appender = new ConsoleAppender(pw, layout);
	}

	return appender;
    }

    /*
     * public Logger getLogger() { getLog();// make sure it exists return
     * ((LogAdapter)_log).getLogger(); }
     */

    public static String id(String name, ClassDefinition appender, Map<String, String> appenderArgs, ClassDefinition layout, Map<String, String> layoutArgs, int level,
	    boolean readOnly) {
	StringBuilder sb = new StringBuilder(name).append(';').append(appender).append(';');
	toString(sb, appenderArgs);
	sb.append(';').append(layout).append(';');
	toString(sb, layoutArgs);
	sb.append(';').append(level).append(';').append(readOnly);

	return HashUtil.create64BitHashAsString(sb.toString(), Character.MAX_RADIX);
    }

    private static void toString(StringBuilder sb, Map<String, String> map) {
	if (map == null) return;
	Iterator<Entry<String, String>> it = map.entrySet().iterator();
	Entry<String, String> e;
	while (it.hasNext()) {
	    e = it.next();
	    sb.append(e.getKey()).append(':').append(e.getValue()).append('|');
	}
    }

}