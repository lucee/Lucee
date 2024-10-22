package lucee.commons.io.log.log4j2.layout;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

import lucee.commons.i18n.FormatUtil;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.log.LogUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.type.Struct;
import lucee.runtime.util.Cast;
import lucee.transformer.dynamic.meta.Method;

public class DataDogLayout extends AbstractStringLayout {
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private static final Class[] EMPTY_CLASS = new Class[0];
	private static final Object[] EMPTY_OBJ = new Object[0];
	private DateTimeFormatter format;
	private CFMLEngine engine;
	private Cast caster;

	private static Class<?> correlationIdentifierClass;
	private static Method getTraceId;
	private static Method getSpanId;
	private BIF serializeJSONBIF;
	private static Object[] ids;
	private static long idsTimestamp;
	private static int idsTries = 0;
	private static boolean idsValid;

	public DataDogLayout() {
		super(CharsetUtil.UTF8, new byte[0], new byte[0]);
		engine = CFMLEngineFactory.getInstance();
		caster = engine.getCastUtil();
		format = FormatUtil.getDateTimeFormatter(null, "yyyy-MM-dd HH:mm:ss").formatter;
	}

	@Override
	public String getContentType() {
		return super.getContentType();
	}

	@Override
	public String toSerializable(final LogEvent event) {

		StringBuilder data = new StringBuilder();

		data.append(FormatUtil.format(format, System.currentTimeMillis(), null));
		data.append(' ');
		data.append(event.getLevel().toString());
		data.append(' ');
		data.append(lucee.commons.io.log.log4j2.layout.Util.getLoggerName(event));
		data.append(':');
		data.append(caster.toString(getLineNumber()));
		data.append(" - ");

		Object[] id = getCorrelationIdentifier();
		data.append(id[0]);
		data.append(' ');
		data.append(id[1]);
		data.append(" - ");

		String application;
		String msg = caster.toString(event.getMessage(), null);
		int index = msg.indexOf("->");
		if (index > -1) {
			application = msg.substring(0, index);
			msg = msg.substring(index + 2);
		}
		else application = "";
		// StringUtil.replace(application, "\"", "\"\"", false)

		// Message
		if (msg == null && event.getMessage() != null) msg = event.getMessage().toString();

		// Throwable
		Throwable t = event.getThrown();
		if (t != null) {

			String em = getMessage(t);
			if (!Util.isEmpty(em, true)) {
				if (!em.trim().equals(msg.trim())) msg += ";" + em;
			}

			Struct sct = engine.getCreationUtil().createStruct();
			sct.setEL("message", msg);
			sct.setEL("stack", getStacktrace(t, false, true));
			sct.setEL("kind", t.getClass().getName());
			try {
				data.append(serializeJSON(sct));
			}
			catch (PageException e) {
				data.append(msg);
			}

		}
		else data.append(msg);

		return data.append(LINE_SEPARATOR).toString();

	}

	private String serializeJSON(Struct sct) throws PageException {
		boolean release = false;
		PageContext pc = engine.getThreadPageContext();
		if (pc == null) {
			try {
				pc = engine.createPageContext(

						engine.getCastUtil().toFile(engine.getResourceUtil().getTempDirectory()),

						"localhost",

						"/",

						"",

						null,

						null,

						null,

						null,

						new ByteArrayOutputStream(),

						-1,

						true

				);
				release = true;
			}
			catch (Exception e) {
				throw caster.toPageException(e);
			}
		}

		if (pc != null) {
			try {
				if (serializeJSONBIF == null) {
					serializeJSONBIF = engine.getClassUtil().loadBIF(pc, "lucee.runtime.functions.conversion.SerializeJSON");
				}
				return caster.toString(serializeJSONBIF.invoke(pc, new Object[] { sct }));
			}
			catch (Exception e) {
				throw caster.toPageException(e);
			}
			finally {
				if (release) engine.releasePageContext(pc, true);
			}
		}
		throw engine.getExceptionUtil().createApplicationException("no PageContext available for the current thread and could not create one");
	}

	private static Object[] getCorrelationIdentifier() {

		if (idsValid) return ids;

		long now = System.currentTimeMillis();
		if (ids != null) {
			// if we have less than 300 tries, we try once a second (so for 5 minutes) after that every minute
			if (idsTries < 300 && idsTimestamp + 1000 > now) return ids;
			if (idsTries > 300 && idsTimestamp + 300000 > now) return ids;

		}

		idsTries++;
		idsTimestamp = now;
		try {
			if (correlationIdentifierClass == null) {
				getTraceId = null;
				correlationIdentifierClass = CFMLEngineFactory.getInstance().getClassUtil().loadClass("datadog.trace.api.CorrelationIdentifier");
			}

			// CorrelationIdentifier.getTraceId()
			if (getTraceId == null) {
				getTraceId = Reflector.getMethod(correlationIdentifierClass, "getTraceId", EMPTY_CLASS);
			}

			// CorrelationIdentifier.getSpanId()
			if (getSpanId == null) {
				getSpanId = Reflector.getMethod(correlationIdentifierClass, "getSpanId", EMPTY_CLASS);
			}
			Object[] tmp = new Object[] { getTraceId.invoke(null, EMPTY_OBJ), getSpanId.invoke(null, EMPTY_OBJ) };

			if (!"0".equals(tmp[0])) {
				ids = tmp;
				idsValid = true;
				return ids;
			}
			return ids = new Object[] { "0", "0" };
		}
		catch (Exception e) {
			// we cannot send this to a logger, because that could cause an infiniti loop
			try {
				LogUtil.logGlobal(null, "datadog", e);
			}
			catch (Exception ee) {
				e.printStackTrace();
			}
		}

		return ids = new Object[] { "-1", "-1" };
	}

	public static Object[] getCorrelationIdentifierWhenValid() {
		Object[] _ids = getCorrelationIdentifier();
		if (idsValid) return _ids;
		return null;
	}

	public int getLineNumber() {
		int line = 0;
		String template;

		for (StackTraceElement trace: Thread.currentThread().getStackTrace()) {
			template = trace.getFileName();
			if (trace.getLineNumber() <= 0 || template == null || engine.getResourceUtil().getExtension(template, "").equals("java")) continue;
			line = trace.getLineNumber();
			if (line > 0) return line;
		}
		return 0;
	}

	public static String getMessage(Throwable t) {
		String msg = t.getMessage();
		if (Util.isEmpty(msg, true)) msg = t.getClass().getName();

		StringBuilder sb = new StringBuilder(msg);

		if (t instanceof PageException) {
			PageException pe = (PageException) t;
			String detail = pe.getDetail();
			if (!Util.isEmpty(detail, true)) {
				sb.append('\n');
				sb.append(detail);
			}
		}
		return sb.toString();
	}

	public static String getStacktrace(Throwable t, boolean addMessage, boolean onlyLuceePart) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.close();
		String st = sw.toString();
		// shrink the stacktrace
		if (onlyLuceePart && st.indexOf("Caused by:") == -1) {
			int index = st.indexOf("lucee.loader.servlet.CFMLServlet.service(");
			if (index == -1) index = st.indexOf("lucee.runtime.jsr223.ScriptEngineImpl.eval(");

			if (index != -1) {
				index = st.indexOf(")", index + 1);
				if (index != -1) {
					st = st.substring(0, index + 1) + "\n...";
				}
			}
		}

		String msg = t.getMessage();
		if (addMessage && !Util.isEmpty(msg) && !st.startsWith(msg.trim())) st = msg + "\n" + st;
		return st;

	}
}