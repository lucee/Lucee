package lucee.commons.io.log.log4j;

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
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.xml.XMLLayout;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogEngine;
import lucee.commons.io.log.log4j.appender.ConsoleAppender;
import lucee.commons.io.log.log4j.appender.DatasourceAppender;
import lucee.commons.io.log.log4j.appender.RollingResourceAppender;
import lucee.commons.io.log.log4j.appender.TaskAppender;
import lucee.commons.io.log.log4j.layout.ClassicLayout;
import lucee.commons.io.log.log4j.layout.DatasourceLayout;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.io.retirement.RetireListener;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.SystemOut;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;
import lucee.transformer.library.ClassDefinitionImpl;

public class Log4jEngine extends LogEngine {

	// public static final long MAX_FILE_SIZE = 1024 * 1024 * 10;
	// public static final int MAX_FILES = 10;

	private static final String DEFAULT_PATTERN = "%d{dd.MM.yyyy HH:mm:ss,SSS} %-5p [%c] %m%n";

	private Config config;

	public Log4jEngine(Config config) {
		this.config = config;
	}

	@Override
	public Log getConsoleLog(boolean errorStream, String name, int level) {

		PrintWriter pw = errorStream ? config.getErrWriter() : config.getOutWriter();
		if (pw == null) pw = new PrintWriter(errorStream ? System.err : System.out);

		return new LogAdapter(_getLogger(config, new ConsoleAppender(pw, new PatternLayout(DEFAULT_PATTERN)), name, level));
	}

	@Override
	public Log getResourceLog(Resource res, Charset charset, String name, int level, int timeout, RetireListener listener, boolean async) throws IOException {
		Appender a = new RollingResourceAppender(new ClassicLayout(), res, charset, true, RollingResourceAppender.DEFAULT_MAX_FILE_SIZE,
				RollingResourceAppender.DEFAULT_MAX_BACKUP_INDEX, timeout, listener); // no open stream at all

		if (async) {
			a = new TaskAppender(config, a);
		}
		return new LogAdapter(_getLogger(config, a, name, level));
	}

	@Override
	public ClassDefinition appenderClassDefintion(String className) {
		if ("console".equalsIgnoreCase(className)) return new ClassDefinitionImpl(ConsoleAppender.class);
		if ("resource".equalsIgnoreCase(className)) return new ClassDefinitionImpl(RollingResourceAppender.class);
		if ("datasource".equalsIgnoreCase(className)) return new ClassDefinitionImpl(DatasourceAppender.class);

		return new ClassDefinitionImpl(className);
	}

	/*
	 * public ClassDefinition toClassDefinitionAppender(Struct sct, ClassDefinition defaultValue) { if
	 * (sct == null) return defaultValue;
	 * 
	 * // class String className = Caster.toString(sct.get("class", null), null); if
	 * (StringUtil.isEmpty(className)) return defaultValue;
	 * 
	 * if ("console".equalsIgnoreCase(className)) return new ClassDefinitionImpl(ConsoleAppender.class);
	 * if ("resource".equalsIgnoreCase(className)) return new
	 * ClassDefinitionImpl(RollingResourceAppender.class); if ("datasource".equalsIgnoreCase(className))
	 * return new ClassDefinitionImpl(DatasourceAppender.class);
	 * 
	 * // name String name = bundleName(sct); Version version = bundleVersion(sct);
	 * 
	 * if (StringUtil.isEmpty(name)) return new ClassDefinitionImpl(className);
	 * 
	 * return new ClassDefinitionImpl(null, className, name, version); }
	 */

	@Override
	public ClassDefinition layoutClassDefintion(String className) {
		if ("classic".equalsIgnoreCase(className)) return new ClassDefinitionImpl(ClassicLayout.class);
		if ("datasource".equalsIgnoreCase(className)) return new ClassDefinitionImpl(DatasourceLayout.class);
		if ("html".equalsIgnoreCase(className)) return new ClassDefinitionImpl(HTMLLayout.class);
		if ("xml".equalsIgnoreCase(className)) return new ClassDefinitionImpl(XMLLayout.class);
		if ("pattern".equalsIgnoreCase(className)) return new ClassDefinitionImpl(PatternLayout.class);

		return new ClassDefinitionImpl(className);
	}

	/*
	 * public ClassDefinition toClassDefinitionLayout(Struct sct, ClassDefinition defaultValue) { if
	 * (sct == null) return defaultValue;
	 * 
	 * // class String className = Caster.toString(sct.get("class", null), null); if
	 * (StringUtil.isEmpty(className)) return defaultValue;
	 * 
	 * if ("classic".equalsIgnoreCase(className)) return new ClassDefinitionImpl(ClassicLayout.class);
	 * if ("datasource".equalsIgnoreCase(className)) return new
	 * ClassDefinitionImpl(DatasourceLayout.class); if ("html".equalsIgnoreCase(className)) return new
	 * ClassDefinitionImpl(HTMLLayout.class); if ("xml".equalsIgnoreCase(className)) return new
	 * ClassDefinitionImpl(XMLLayout.class); if ("pattern".equalsIgnoreCase(className)) return new
	 * ClassDefinitionImpl(PatternLayout.class);
	 * 
	 * String name = bundleName(sct); Version version = bundleVersion(sct);
	 * 
	 * if (StringUtil.isEmpty(name)) return new ClassDefinitionImpl(className);
	 * 
	 * return new ClassDefinitionImpl(null, className, name, version); }
	 */

	@Override
	public final Object getLayout(ClassDefinition cd, Map<String, String> layoutArgs, ClassDefinition cdAppender, String name) {
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
					layout = toLayout(obj);
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

	@Override
	public final Object getAppender(Config config, Object layout, String name, ClassDefinition cd, Map<String, String> appenderArgs) {
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
				appender = new ConsoleAppender(pw, toLayout(layout));
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
					appender = new DatasourceAppender(config, toLayout(layout), dsn, user, pass, table, custom);
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
					appender = new RollingResourceAppender(toLayout(layout), res, charset, true, maxfilesize, maxfiles, timeout, null);
				}
				catch (IOException e) {
					SystemOut.printDate(e);
				}
			}
			// class definition
			else {
				Object obj = ClassUtil.loadInstance(cd.getClazz(null), null, null);
				if (obj instanceof Appender) {
					appender = toAppender(obj);
					AppenderSkeleton as = obj instanceof AppenderSkeleton ? (AppenderSkeleton) obj : null;
					appender.setName(name);
					appender.setLayout(toLayout(layout));
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
			appender = new ConsoleAppender(pw, toLayout(layout));
		}

		return appender;
	}

	@Override
	public Log getLogger(Config config, Object appender, String name, int level) {
		return new LogAdapter(_getLogger(config, toAppender(appender), name, level));
	}

	private static final Logger _getLogger(Config config, Appender appender, String name, int level) {
		// fullname
		String fullname = name;
		if (config instanceof ConfigWeb) {
			ConfigWeb cw = (ConfigWeb) config;
			fullname = "web." + cw.getLabel() + "." + name;
		}
		else fullname = "server." + name;

		Logger l = LogManager.exists(fullname);
		if (l != null) l.removeAllAppenders();
		else l = LogManager.getLogger(fullname);
		l.setAdditivity(false);
		l.addAppender(appender);
		l.setLevel(LogAdapter.toLevel(level));
		return l;
	}

	@Override
	public void closeAppender(Object appender) {
		toAppender(appender).close();
	}

	private Appender toAppender(Object l) {
		if (l instanceof Appender) return (Appender) l;
		throw new RuntimeException("cannot convert [" + l + "] to an Appender");
	}

	private Layout toLayout(Object l) {
		if (l instanceof Layout) return (Layout) l;
		throw new RuntimeException("cannot convert [" + l + "] to a Layout");
	}

	@Override
	public Object getDefaultLayout() {
		return new PatternLayout("%d{dd.MM.yyyy HH:mm:ss,SSS} %-5p [%c] %m%n");
	}
}
