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

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

public class XMLLayout extends AbstractStringLayout { // TODO <Serializable>

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private static final String ROOT_TAG = "Events";
	private static final String XML_NAMESPACE = "http://logging.apache.org/log4j/2.0/events";
	private boolean doLocationInfo;

	// private static final DateFormat dateFormat = new DateFormat(Locale.US);
	// private static final TimeFormat timeFormat = new TimeFormat(Locale.US);

	public XMLLayout(Charset cs, boolean complete, boolean doLocationInfo) {
		super(cs, createHeader(cs, complete), createFooter(cs, complete));
		this.doLocationInfo = doLocationInfo;
	}

	private static byte[] createHeader(Charset cs, boolean complete) {
		if (!complete) {
			return null;
		}
		// .getBytes(CharsetUtil.UTF8)
		final StringBuilder buf = new StringBuilder();
		buf.append("<?xml version=\"1.0\" encoding=\"");
		buf.append(cs.name());
		buf.append("\"?>");
		buf.append(LINE_SEPARATOR);

		buf.append('<');
		buf.append(ROOT_TAG);
		buf.append(" xmlns=\"" + XML_NAMESPACE + "\">");
		buf.append(LINE_SEPARATOR);
		return buf.toString().getBytes(cs);
	}

	private static byte[] createFooter(Charset cs, boolean complete) {
		if (!complete) {
			return null;
		}

		final StringBuilder buf = new StringBuilder();
		buf.append("</");
		buf.append(ROOT_TAG);
		buf.append('>');
		buf.append(LINE_SEPARATOR);
		return buf.toString().getBytes(cs);
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

		final StringBuilder buf = new StringBuilder();
		buf.append("	<log4j:event logger=\"");
		buf.append(event.getLoggerName());
		buf.append("\" timestamp=\"");
		buf.append(System.currentTimeMillis());
		buf.append("\" level=\"");
		buf.append(event.getLevel().name());
		buf.append("\" thread=\"");
		buf.append(Thread.currentThread().getName());
		buf.append("\">");
		buf.append(LINE_SEPARATOR);

		buf.append("		<log4j:message>");
		buf.append(createCDATASection(event.getMessage().toString())); // TODO cdata escape
		buf.append("</log4j:message>");
		buf.append(LINE_SEPARATOR);

		if (doLocationInfo) {
			StackTraceElement data = null;
			for (StackTraceElement ste: Thread.currentThread().getStackTrace()) {
				if (ste.getClassName().startsWith("lucee.commons.io.log.")) continue;
				if (ste.getClassName().startsWith("org.apache.logging.log4j.")) continue;
				if (ste.getClassName().equals("lucee.runtime.tag.Log")) continue;
				data = ste;
			}
			if (data != null) {
				buf.append("		<log4j:locationInfo class=\"");
				buf.append(data.getClassName());
				buf.append("\" method=\"");
				buf.append(data.getMethodName());
				buf.append("\" file=\"");
				buf.append("LogAppender.java");
				buf.append("\" line=\"");
				buf.append(data.getLineNumber());
				buf.append("\"/>");
				buf.append(LINE_SEPARATOR);
			}
		}

		buf.append("	</log4j:event>");
		buf.append(LINE_SEPARATOR);

		return buf.toString();

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