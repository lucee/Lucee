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
package lucee.commons.io.log.log4j2.layout;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.ThreadContext.ContextStack;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.time.Instant;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MultiformatMessage;

import lucee.commons.lang.ExceptionUtil;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.PageContext;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.security.Credential;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.util.Creation;

public class JsonLayout extends AbstractStringLayout { // TODO <Serializable>

	private static final int DEFAULT_SIZE = 256;
	private static final String[] FORMATS = new String[] { "json" };

	private final boolean includeStacktrace;
	private final boolean includeTimeMillis;
	private final boolean stacktraceAsString;
	private final boolean locationInfo;
	private final boolean properties;
	private boolean doComma = true;
	private final Charset charset;
	private boolean compact;

	// private static final DateFormat dateFormat = new DateFormat(Locale.US);
	// private static final TimeFormat timeFormat = new TimeFormat(Locale.US);

	public JsonLayout(Charset charset, boolean complete, boolean compact, boolean includeStacktrace, boolean includeTimeMillis, boolean stacktraceAsString, boolean locationInfo,
			boolean properties) {
		super(charset, createHeader(charset, complete), createFooter(charset, complete));

		this.charset = charset;
		this.includeStacktrace = includeStacktrace;
		this.includeTimeMillis = includeTimeMillis;
		this.stacktraceAsString = stacktraceAsString;
		this.locationInfo = locationInfo;
		this.properties = properties;

		this.compact = compact;

	}

	@Override
	public byte[] getHeader() {
		doComma = false;
		return super.getHeader();

	}

	private static byte[] createHeader(Charset cs, boolean complete) {
		if (!complete) return new byte[] {};
		return "[".getBytes(cs);
	}

	private static byte[] createFooter(Charset cs, boolean complete) {
		if (!complete) return new byte[] {};
		return "]".getBytes(cs);
	}

	@Override
	public String getContentType() {
		return "text/xml; charset=" + this.getCharset();
	}

	@Override
	public Map<String, String> getContentFormat() {
		final Map<String, String> result = new HashMap<>();
		// result.put("dtd", "log4j-events.dtd");
		result.put("xsd", "log4j-events.xsd");
		result.put("version", "2.0");
		return result;
	}

	@Override
	public String toSerializable(final LogEvent event) {
		final StringBuilder buf = new StringBuilder(DEFAULT_SIZE);

		// long now = System.currentTimeMillis();
		Creation util = CFMLEngineFactory.getInstance().getCreationUtil();
		try {
			Struct root = util.createStruct("linked");
			long now = event.getTimeMillis();
			// timeMillis
			if (includeTimeMillis) {
				root.setEL("timeMillis", now);
			}
			// instant
			else {
				Instant instant = event.getInstant();
				Struct sct = util.createStruct("linked");
				root.setEL("instant", sct);
				sct.set("epochSecond", instant.getEpochSecond());
				sct.set("nanoOfSecond", instant.getNanoOfSecond());
			}

			root.setEL("thread", event.getThreadName());
			root.setEL("level", event.getLevel().name());

			// name
			String name = event.getLoggerName();
			if (Util.isEmpty(name)) {
				name = "root";
			}
			root.setEL("loggerName", name);

			// marker
			Marker marker = event.getMarker();
			if (marker != null) {
				root.setEL("marker", createMarker(util, marker));

			}

			// Message
			Message msg = event.getMessage();
			if (msg != null) {
				boolean jsonSupported = false;
				if (msg instanceof MultiformatMessage) {
					final String[] formats = ((MultiformatMessage) msg).getFormats();
					for (final String format: formats) {
						if (format.equalsIgnoreCase("JSON")) {
							jsonSupported = true;
							break;
						}
					}
				}
				if (jsonSupported) {
					root.setEL("message", ((MultiformatMessage) msg).getFormattedMessage(FORMATS));
				}
				else {
					root.setEL("message", msg.getFormattedMessage());
				}
			}

			// Thrown
			Throwable thrown = event.getThrown();
			if (thrown != null) {
				Struct sct = util.createStruct("linked");
				root.setEL("thrown", sct);
				sct.setEL("commonElementCount", 0D); // TODO
				sct.setEL("message", thrown.getMessage());
				sct.setEL("name", thrown.getClass().getName());
				if (includeStacktrace) {
					root.setEL("extendedStackTrace", createStacktrace(util, thrown.getStackTrace(), stacktraceAsString));
				}
			}

			// context stack
			{
				Array contextStack = util.createArray();
				root.setEL("contextStack", contextStack);
				ContextStack stack = event.getContextStack();
				if (stack.getDepth() > 0) {
					for (String cse: stack.asList()) {
						contextStack.append(cse);
					}

				}
			}
			// end of batch
			root.setEL("endOfBatch", event.isEndOfBatch());

			// TODO "loggerFqcn" : "org.apache.logging.log4j.spi.AbstractLogger",

			// context map
			if (this.properties) {
				Map<String, String> map = event.getContextMap();
				if (map.size() > 0) {
					Struct contextMap = util.createStruct("linked");
					root.setEL("contextMap", contextMap);
					for (Entry<String, String> e: map.entrySet()) {
						contextMap.setEL(e.getKey(), e.getValue());
					}
				}
			}

			// thread
			{
				Thread thread = Thread.currentThread();
				root.setEL("threadId", thread.getId());
				root.setEL("threadId", thread.getPriority());
			}

			if (this.locationInfo) {
				StackTraceElement data = null;
				for (StackTraceElement ste: Thread.currentThread().getStackTrace()) {
					if (ste.getClassName().startsWith("lucee.commons.io.log.")) continue;
					if (ste.getClassName().startsWith("org.apache.logging.log4j.")) continue;
					if (ste.getClassName().equals("lucee.runtime.tag.Log")) continue;
					data = ste;
				}
				if (data != null) {
					Struct source = util.createStruct("linked");
					root.setEL("source", source);
					source.setEL("class", data.getClassName());
					source.setEL("method", data.getMethodName());
					source.setEL("file", data.getFileName());
					source.setEL("line", data.getLineNumber());
				}
			}
			// datadog
			Object[] ddids = DataDogLayout.getCorrelationIdentifierWhenValid();
			if (ddids != null && ddids.length == 2) {
				Struct ids = util.createStruct("linked");
				ids.set("traceId", ddids[0]);
				ids.set("spanId", ddids[1]);
				root.setEL("correlationIdentifier", ids);
			}

			// auth user
			PageContext pc = ThreadLocalPageContext.get();
			if (pc != null) {
				String user = null;
				Credential remoteUser = pc.getRemoteUser();
				if (remoteUser == null) {
					user = pc.getHttpServletRequest().getRemoteUser();
				}
				else user = remoteUser.getUsername();
				if (!Util.isEmpty(user, true)) root.setEL("authUser", user);
			}

			// Properties
			/*
			 * if (this.properties && event.getContextMap().size() > 0) { Array arr = util.createArray();
			 * sct.setEL("Properties", arr);
			 * 
			 * Set<Entry<String, String>> entrySet = event.getContextMap().entrySet(); int i = 1; for (final
			 * Map.Entry<String, String> entry: entrySet) { Struct s = util.createStruct("linked");
			 * arr.appendEL(s); sct.setEL("name", entry.getKey()); sct.setEL("value", entry.getValue()); } }
			 */

			try {
				JSONConverter json = new JSONConverter(true, charset, JSONDateFormat.PATTERN_ISO8601, compact, null);

				String result = json.serialize(null, root, -1, Boolean.TRUE);
				if (doComma) return "," + result;
				else doComma = true;
				return result;
			}
			catch (ConverterException e) {
				throw Caster.toPageException(e);
			}

		}
		catch (PageException e) {
			throw Caster.toPageRuntimeException(e);
		}

	}

	private Object createStacktrace(Creation util, StackTraceElement[] stackTraces, boolean stacktraceAsString) throws PageException {

		// Stacktrace
		//
		if (stacktraceAsString) {
			return ExceptionUtil.toString(stackTraces);
		}
		else {
			Array arr = util.createArray();
			Struct sct;
			for (StackTraceElement ste: stackTraces) {
				sct = util.createStruct("linked");
				arr.appendEL(sct);
				sct.setEL("class", ste.getClassName());
				sct.setEL("method", ste.getMethodName());
				sct.setEL("class", ste.getClassName());
				sct.setEL("file", ste.getFileName());
				sct.setEL("line", ste.getLineNumber());
				sct.setEL("exact", Boolean.TRUE);
				sct.setEL("location", "classes/");
				sct.setEL("version", "?");
				// TODO exact location version
			}
			return arr;
		}
	}

	private Struct createMarker(Creation util, Marker marker) throws PageException {
		Struct sct = util.createStruct("linked");
		sct.setEL("name", marker.getName());
		Marker[] parents = marker.getParents();
		if (parents != null && parents.length > 0) {
			Array arr = util.createArray();
			sct.setEL("parents", arr);
			for (Marker p: parents) {
				arr.appendEL(createMarker(util, p));
			}
		}
		return sct;
	}

	private static String createCDATASection(String str) {

		final StringBuilder buf = new StringBuilder("<![CDATA[");

		int index, lastIndex = 0;

		while ((index = str.indexOf("]]>", lastIndex)) != -1) {
			buf.append(str.substring(lastIndex, index)).append("]]]]><![CDATA[>");
			lastIndex = index + 3;
		}

		return buf.append(str.substring(lastIndex)).append("]]>").toString();
	}
}