package lucee.commons.io.log.log4j2.layout;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.Cookie;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

import lucee.commons.io.CharsetUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.type.Struct;
import lucee.runtime.util.Cast;

public class DataDogLayout extends AbstractStringLayout {
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private static final Class[] EMPTY_CLASS = new Class[0];
	private static final Object[] EMPTY_OBJ = new Object[0];
	private DateFormat format;
	private CFMLEngine engine;
	private Cast caster;

	private static Class<?> correlationIdentifierClass;
	private static Method getTraceId;
	private static Method getSpanId;
	private BIF serializeJSONBIF;
	private static Object[] ids;

	public DataDogLayout() {
		super(CharsetUtil.UTF8, new byte[0], new byte[0]);
		engine = CFMLEngineFactory.getInstance();
		caster = engine.getCastUtil();
		format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	@Override
	public String getContentType() {
		return super.getContentType();
	}

	@Override
	public String toSerializable(final LogEvent event) {

		StringBuilder data = new StringBuilder();

		data.append(format.format(new Date()));
		data.append(' ');
		data.append(event.getLevel().toString());
		data.append(' ');
		data.append(getLoggerName(event));
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

	private Object getLoggerName(LogEvent event) {
		String name = event.getLoggerName();
		if (name.startsWith("web.")) {
			int index = name.indexOf('.', 4);
			if (index != -1) name = name.substring(index + 1);
		}
		else if (name.startsWith("server.")) {
			name = name.substring(7);
		}

		return name;
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

						new Cookie[0],

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

		if (ids != null) return ids;

		try {
			if (correlationIdentifierClass == null) {
				getTraceId = null;
				correlationIdentifierClass = CFMLEngineFactory.getInstance().getClassUtil().loadClass("datadog.trace.api.CorrelationIdentifier");
			}

			// CorrelationIdentifier.getTraceId()
			if (getTraceId == null) {
				getTraceId = correlationIdentifierClass.getMethod("getTraceId", EMPTY_CLASS);
			}

			// CorrelationIdentifier.getSpanId()
			if (getSpanId == null) {
				getSpanId = correlationIdentifierClass.getMethod("getSpanId", EMPTY_CLASS);
			}
			ids = new Object[] { getTraceId.invoke(null, EMPTY_OBJ), getSpanId.invoke(null, EMPTY_OBJ) };
			return ids;
		}
		catch (Exception e) {
			// we cannot send this to a logger, because that could cause an infiniti loop
			e.printStackTrace();
		}

		return ids = new Object[] { "-1", "-1" };
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