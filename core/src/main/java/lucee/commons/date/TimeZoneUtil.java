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
package lucee.commons.date;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ListUtil;

public class TimeZoneUtil {

	private static final Map<String, Object> IDS = new HashMap<String, Object>();
	private static Map<String, TimeZone> dn = new HashMap<String, TimeZone>();

	static {
		String[] ids = TimeZone.getAvailableIDs();
		for (int i = 0; i < ids.length; i++) {
			IDS.put(ids[i].toLowerCase(), TimeZone.getTimeZone(ids[i]));
		}
		TimeZone def = TimeZone.getDefault();
		if (def != null) IDS.put(def.getID(), def);
		IDS.put("jvm", TimeZone.getDefault());
		IDS.put("default", TimeZone.getDefault());
		IDS.put("", TimeZone.getDefault());

		// MS specific Timezone definitions
		set("Dateline Standard Time", "Etc/GMT+12");
		set("Samoa Standard Time", "Pacific/Midway");
		set("Hawaiian Standard Time", "HST");
		set("Alaskan Standard Time", "AST");
		set("Pacific Standard Time", "PST");
		set("Mountain Standard Time", "MST");
		set("Mexico Standard Time", "Mexico/General");
		set("Mexico Standard Time 2", "America/Chihuahua");
		set("U.S. Mountain Standard Time", "MST");
		set("Central Standard Time", "CST");
		set("Canada Central Standard Time", "Canada/Central");
		set("Central America Standard Time", "CST");
		set("Eastern Standard Time", "EST");
		set("U.S. Eastern Standard Time", "EST");
		set("S.A. Pacific Standard Time", "America/Bogota");
		set("Atlantic Standard Time", "Canada/Atlantic");
		set("S.A. Western Standard Time", "America/Antigua");
		set("Pacific S.A. Standard Time", "America/Santiago");
		set("Newfoundland and Labrador Standard Time", "CNT");
		set("E. South America Standard Time", "BET");
		set("S.A. Eastern Standard Time", "America/Argentina/Buenos_Aires");
		set("Greenland Standard Time", "America/Godthab");
		set("Mid-Atlantic Standard Time", "America/Noronha");
		set("Azores Standard Time", "Atlantic/Azores");
		set("Cape Verde Standard Time", "Atlantic/Cape_Verde");
		set("Central Europe Standard Time", "CET");
		set("Central European Standard Time", "CET");
		set("Romance Standard Time", "Europe/Brussels");
		set("W. Europe Standard Time", "CET");
		set("E. Europe Standard Time", "ART");
		set("Egypt Standard Time", "Egypt");
		set("FLE Standard Time", "EET");
		set("GTB Standard Time", "Europe/Athens");
		set("Israel Standard Time", "Asia/Jerusalem");
		set("South Africa Standard Time", "Africa/Johannesburg");
		set("Russian Standard Time", "Europe/Moscow");
		set("Arab Standard Time", "Asia/Kuwait");
		set("E. Africa Standard Time", "Africa/Nairobi");
		set("Arabic Standard Time", "Asia/Baghdad");
		set("Iran Standard Time", "Asia/Tehran");
		set("Arabian Standard Time", "Asia/Muscat");
		set("Caucasus Standard Time", "Asia/Yerevan");
		set("Transitional Islamic State of Afghanistan Standard Time", "Asia/Kabul");
		set("Ekaterinburg Standard Time", "Asia/Yekaterinburg");
		set("West Asia Standard Time", "Asia/Karachi");
		set("India Standard Time", "IST");
		set("Nepal Standard Time", "Asia/Katmandu");
		set("Central Asia Standard Time", "Asia/Dhaka");
		set("Sri Lanka Standard Time", "Asia/Colombo");
		set("N. Central Asia Standard Time", "Asia/Almaty");
		set("Myanmar Standard Time", "Asia/Rangoon");
		set("S.E. Asia Standard Time", "Asia/Bangkok");
		set("North Asia Standard Time", "Asia/Krasnoyarsk");
		set("China Standard Time", "CTT");
		set("Singapore Standard Time", "Asia/Singapore");
		set("Taipei Standard Time", "Asia/Taipei");
		set("W. Australia Standard Time", "Australia/Perth");
		set("North Asia East Standard Time", "Asia/Irkutsk");
		set("Korea Standard Time", "Asia/Seoul");
		set("Tokyo Standard Time", "Asia/Tokyo");
		set("Yakutsk Standard Time", "Asia/Yakutsk");
		set("A.U.S. Central Standard Time", "ACT");
		set("Cen. Australia Standard Time", "ACT");
		set("A.U.S. Eastern Standard Time", "AET");
		set("E. Australia Standard Time", "AET");
		set("Tasmania Standard Time", "Australia/Tasmania");
		set("Vladivostok Standard Time", "Asia/Vladivostok");
		set("West Pacific Standard Time", "Pacific/Guam");
		set("Central Pacific Standard Time", "Asia/Magadan");
		set("Fiji Islands Standard Time", "Pacific/Fiji");
		set("New Zealand Standard Time", "NZ");
		set("Tonga Standard Time", "Pacific/Tongatapu");
		set("CEST", "CET");
		set("ACDT", "ACT");
		set("ACST", "Australia/Eucla");
		set("ACST", "Australia/Tasmania");
		set("AEST", "Australia/Queensland");
		set("ET", "US/Eastern");
		set("EDT", "US/Eastern");
		set("EST", "US/Eastern");
		set("MT", "US/Mountain");
		set("MST", "US/Mountain");
		set("MDT", "US/Mountain");
		set("CT", "US/Central");
		set("CST", "US/Central");
		set("CDT", "US/Central");
		set("PT", "US/Pacific");
		set("PST", "US/Pacific");
		set("PDT", "US/Pacific");

	}

	private static void set(String name, String ID) {
		if (StringUtil.isEmpty(ID)) return;
		name = StringUtil.replace(name.trim().toLowerCase(), " ", "", false);
		IDS.put(name.toLowerCase(), ID);
	}

	/**
	 * return the string format of the Timezone
	 * 
	 * @param timezone
	 * @return
	 */
	public static String toString(TimeZone timezone) {
		return timezone.getID();
	}

	private static String getSupportedTimeZonesAsString() {
		return ListUtil.arrayToList(TimeZone.getAvailableIDs(), ", ");
	}

	private static TimeZone getTimeZoneFromIDS(String strTimezone) {
		Object obj = IDS.get(strTimezone);
		if (obj == null) return null;

		if (obj instanceof String) {
			TimeZone tz = TimeZone.getTimeZone((String) obj);
			IDS.put(strTimezone, tz);
			return tz;
		}
		return (TimeZone) obj;
	}

	/**
	 * translate timezone string format to a timezone
	 * 
	 * @param strTimezoneTrimmed
	 * @return
	 */
	public static TimeZone toTimeZone(String strTimezone, TimeZone defaultValue) {
		if (strTimezone == null) return defaultValue;

		String strTimezoneTrimmed = StringUtil.replace(strTimezone.trim().toLowerCase(), " ", "", false);
		TimeZone tz = getTimeZoneFromIDS(strTimezoneTrimmed);
		if (tz != null) return tz;

		// parse GMT followd by a number
		float gmtOffset = Float.NaN;
		if (strTimezoneTrimmed.startsWith("gmt")) gmtOffset = getGMTOffset(strTimezoneTrimmed.substring(3).trim(), Float.NaN);
		else if (strTimezoneTrimmed.startsWith("etc/gmt")) gmtOffset = getGMTOffset(strTimezoneTrimmed.substring(7).trim(), Float.NaN);
		else if (strTimezoneTrimmed.startsWith("utc")) gmtOffset = getGMTOffset(strTimezoneTrimmed.substring(3).trim(), Float.NaN);
		else if (strTimezoneTrimmed.startsWith("etc/utc")) gmtOffset = getGMTOffset(strTimezoneTrimmed.substring(7).trim(), Float.NaN);

		if (!Float.isNaN(gmtOffset)) {
			strTimezoneTrimmed = "etc/gmt" + (gmtOffset >= 0 ? "+" : "") + Caster.toString(gmtOffset);
			tz = getTimeZoneFromIDS(strTimezoneTrimmed);
			if (tz != null) return tz;

		}

		// display name in all variations
		if (!StringUtil.isEmpty(strTimezoneTrimmed)) {
			tz = dn.get(strTimezoneTrimmed);
			if (tz != null) return tz;
			Iterator<Object> it = IDS.values().iterator();
			Object o;
			while (it.hasNext()) {
				o = it.next();
				if (o instanceof TimeZone) {
					tz = (TimeZone) o;
					if (strTimezone.equalsIgnoreCase(tz.getDisplayName(true, TimeZone.SHORT, Locale.US))
							|| strTimezone.equalsIgnoreCase(tz.getDisplayName(false, TimeZone.SHORT, Locale.US))
							|| strTimezone.equalsIgnoreCase(tz.getDisplayName(true, TimeZone.LONG, Locale.US))
							|| strTimezone.equalsIgnoreCase(tz.getDisplayName(false, TimeZone.LONG, Locale.US))) {
						dn.put(strTimezoneTrimmed, tz);
						return tz;
					}
				}
			}
		}
		return defaultValue;
	}

	private static float getGMTOffset(String str, float defaultValue) {
		int index;
		String left = null, right = null;
		if ((index = str.indexOf(':')) != -1) {
			left = str.substring(0, index);
			right = str.substring(index + 1);
		}
		else if (str.startsWith("-")) {
			if (str.length() >= 4 && str.indexOf('.') == -1) {
				left = str.substring(0, str.length() - 2);
				right = str.substring(str.length() - 2);
			}
		}
		else if (str.length() >= 3 && str.indexOf('.') == -1) {
			left = str.substring(0, str.length() - 2);
			right = str.substring(str.length() - 2);
		}
		if (left != null) {
			int l = Caster.toIntValue(left, Integer.MIN_VALUE);
			int r = Caster.toIntValue(right, Integer.MIN_VALUE);
			if (l == Integer.MIN_VALUE || r == Integer.MIN_VALUE || r > 59) return defaultValue;
			return l + (r / 60f);
		}

		float f = Caster.toFloatValue(str, Float.NaN);
		if (Float.isNaN(f)) return defaultValue;
		return f;
	}

	public static TimeZone toTimeZone(String strTimezone) throws ExpressionException {
		TimeZone tz = toTimeZone(strTimezone, null);
		if (tz != null) return tz;
		throw new ExpressionException("Can't cast value [" + strTimezone + "] to a TimeZone", "supported TimeZones are: [" + getSupportedTimeZonesAsString() +"]");
	}
}
