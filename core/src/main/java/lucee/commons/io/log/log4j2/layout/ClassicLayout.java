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
import java.util.TimeZone;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.pattern.DatePatternConverter;
import org.apache.logging.log4j.message.Message;

import lucee.commons.io.log.log4j2.ContextualMessage;
import lucee.commons.io.log.log4j2.LogAdapter;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.loader.util.Util;
import lucee.runtime.exp.PageException;

public final class ClassicLayout extends AbstractStringLayout { // TODO <Serializable>

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	// quotes, commas, date (10), time (8), line separator, plus slack for doubled quotes
	private static final int FIXED_OVERHEAD = 64;

	private final DatePatternConverter date;
	private final DatePatternConverter time;

	public ClassicLayout() {
		// TODO custom charset?
		super(StandardCharsets.UTF_8, (LogAdapter.logWebContextInfo ?

				"\"Severity\",\"ThreadID\",\"Date\",\"Time\",\"Context\",\"Application\",\"Message\"" + LINE_SEPARATOR
				: "\"Severity\",\"ThreadID\",\"Date\",\"Time\",\"Application\",\"Message\"" + LINE_SEPARATOR).getBytes(StandardCharsets.UTF_8), new byte[0]);

		// zone resolved once for the life of the layout, log4j caches everything above second precision
		String tz = TimeZone.getDefault().getID();
		date = DatePatternConverter.newInstance( new String[] { "MM/dd/yyyy", tz, "en-US" } );
		time = DatePatternConverter.newInstance( new String[] { "HH:mm:ss", tz, "en-US" } );
	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return super.getContentType();
	}

	@Override
	public String toSerializable(final LogEvent event) {
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

		Throwable t = event.getThrown();
		String level = event.getLevel().toString();
		String thread = event.getThreadName();
		msg = getFormattedMessage( t, msg );
		String stacktrace = t != null ? ExceptionUtil.getStacktrace( t, false, true ) : "";

		StringBuilder data = new StringBuilder( FIXED_OVERHEAD + level.length() + thread.length() + context.length() + application.length() + msg.length() + stacktrace.length() );

		data.append('"').append( level ).append('"');
		data.append(',');
		data.append('"').append( thread ).append('"');
		data.append(',');

		// Date
		data.append('"');
		date.format( event, data );
		data.append('"');
		data.append(',');

		// Time
		data.append('"');
		time.format( event, data );
		data.append('"');
		data.append(',');

		// Context
		if (LogAdapter.logWebContextInfo) {
			appendQuoted( data, context );
			data.append(',');
		}

		// Application
		appendQuoted( data, application );
		data.append(',');

		// Message
		data.append('"');
		if (t != null) {
			// the stack trace starts with the class name and message of the exception, so only add the message
			// in front when it says something more, and separate the two (LDEV-6119, LDEV-6214)
			if (!StringUtil.isEmpty(msg, true) && !stacktrace.startsWith(msg)) {
				appendEscaped( data, msg );
				data.append(';');
			}
			appendEscaped( data, stacktrace );
		}
		else appendEscaped( data, msg );
		data.append('"');

		return data.append(LINE_SEPARATOR).toString();
	}

	private static void appendQuoted( StringBuilder sb, String str ) {
		sb.append('"');
		appendEscaped( sb, str );
		sb.append('"');
	}

	// CSV escaping, a double quote becomes two, single scan, no intermediate string
	private static void appendEscaped( StringBuilder sb, String str ) {
		int len = str.length();
		for ( int i = 0; i < len; i++ ) {
			char c = str.charAt( i );
			if ( c == '"' ) sb.append( '"' );
			sb.append( c );
		}
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