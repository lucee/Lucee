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

import java.util.Locale;
import java.util.TimeZone;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

import lucee.commons.io.CharsetUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.format.DateFormat;
import lucee.runtime.format.TimeFormat;
import lucee.runtime.op.Caster;

public class ClassicLayout extends AbstractStringLayout { // TODO <Serializable>

	public ClassicLayout() {
		// TODO custom charset?
		super(CharsetUtil.UTF8, ("\"Severity\",\"ThreadID\",\"Date\",\"Time\",\"Application\",\"Message\"" + LINE_SEPARATOR).getBytes(CharsetUtil.UTF8), new byte[0]);
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
		String application;

		String msg = Caster.toString(event.getMessage(), null);
		int index = msg.indexOf("->");
		if (index > -1) {
			application = msg.substring(0, index);
			msg = msg.substring(index + 2);
		}
		else application = "";

		// if(!ArrayUtil.isEmpty(params))
		// application=Caster.toString(params[0],"");
		// Severity
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

		// Application
		data.append('"');
		data.append(StringUtil.replace(application, "\"", "\"\"", false));
		data.append('"');

		data.append(',');

		// Message
		data.append('"');
		if (msg == null && event.getMessage() != null) msg = event.getMessage().toString();
		data.append(StringUtil.replace(msg, "\"", "\"\"", false));
		Throwable t = event.getThrown();
		if (t != null) {
			data.append(';');
			String em = ExceptionUtil.getMessage(t);
			data.append(StringUtil.replace(em, "\"", "\"\"", false));
			data.append(';');
			String est = ExceptionUtil.getStacktrace(t, false, true);
			data.append(StringUtil.replace(est, "\"", "\"\"", false));
		}

		data.append('"');

		return data.append(LINE_SEPARATOR).toString();

	}

}