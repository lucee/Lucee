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
/**
 * Implements the CFML Function gettimezoneinfo
 */
package lucee.runtime.functions.international;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import lucee.commons.date.JREDateTimeUtil;
import lucee.runtime.PageContext;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public final class GetTimeZoneInfo implements Function {

	private static final long serialVersionUID = -5462276373169138909L;

	public static lucee.runtime.type.Struct call(PageContext pc) {
		return call(pc, null, null);
	}

	public static lucee.runtime.type.Struct call(PageContext pc, TimeZone tz) {
		return call(pc, tz, null);
	}

	public static lucee.runtime.type.Struct call(PageContext pc, TimeZone tz, Locale dspLocale) {
		if (tz == null) tz = pc.getTimeZone();
		if (dspLocale == null) dspLocale = pc.getLocale();
		// Date date = ;
		Calendar c = JREDateTimeUtil.getThreadCalendar(tz);
		c.setTimeInMillis(System.currentTimeMillis());

		int dstOffset = c.get(Calendar.DST_OFFSET);
		int total = c.get(Calendar.ZONE_OFFSET) / 1000 + dstOffset / 1000;
		total *= -1;
		int j = total / 60;
		int hour = total / 60 / 60;
		int minutes = j % 60;

		Struct struct = new StructImpl();
		struct.setEL("utcTotalOffset", new Double(total));
		struct.setEL("utcHourOffset", new Double(hour));
		struct.setEL("utcMinuteOffset", new Double(minutes));
		struct.setEL("isDSTon", (dstOffset > 0) ? Boolean.TRUE : Boolean.FALSE);
		struct.setEL(KeyConstants._name, tz.getDisplayName(dspLocale));
		struct.setEL("nameDST", tz.getDisplayName(Boolean.TRUE, TimeZone.LONG, dspLocale));
		struct.setEL(KeyConstants._shortName, tz.getDisplayName(Boolean.FALSE, TimeZone.SHORT, dspLocale));
		struct.setEL("shortNameDST", tz.getDisplayName(Boolean.TRUE, TimeZone.SHORT, dspLocale));
		struct.setEL(KeyConstants._id, tz.getID());
		struct.setEL(KeyConstants._timezone, tz.getID()); 
		struct.setEL(KeyConstants._offset, new Double(-total));
		struct.setEL("DSTOffset", new Double(dstOffset / 1000));

		return struct;

		// return new StructImpl();
	}
}
