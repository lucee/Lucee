package lucee.commons.io.log.log4j2.layout;

import java.nio.charset.StandardCharsets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.message.Message;

import lucee.commons.i18n.FormatUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.log.log4j2.ContextualMessage;
import lucee.commons.io.log.log4j2.LogAdapter;
import lucee.commons.lang.ExceptionUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.util.Cast;
import lucee.transformer.dynamic.meta.Method;

public final class DataDogLayout extends AbstractStringLayout {
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
		super(StandardCharsets.UTF_8, new byte[0], new byte[0]);
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

		String msg, application, context;
		Message message = event.getMessage();
		if (message instanceof ContextualMessage) {
			ContextualMessage cm = (ContextualMessage) message;
			msg = cm.getFormattedMessage();
			application = cm.getApplication();
			context = cm.getContext();
		}
		else {
			msg = message != null ? message.getFormattedMessage() : "";
			application = "";
			context = "";
		}

		// Throwable
		Throwable t = event.getThrown();
		Struct sct = engine.getCreationUtil().createStruct(StructImpl.TYPE_LINKED);
		if (LogAdapter.logWebContextInfo) sct.setEL(KeyConstants._context, context);
		sct.setEL(KeyConstants._application, application);
		sct.setEL(KeyConstants._message, ClassicLayout.getFormattedMessage(t, msg));
		sct.setEL("stack", getStacktrace(t, false, true));
		sct.setEL("kind", t.getClass().getName());
		try {
			data.append(serializeJSON(sct));
		}
		catch (PageException e) {
			data.append(msg);
		}

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
				getTraceId = Reflector.getMethod(correlationIdentifierClass, "getTraceId", EMPTY_CLASS, true);
			}

			// CorrelationIdentifier.getSpanId()
			if (getSpanId == null) {
				getSpanId = Reflector.getMethod(correlationIdentifierClass, "getSpanId", EMPTY_CLASS, true);
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
			IOException ioe = new IOException(
					"Datadog classes not found - this is optional and does not affect application functionality. Logging will continue with placeholder TraceId/SpanId values (-1/-1). To enable Datadog tracing, add the datadog-trace-api JAR to the classpath.");
			try {
				ExceptionUtil.initCauseEL(ioe, e);
				LogUtil.logGlobal(CFMLEngineFactory.getInstance().getCFMLEngineFactory(), "datadog", ioe);
			}
			catch (Exception ee) {
				ioe.printStackTrace();
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