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

import java.nio.charset.StandardCharsets;

import java.util.Locale;
import java.util.TimeZone;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.message.Message;

import lucee.commons.io.log.log4j2.ContextualMessage;
import lucee.commons.io.log.log4j2.LogAdapter;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.loader.util.Util;
import lucee.runtime.exp.PageException;
import lucee.runtime.format.DateFormat;
import lucee.runtime.format.TimeFormat;

public final class ClassicLayout extends AbstractStringLayout { // TODO <Serializable>

	public ClassicLayout() {
		// TODO custom charset?
		super(StandardCharsets.UTF_8, (LogAdapter.logWebContextInfo ?

				"\"Severity\",\"ThreadID\",\"Date\",\"Time\",\"Context\",\"Application\",\"Message\"" + LINE_SEPARATOR
				: "\"Severity\",\"ThreadID\",\"Date\",\"Time\",\"Application\",\"Message\"" + LINE_SEPARATOR).getBytes(StandardCharsets.UTF_8), new byte[0]);
	}

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	private static final DateFormat dateFormat = new DateFormat(Locale.US);
	private static final TimeFormat timeFormat = new TimeFormat(Locale.US);

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return super.getContentType();
	}

	@Override
	public String toSerializable(final LogEvent event) {

		StringBuilder data = new StringBuilder();
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
			context = ContextualMessage.getLabel();
		}
		data.append('"');
		data.append(event.getLevel().toString());
		data.append('"');

		data.append(',');

		data.append('"');
		data.append(event.getThreadName());
		data.append('"');

		data.append(',');

		// Date
		data.append('"');

		data.append(dateFormat.format(event.getTimeMillis(), "mm/dd/yyyy", TimeZone.getDefault()));
		data.append('"');

		data.append(',');

		// Time
		data.append('"');
		data.append(timeFormat.format(event.getTimeMillis(), "HH:mm:ss", TimeZone.getDefault()));
		data.append('"');

		data.append(',');

		// Context
		if (LogAdapter.logWebContextInfo) {
			data.append('"');
			data.append(StringUtil.replace(context, "\"", "\"\"", false));
			data.append('"');
			data.append(',');
		}

		// Application
		data.append('"');
		data.append(StringUtil.replace(application, "\"", "\"\"", false));
		data.append('"');

		data.append(',');

		// Message
		Throwable t = event.getThrown();
		data.append('"');
		data.append(StringUtil.replace(getFormattedMessage(t, msg), "\"", "\"\"", false));
		if (t != null) {
			String est = ExceptionUtil.getStacktrace(t, false, true);
			data.append(StringUtil.replace(est, "\"", "\"\"", false));
		}
		data.append('"');

		return data.append(LINE_SEPARATOR).toString();

	}

	public static String getFormattedMessage(Throwable throwable, String message) {
		if (throwable != null) {

			String expMessage = throwable.getMessage();
			if (throwable instanceof PageException) {
				PageException pe = (PageException) throwable;
				String detail = pe.getDetail();
				if (!Util.isEmpty(detail, true)) {
					expMessage += "\n" + detail;
					expMessage = expMessage.trim();
				}
			}
			if (StringUtil.isEmpty(expMessage, true)) expMessage = throwable.getClass().getName();
			if (StringUtil.isEmpty(message, true)) {
				return expMessage;
			}
			if (expMessage.indexOf(message) != -1) return expMessage;
			else if (message.indexOf(expMessage) != -1) return message;
			return expMessage + ";" + message;

		}
		return message;
	}
}