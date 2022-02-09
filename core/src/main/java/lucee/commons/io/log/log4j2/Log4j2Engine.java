package lucee.commons.io.log.log4j2;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.felix.framework.BundleWiringImpl.BundleClassLoader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.WriterAppender;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.apache.logging.log4j.core.layout.HtmlLayout;
import org.apache.logging.log4j.core.layout.HtmlLayout.Builder;
import org.apache.logging.log4j.core.layout.HtmlLayout.FontSize;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.status.StatusLogger;
import org.osgi.framework.Bundle;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogEngine;
import lucee.commons.io.log.log4j2.appender.ConsoleAppender;
import lucee.commons.io.log.log4j2.appender.DatasourceAppender;
import lucee.commons.io.log.log4j2.appender.ResourceAppender;
import lucee.commons.io.log.log4j2.appender.TaskAppender;
import lucee.commons.io.log.log4j2.layout.ClassicLayout;
import lucee.commons.io.log.log4j2.layout.DataDogLayout;
import lucee.commons.io.log.log4j2.layout.XMLLayout;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.io.retirement.RetireListener;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.reflection.pairs.MethodInstance;
import lucee.transformer.library.ClassDefinitionImpl;

public class Log4j2Engine extends LogEngine {

	public static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024;
	public static final int DEFAULT_MAX_BACKUP_INDEX = 10;

	private static final String DEFAULT_PATTERN = "%d{dd.MM.yyyy HH:mm:ss,SSS} %-5p [%c] %m%n";

	private Config config;
	private String version;

	public Log4j2Engine(Config config) {
		this.config = config;
		init();
	}

	@Override
	public Log getConsoleLog(boolean errorStream, String name, int level) {
		PrintWriter pw = errorStream ? config.getErrWriter() : config.getOutWriter();
		if (pw == null) pw = new PrintWriter(errorStream ? System.err : System.out);

		return new LogAdapter(_getLogger(config,
				getConsoleAppender(createFullName(ThreadLocalPageContext.getConfig(), name), pw, PatternLayout.newBuilder().withPattern(DEFAULT_PATTERN).build(), true), name,
				level));
	}

	@Override
	public Log getResourceLog(Resource res, Charset charset, String name, int level, int timeout, RetireListener listener, boolean async) throws PageException {
		Appender a = toResourceAppender(createFullName(ThreadLocalPageContext.getConfig(), name), res, new ClassicLayout(), charset, DEFAULT_MAX_BACKUP_INDEX,
				DEFAULT_MAX_FILE_SIZE, timeout, true);
		if (async) {
			a = new TaskAppender(config, a);
		}
		return new LogAdapter(_getLogger(config, a, name, level));
	}

	@Override
	public ClassDefinition appenderClassDefintion(String className) {
		// we define the old classes for all existing log entries
		if ("console".equalsIgnoreCase(className) || "lucee.commons.io.log.log4j.appender.ConsoleAppender".equals(className)
				|| "lucee.commons.io.log.log4j2.appender.ConsoleAppender".equals(className)) {
			return new ClassDefinitionImpl(ConsoleAppender.class);
		}
		if ("resource".equalsIgnoreCase(className) || "lucee.commons.io.log.log4j.appender.RollingResourceAppender".equals(className)
				|| "lucee.commons.io.log.log4j2.appender.ResourceAppender".equals(className)) {
			return new ClassDefinitionImpl(ResourceAppender.class);
		}
		if ("datasource".equalsIgnoreCase(className) || "lucee.commons.io.log.log4j.appender.DatasourceAppender".equals(className)
				|| "lucee.commons.io.log.log4j2.appender.DatasourceAppender".equals(className)) {
			return new ClassDefinitionImpl(DatasourceAppender.class);
		}
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
	public ClassDefinition<?> layoutClassDefintion(String className) {
		if ("classic".equalsIgnoreCase(className) || "lucee.commons.io.log.log4j.layout.ClassicLayout".equals(className)
				|| "lucee.commons.io.log.log4j2.layout.ClassicLayout".equals(className)) {
			return new ClassDefinitionImpl(ClassicLayout.class);
		}
		if ("datasource".equalsIgnoreCase(className)) return new ClassDefinitionImpl(ClassicLayout.class);
		if ("html".equalsIgnoreCase(className) || "org.apache.log4j.HTMLLayout".equals(className) || "org.apache.logging.log4j.core.layout.HtmlLayout".equals(className)) {
			return new ClassDefinitionImpl(HtmlLayout.class);
		}
		if ("xml".equalsIgnoreCase(className) || "org.apache.log4j.xml.XMLLayout".equalsIgnoreCase(className)
				|| "org.apache.logging.log4j.core.layout.XmlLayout".equalsIgnoreCase(className) || "lucee.commons.io.log.log4j2.layout.XMLLayout".equals(className)) {
			return new ClassDefinitionImpl(XMLLayout.class);
		}
		if ("pattern".equalsIgnoreCase(className) || "org.apache.log4j.PatternLayout".equals(className) || "org.apache.logging.log4j.core.layout.PatternLayout".equals(className)) {
			return new ClassDefinitionImpl(PatternLayout.class);
		}
		if ("datadog".equalsIgnoreCase(className) || className.indexOf(".DataDogLayout") != -1) {
			return new ClassDefinitionImpl(DataDogLayout.class);
		}

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
	public final Object getLayout(ClassDefinition cd, Map<String, String> layoutArgs, ClassDefinition cdAppender, String name) throws PageException {
		if (layoutArgs == null) layoutArgs = new HashMap<String, String>();

		// Layout
		Layout layout = null;

		if (cd != null && cd.hasClass()) {
			// Classic Layout
			if (ClassicLayout.class.getName().equalsIgnoreCase(cd.getClassName())) {
				layout = new ClassicLayout();
			}
			// HTML Layout
			else if (HtmlLayout.class.getName().equalsIgnoreCase(cd.getClassName())) {
				Builder builder = HtmlLayout.newBuilder();

				// Location Info
				Boolean locInfo = Caster.toBoolean(layoutArgs.get("locationinfo"), null);
				if (locInfo != null) builder.withLocationInfo(locInfo.booleanValue());
				else locInfo = Boolean.FALSE;
				layoutArgs.put("locationinfo", locInfo.toString());

				// Title
				String title = Caster.toString(layoutArgs.get("title"), "");
				if (!StringUtil.isEmpty(title, true)) builder.withTitle(title);
				layoutArgs.put("title", title);

				// font name
				String fontName = Caster.toString(layoutArgs.get("fontName"), "");
				if (!StringUtil.isEmpty(fontName, true)) builder.withFontName(fontName);
				layoutArgs.put("fontName", fontName);

				// font size
				FontSize fontSize = toFontSize(Caster.toString(layoutArgs.get("fontSize"), null));
				if (fontSize != null) builder.withFontSize(fontSize);
				layoutArgs.put("fontSize", fontSize == null ? "" : fontSize.name());

				layout = builder.build();

			}
			// XML Layout
			else if (XMLLayout.class.getName().equalsIgnoreCase(cd.getClassName())) {

				// Location Info
				boolean locInfo = Caster.toBooleanValue(layoutArgs.get("locationinfo"), false);
				layoutArgs.put("locationinfo", locInfo + "");

				// Properties TODO
				Boolean props = Caster.toBoolean(layoutArgs.get("properties"), null);
				layoutArgs.put("properties", props.toString());
				// TODO add more attribute

				return new XMLLayout(CharsetUtil.UTF8, true, locInfo);

			}
			// Pattern Layout
			else if (PatternLayout.class.getName().equalsIgnoreCase(cd.getClassName())) {
				org.apache.logging.log4j.core.layout.PatternLayout.Builder builder = PatternLayout.newBuilder();

				// pattern
				String pattern = Caster.toString(layoutArgs.get("pattern"), null);
				if (!StringUtil.isEmpty(pattern, true)) builder.withPattern(pattern);
				else {
					builder.withPattern(DEFAULT_PATTERN);
					layoutArgs.put("pattern", DEFAULT_PATTERN);
				}

				layout = builder.build();
			}
			// DataDog Layout
			else if (cd.getClassName().indexOf(".DataDogLayout") != -1) {
				layout = new DataDogLayout();
			}
			// class definition
			else {
				// MUST that will no longer work that way
				Object obj = ClassUtil.loadInstance(cd.getClazz(null), null, null);
				if (obj instanceof Layout) {

					Reflector.callSetter(obj, "name", name);
					Reflector.callSetter(obj, "layout", toLayout(layout));
					Iterator<Entry<String, String>> it = layoutArgs.entrySet().iterator();
					Entry<String, String> entry;
					while (it.hasNext()) {
						entry = it.next();
						MethodInstance mi = Reflector.getSetter(obj, entry.getKey(), entry.getValue(), null);
						if (mi != null) {
							try {
								mi.invoke(obj);
							}
							catch (Exception e) {
								throw Caster.toPageException(e);
							}
						}
					}
				}
			}
		}
		if (layout != null) return layout;
		return new ClassicLayout();
	}

	@Override
	public final Object getAppender(Config config, Object layout, String name, ClassDefinition cd, Map<String, String> appenderArgs) throws PageException {
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
				appender = getConsoleAppender(createFullName(config, name), pw, toLayout(layout), true);
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

				appender = getDatasourceAppender(config, createFullName(config, name), dsn, user, pass, table, custom, true);
			}
			else if (ResourceAppender.class.getName().equalsIgnoreCase(cd.getClassName())) {

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
				appender = toResourceAppender(createFullName(config, name), res, toLayout(layout), charset, maxfiles, maxfilesize, timeout, true);

			}
			// class definition
			else {
				Object obj = ClassUtil.loadInstance(cd.getClazz(null), null, null);

				if (obj instanceof Appender) {
					Appender a = (Appender) obj;
					Reflector.callSetter(obj, "name", name);
					Reflector.callSetter(obj, "layout", toLayout(layout));
					Iterator<Entry<String, String>> it = appenderArgs.entrySet().iterator();
					Entry<String, String> entry;
					while (it.hasNext()) {
						entry = it.next();
						MethodInstance mi = Reflector.getSetter(obj, entry.getKey(), entry.getValue(), null);
						if (mi != null) {
							try {
								mi.invoke(obj);
							}
							catch (Exception e) {
								throw Caster.toPageException(e);
							}
						}
					}
				}
			}
		}
		// if (appender instanceof AppenderSkeleton) {
		// TODO ((AppenderSkeleton) appender).activateOptions();
		// }
		if (appender == null) {
			PrintWriter pw;
			if (config.getOutWriter() == null) pw = new PrintWriter(System.out);
			else pw = config.getOutWriter();
			appender = getConsoleAppender(createFullName(config, name), pw, toLayout(layout), true);
		}

		return appender;
	}

	@Override
	public Log getLogger(Config config, Object appender, String name, int level) throws ApplicationException {
		return new LogAdapter(_getLogger(config, toAppender(appender), name, level));
	}

	private static final Logger _getLogger(Config config, Appender appender, String name, int level) {

		if (!(LogManager.getFactory() instanceof org.apache.logging.log4j.core.impl.Log4jContextFactory)) {
			init();
		}

		String fullname = createFullName(config, name);

		// fullname

		Logger l;
		if (LogManager.exists(fullname)) {
			l = LogManager.getLogger(fullname);
			if (l instanceof org.apache.logging.log4j.core.Logger) {
				org.apache.logging.log4j.core.Logger cl = (org.apache.logging.log4j.core.Logger) l;
				for (Appender a: cl.getAppenders().values()) {
					cl.removeAppender(a);
				}
			}
		}
		else l = LogManager.getLogger(fullname);

		if (l instanceof org.apache.logging.log4j.core.Logger) {
			org.apache.logging.log4j.core.Logger cl = (org.apache.logging.log4j.core.Logger) l;
			cl.setAdditive(false);
			cl.addAppender(appender);
			cl.setLevel(LogAdapter.toLevel(level));
		}
		else {
			l.atLevel(LogAdapter.toLevel(level));
		}

		//
		//
		//

		return l;
	}

	private static String createFullName(Config config, String name) {
		String fullname = name;
		if (config instanceof ConfigWeb) {
			ConfigWeb cw = (ConfigWeb) config;
			return "web." + cw.getLabel() + "." + name;
		}
		if (config == null) return name;
		return fullname = "server." + name;
	}

	private static void init() {
		StatusLogger.getLogger().setLevel(Level.FATAL);
		LogManager.setFactory(new org.apache.logging.log4j.core.impl.Log4jContextFactory());
		PluginManager.addPackage("");
	}

	@Override
	public void closeAppender(Object appender) throws ApplicationException {
		toAppender(appender).stop();
	}

	private Appender toAppender(Object l) throws ApplicationException {
		if (l instanceof Appender) return (Appender) l;
		throw new ApplicationException("cannot convert [" + l + "] to an Appender");
	}

	private Layout toLayout(Object l) throws ApplicationException {
		if (l instanceof Layout) return (Layout) l;
		throw new ApplicationException("cannot convert [" + l + "] to a Layout");
	}

	@Override
	public Object getDefaultLayout() {

		return PatternLayout.newBuilder().withPattern("%d{dd.MM.yyyy HH:mm:ss,SSS} %-5p [%c] %m%n").build();
	}

	@Override
	public Object getClassicLayout() {
		return new ClassicLayout();
	}

	private static Appender getDatasourceAppender(Config config, String name, String dsn, String user, String pass, String table, String custom, boolean start)
			throws PageException {

		DatasourceAppender appender = new DatasourceAppender(config, name, null, dsn, user, pass, table, custom);

		if (start) appender.start();
		return appender;
	}

	private static Appender getConsoleAppender(String name, PrintWriter pw, Layout<?> layout, boolean start) {
		WriterAppender appender = WriterAppender.newBuilder()

				.setName(name)

				.setTarget(pw)

				.setLayout(layout)

				.build();
		if (start) appender.start();
		return appender;
	}

	private Appender toResourceAppender(String name, Resource res, Layout<?> layout, Charset charset, int maxfiles, long maxFileSize, int timeout, boolean start)
			throws PageException {
		try {
			ResourceAppender appender = new ResourceAppender(name, null, layout, res, charset, true, timeout, maxFileSize, maxfiles, null);
			if (start) appender.start();
			return appender;
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}

	private FontSize toFontSize(String str) {
		if (StringUtil.isEmpty(str, true)) return null;
		str = str.trim();
		if ("large".equalsIgnoreCase(str)) return FontSize.LARGE;
		if ("larger".equalsIgnoreCase(str)) return FontSize.LARGER;
		if ("medium".equalsIgnoreCase(str)) return FontSize.MEDIUM;
		if ("small".equalsIgnoreCase(str)) return FontSize.SMALL;
		if ("smaller".equalsIgnoreCase(str)) return FontSize.SMALLER;
		if ("xlarge".equalsIgnoreCase(str)) return FontSize.XLARGE;
		if ("xsmall".equalsIgnoreCase(str)) return FontSize.XSMALL;
		if ("xxlarge".equalsIgnoreCase(str)) return FontSize.XXLARGE;
		if ("xxsmall".equalsIgnoreCase(str)) return FontSize.XXSMALL;

		return null;
	}

	@Override
	public String getVersion() {
		if (version == null) {
			ClassLoader cl = LogManager.class.getClassLoader();
			if (cl instanceof BundleClassLoader) {
				BundleClassLoader bcl = (BundleClassLoader) cl;
				Bundle b = bcl.getBundle();
				version = b.getVersion().toString();
			}
			else version = "2";
		}
		return version;
	}

}
