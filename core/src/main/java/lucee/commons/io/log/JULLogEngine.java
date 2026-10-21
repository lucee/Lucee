package lucee.commons.io.log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.io.retirement.RetireListener;
import lucee.commons.io.retirement.RetireOutputStream;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.transformer.library.ClassDefinitionImpl;

/**
 * The log engine that is used when no extension provides one, it is based on java.util.logging. It
 * supports the console and the resource (file) appender with a size based rotation and the classic
 * and pattern layout. Other appenders and layouts are replaced by these ones (a warning is logged
 * once), an extension like the Log4j extension provides more.
 */
public final class JULLogEngine implements LogEngine {

	// the classes the class definitions of this engine point to, they only exist to be loadable and are never instantiated
	public static final class ConsoleAppenderType {
	}

	public static final class ResourceAppenderType {
	}

	public static final class ClassicLayoutType {
	}

	public static final class PatternLayoutType {
	}

	private static final int DEFAULT_MAX_FILES = 10;
	private static final long DEFAULT_MAX_FILE_SIZE = 1024 * 1024 * 10;
	private static final int DEFAULT_TIMEOUT = 60;

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	// levels with the names of Lucee, ordered like the levels of Lucee
	private static final class LuceeLevel extends Level {
		private static final long serialVersionUID = 1L;

		LuceeLevel(String name, int value) {
			super(name, value);
		}
	}

	private static final Level[] LEVELS = new Level[] { new LuceeLevel("TRACE", 400), new LuceeLevel("INFO", 800), new LuceeLevel("DEBUG", 500), new LuceeLevel("WARN", 900),
			new LuceeLevel("ERROR", 1000), new LuceeLevel("FATAL", 1100) };

	private static final JULLogEngine INSTANCE = new JULLogEngine();

	private final Map<String, Boolean> warned = new ConcurrentHashMap<>();

	private JULLogEngine() {
	}

	public static JULLogEngine getInstance() {
		return INSTANCE;
	}

	private static Level toLevel(int level) {
		return level >= 0 && level < LEVELS.length ? LEVELS[level] : LEVELS[Log.LEVEL_INFO];
	}

	@Override
	public void init(Config config) {
	}

	@Override
	public String getVersion() {
		return "jul";
	}

	@Override
	public Log getConsoleLog(boolean errorStream, String name, int level) throws PageException {
		return getLogger(null, handler(new ConsoleHandler(null, errorStream), new PatternFormatter(null)), name, level);
	}

	@Override
	public Log getResourceLog(Resource res, Charset charset, String name, int level, int timeout, RetireListener listener, boolean async) throws PageException {
		ClassicFormatter formatter = new ClassicFormatter(null);
		return getLogger(null, handler(fileHandler(res, charset, DEFAULT_MAX_FILES, DEFAULT_MAX_FILE_SIZE, timeout, listener, formatter), formatter), name, level);
	}

	@Override
	public ClassDefinition<?> appenderClassDefintion(String className) {
		if (LogLegacyHandler.isConsoleAppender(className)) return new ClassDefinitionImpl(ConsoleAppenderType.class);
		if (LogLegacyHandler.isResourceAppender(className)) return new ClassDefinitionImpl(ResourceAppenderType.class);
		warnOnce("appender:" + className, "The appender [" + className + "] is not supported by the built-in log engine, the console appender is used instead. Install the Log4j extension to use it.");
		return new ClassDefinitionImpl(ConsoleAppenderType.class);
	}

	@Override
	public ClassDefinition<?> layoutClassDefintion(String className) {
		if (LogLegacyHandler.isClassicLayout(className)) return new ClassDefinitionImpl(ClassicLayoutType.class);
		if (LogLegacyHandler.isPatternLayout(className)) return new ClassDefinitionImpl(PatternLayoutType.class);
		warnOnce("layout:" + className, "The layout [" + className + "] is not supported by the built-in log engine, the classic layout is used instead. Install the Log4j extension to use it.");
		// no class, the logger gets the classic layout
		return new ClassDefinitionImpl();
	}

	@Override
	public Log getLogger(Config config, Object appender, String name, int level) throws PageException {
		if (!(appender instanceof Handler)) throw new IllegalArgumentException("the appender [" + appender + "] was not created by the JUL log engine");
		// an anonymous logger is not registered with the LogManager, so it cannot clash with the loggers of other libraries
		Logger logger = Logger.getAnonymousLogger();
		logger.setUseParentHandlers(false);
		logger.setLevel(toLevel(level));
		logger.addHandler((Handler) appender);
		return new JULLog(logger, level);
	}

	@Override
	public Object getLayout(ClassDefinition cd, Map<String, String> layoutArgs, ClassDefinition cdAppender, String name) throws PageException {
		String className = cd != null ? cd.getClassName() : null;
		if (LogLegacyHandler.isClassicLayout(className)) return new ClassicFormatter(null);
		if (LogLegacyHandler.isPatternLayout(className)) return new PatternFormatter(null);
		warnOnce("layout:" + className, "The layout [" + className + "] is not supported by the built-in log engine (log [" + name
				+ "]), the classic layout is used instead. Install the Log4j extension to use it.");
		return new ClassicFormatter(null);
	}

	@Override
	public Object getAppender(Config config, Object layout, String name, ClassDefinition cd, Map<String, String> appenderArgs) throws PageException {
		Formatter formatter = layout instanceof Formatter ? (Formatter) layout : null;
		String className = cd != null ? cd.getClassName() : null;
		// the names of the arguments are not case sensitive
		Map<String, String> args = new java.util.HashMap<>();
		if (appenderArgs != null) {
			for (Map.Entry<String, String> e: appenderArgs.entrySet()) {
				args.put(e.getKey().toLowerCase(), e.getValue());
			}
		}
		appenderArgs = args;

		if (LogLegacyHandler.isResourceAppender(className)) return resourceHandler(config, formatter, name, appenderArgs);
		if (StringUtil.isEmpty(className, true) || LogLegacyHandler.isConsoleAppender(className)) {
			String st = Caster.toString(appenderArgs.get("streamtype"), null);
			boolean error = !StringUtil.isEmpty(st, true) && ("err".equalsIgnoreCase(st.trim()) || "error".equalsIgnoreCase(st.trim()));
			return handler(new ConsoleHandler(config, error), formatter != null ? formatter : new PatternFormatter(config));
		}

		warnOnce("appender:" + className, "The appender [" + className + "] is not supported by the built-in log engine (log [" + name
				+ "]), the console appender is used instead. Install the Log4j extension to use it.");
		return handler(new ConsoleHandler(config, false), formatter != null ? formatter : new PatternFormatter(config));
	}

	private Handler resourceHandler(Config config, Formatter formatter, String name, Map<String, String> args) throws PageException {
		Resource res = null;
		String path = Caster.toString(args.get("path"), null);
		if (!StringUtil.isEmpty(path, true)) {
			res = ConfigUtil.getFile(config, config.getConfigDir(), ConfigUtil.translateOldPath(path.trim()), ResourceUtil.TYPE_FILE);
			if (res != null && res.isDirectory()) res = res.getRealResource(name + ".log");
		}
		if (res == null) res = ConfigUtil.getFile(config, config.getConfigDir(), "logs/" + name + ".log", ResourceUtil.TYPE_FILE);

		Charset charset = CharsetUtil.toCharset(Caster.toString(args.get("charset"), null), null);
		if (charset == null) charset = config.getResourceCharset();
		int maxFiles = Caster.toIntValue(args.get("maxfiles"), DEFAULT_MAX_FILES);
		long maxFileSize = Caster.toLongValue(args.get("maxfilesize"), DEFAULT_MAX_FILE_SIZE);
		int timeout = Caster.toIntValue(args.get("timeout"), DEFAULT_TIMEOUT);
		if (formatter == null) formatter = new ClassicFormatter(config);
		return handler(fileHandler(res, charset, maxFiles, maxFileSize, timeout, null, formatter), formatter);
	}

	// one handler for every file, shared by all the loggers that write to it, so a file is not rotated twice
	private static final Map<String, SharedFile> sharedFiles = new ConcurrentHashMap<>();

	private static final class SharedFile {
		final String key;
		final ResourceHandler handler;
		int users;

		SharedFile(String key, ResourceHandler handler) {
			this.key = key;
			this.handler = handler;
		}
	}

	// a logger of a shared file, it formats with its own layout and writes through the shared handler
	private static final class SharedFileHandler extends Handler {
		private final SharedFile shared;
		private boolean closed;

		SharedFileHandler(SharedFile shared) {
			this.shared = shared;
		}

		@Override
		public void publish(LogRecord record) {
			if (closed || !isLoggable(record)) return;
			shared.handler.write(getFormatter().format(record));
		}

		@Override
		public void flush() {
			shared.handler.flush();
		}

		@Override
		public void close() {
			synchronized (sharedFiles) {
				if (closed) return;
				closed = true;
				if (--shared.users <= 0) {
					sharedFiles.remove(shared.key);
					shared.handler.close();
				}
			}
		}
	}

	private static Handler fileHandler(Resource res, Charset charset, int maxFiles, long maxFileSize, int timeout, RetireListener listener, Formatter formatter) {
		String key = res.getAbsolutePath() + "|" + charset.name();
		synchronized (sharedFiles) {
			SharedFile shared = sharedFiles.get(key);
			if (shared == null) {
				shared = new SharedFile(key, new ResourceHandler(res, charset, maxFiles, maxFileSize, timeout, listener, formatter.getHead(null)));
				sharedFiles.put(key, shared);
			}
			shared.users++;
			return new SharedFileHandler(shared);
		}
	}

	private static Handler handler(Handler handler, Formatter formatter) {
		handler.setFormatter(formatter);
		handler.setLevel(Level.ALL);
		return handler;
	}

	@Override
	public void closeAppender(Object appender) throws PageException {
		if (appender instanceof Handler) ((Handler) appender).close();
	}

	@Override
	public Object getDefaultLayout() throws PageException {
		return new PatternFormatter(null);
	}

	@Override
	public Object getClassicLayout() throws PageException {
		return new ClassicFormatter(null);
	}

	private void warnOnce(String key, String message) {
		if (warned.put(key, Boolean.TRUE) == null) LogUtil.logGlobal(Log.LEVEL_WARN, "log-loading", message);
	}

	// ----------------------------------------------------------------------------------------------

	private static final class JULLog implements Log {

		private final Logger logger;
		private volatile int level;

		JULLog(Logger logger, int level) {
			this.logger = logger;
			this.level = level;
		}

		@Override
		public void log(int level, String application, String message) {
			log(level, application, message, null);
		}

		@Override
		public void log(int level, String application, Throwable t) {
			log(level, application, null, t);
		}

		@Override
		public void log(int level, String application, String message, Throwable t) {
			Level julLevel = toLevel(level);
			// the levels of Lucee are not ordered by severity (info is below debug), the logger level of JUL is
			if (!logger.isLoggable(julLevel)) return;
			// the message is not a format pattern, so it is not passed through the parameter substitution
			LogRecord record = new LogRecord(julLevel, message == null ? "" : message);
			record.setParameters(new Object[] { application, Thread.currentThread().getName() });
			record.setThrown(t);
			logger.log(record);
		}

		@Override
		public void trace(String application, String message) {
			log(LEVEL_TRACE, application, message, null);
		}

		@Override
		public void info(String application, String message) {
			log(LEVEL_INFO, application, message, null);
		}

		@Override
		public void debug(String application, String message) {
			log(LEVEL_DEBUG, application, message, null);
		}

		@Override
		public void warn(String application, String message) {
			log(LEVEL_WARN, application, message, null);
		}

		@Override
		public void error(String application, String message) {
			log(LEVEL_ERROR, application, message, null);
		}

		@Override
		public void error(String application, Throwable t) {
			log(LEVEL_ERROR, application, null, t);
		}

		@Override
		public void error(String application, String message, Throwable t) {
			log(LEVEL_ERROR, application, message, t);
		}

		@Override
		public void fatal(String application, String message) {
			log(LEVEL_FATAL, application, message, null);
		}

		@Override
		public int getLogLevel() {
			return level;
		}

		@Override
		public void setLogLevel(int level) {
			this.level = level;
			logger.setLevel(toLevel(level));
		}
	}

	private static String application(LogRecord r) {
		Object[] params = r.getParameters();
		return params != null && params.length > 0 && params[0] != null ? params[0].toString() : "";
	}

	private static String thread(LogRecord r) {
		Object[] params = r.getParameters();
		return params != null && params.length > 1 && params[1] != null ? params[1].toString() : "";
	}

	private static String message(LogRecord r) {
		String msg = r.getMessage() == null ? "" : r.getMessage();
		Throwable t = r.getThrown();
		if (t != null) {
			String em = t.getMessage();
			if (StringUtil.isEmpty(em, true)) em = t.getClass().getName();
			if (StringUtil.isEmpty(msg, true)) return em;
			if (em.indexOf(msg) != -1) return em;
			if (msg.indexOf(em) != -1) return msg;
			return em + ";" + msg;
		}
		return msg;
	}

	private static String fmt(String pattern, Config config, long time) {
		config = config != null ? config : ThreadLocalPageContext.getConfig();
		TimeZone tz = config != null && config.getTimeZone() != null ? config.getTimeZone() : TimeZone.getDefault();
		SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
		sdf.setTimeZone(tz);
		return sdf.format(new Date(time));
	}

	// same format as the classic layout of the Log4j extension
	private static final class ClassicFormatter extends Formatter {

		private static final String HEADER = "\"Severity\",\"ThreadID\",\"Date\",\"Time\",\"Application\",\"Message\"" + LINE_SEPARATOR;

		private final Config config;

		ClassicFormatter(Config config) {
			this.config = config;
		}

		@Override
		public String getHead(Handler h) {
			return HEADER;
		}

		@Override
		public String format(LogRecord r) {
			String msg = message(r);
			Throwable t = r.getThrown();
			String stacktrace = t != null ? ExceptionUtil.getStacktrace(t, false, true) : "";

			StringBuilder sb = new StringBuilder(64 + msg.length() + stacktrace.length());
			sb.append('"').append(r.getLevel().getName()).append("\",\"").append(thread(r)).append("\",\"");
			sb.append(fmt("MM/dd/yyyy", config, r.getMillis())).append("\",\"").append(fmt("HH:mm:ss", config, r.getMillis())).append("\",\"");
			escape(sb, application(r));
			sb.append("\",\"");
			if (t != null) {
				if (!StringUtil.isEmpty(msg, true) && !stacktrace.startsWith(msg)) {
					escape(sb, msg);
					sb.append(';');
				}
				escape(sb, stacktrace);
			}
			else escape(sb, msg);
			return sb.append('"').append(LINE_SEPARATOR).toString();
		}

		private static void escape(StringBuilder sb, String str) {
			for (int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);
				if (c == '"') sb.append('"');
				sb.append(c);
			}
		}
	}

	// dd.MM.yyyy HH:mm:ss,SSS LEVEL [application] message
	private static final class PatternFormatter extends Formatter {

		private final Config config;

		PatternFormatter(Config config) {
			this.config = config;
		}

		@Override
		public String format(LogRecord r) {
			StringBuilder sb = new StringBuilder();
			sb.append(fmt("dd.MM.yyyy HH:mm:ss,SSS", config, r.getMillis())).append(' ');
			String level = r.getLevel().getName();
			sb.append(level);
			for (int i = level.length(); i < 5; i++)
				sb.append(' ');
			sb.append(" [").append(application(r)).append("] ").append(message(r));
			if (r.getThrown() != null) sb.append(LINE_SEPARATOR).append(ExceptionUtil.getStacktrace(r.getThrown(), false, true));
			return sb.append(LINE_SEPARATOR).toString();
		}
	}

	private static final class ConsoleHandler extends Handler {

		private final Config config;
		private final boolean error;

		ConsoleHandler(Config config, boolean error) {
			this.config = config;
			this.error = error;
		}

		@Override
		public void publish(LogRecord record) {
			if (!isLoggable(record)) return;
			Config cfg = config != null ? config : ThreadLocalPageContext.getConfig();
			PrintWriter pw = cfg != null ? (error ? cfg.getErrWriter() : cfg.getOutWriter()) : null;
			String str = getFormatter().format(record);
			if (pw != null) {
				pw.write(str);
				pw.flush();
			}
			else (error ? System.err : System.out).print(str);
		}

		@Override
		public void flush() {
		}

		@Override
		public void close() {
		}
	}

	// not java.util.logging.FileHandler: when that rotates, the file in use is always generation 0 ("name.log.0") and a second one on
	// the same file locks itself out. Here the file in use is "name.log", the rotated files get the number, 1 is the newest.
	private static final class ResourceHandler {

		private final Resource res;
		private final Charset charset;
		private final int maxFiles;
		private final long maxFileSize;
		private final int timeout;
		private final RetireListener listener;
		private final String head;
		private final Object lock = new Object();
		private OutputStream os;
		private long size;

		ResourceHandler(Resource res, Charset charset, int maxFiles, long maxFileSize, int timeout, RetireListener listener, String head) {
			this.res = res;
			this.charset = charset == null ? StandardCharsets.UTF_8 : charset;
			this.maxFiles = maxFiles;
			this.maxFileSize = maxFileSize;
			this.timeout = timeout;
			this.listener = listener;
			this.head = head;
		}

		void write(String str) {
			byte[] data = str.getBytes(charset);
			synchronized (lock) {
				try {
					if (os == null) open();
					if (maxFileSize > 0 && size > 0 && size + data.length > maxFileSize) {
						closeStream();
						rotate();
						open();
					}
					os.write(data);
					os.flush();
					size += data.length;
				}
				catch (IOException ioe) {
					LogUtil.logGlobal(Log.LEVEL_ERROR, "log-loading", "failed to write to the log file [" + res + "]: " + ioe.getMessage());
				}
			}
		}

		private void open() throws IOException {
			Resource parent = res.getParentResource();
			if (parent != null && !parent.exists()) parent.mkdirs();
			boolean isNew = !res.exists() || res.length() == 0;
			size = isNew ? 0 : res.length();
			// closes the connection to the file when it was not used for a while, and opens it again with the next write
			os = new RetireOutputStream(res, true, timeout, listener);
			if (isNew && head != null && head.length() > 0) {
				byte[] header = head.getBytes(charset);
				os.write(header);
				size += header.length;
			}
		}

		private void rotate() {
			if (maxFiles <= 0) {
				res.delete();
				return;
			}
			Resource parent = res.getParentResource();
			String base = res.getName();
			try {
				Resource last = parent.getRealResource(base + "." + maxFiles);
				if (last.exists()) last.remove(false);
				for (int i = maxFiles - 1; i >= 1; i--) {
					Resource from = parent.getRealResource(base + "." + i);
					if (from.exists()) from.moveTo(parent.getRealResource(base + "." + (i + 1)));
				}
				res.moveTo(parent.getRealResource(base + ".1"));
			}
			catch (IOException ioe) {
				LogUtil.logGlobal(Log.LEVEL_ERROR, "log-loading", "failed to rotate the log file [" + res + "]: " + ioe.getMessage());
			}
		}

		private void closeStream() {
			if (os != null) {
				try {
					os.close();
				}
				catch (IOException ioe) {
				}
				os = null;
			}
		}

		void flush() {
			synchronized (lock) {
				if (os != null) {
					try {
						os.flush();
					}
					catch (IOException ioe) {
					}
				}
			}
		}

		void close() {
			synchronized (lock) {
				closeStream();
			}
		}
	}
}
