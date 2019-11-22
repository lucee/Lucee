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
package lucee.runtime.converter;

import java.lang.ref.SoftReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.lang.SerializableObject;
import lucee.runtime.engine.ThreadLocalPageContext;

public class JSONDateFormat {

	public static final String PATTERN_CF = "MMMM, dd yyyy HH:mm:ss Z";
	public static final String PATTERN_ISO8601 = "yyyy-MM-dd'T'HH:mm:ssZ"; // preferred pattern for json

	private static Map<String, SoftReference<DateFormat>> map = new ConcurrentHashMap<String, SoftReference<DateFormat>>();
	//private static DateFormat format=null;
	private static Locale locale=Locale.ENGLISH;
	private final static Object sync=new SerializableObject();
	
	public static String format(Date date, TimeZone tz, String pattern) {
		tz=ThreadLocalPageContext.getTimeZone(tz);
		String id=locale.hashCode()+"-"+tz.getID();
		synchronized (sync) {
			SoftReference<DateFormat> tmp = map.get(id);
			DateFormat format = tmp == null ? null : tmp.get();
			if(format==null){
				format = new SimpleDateFormat(pattern, locale);
				format.setTimeZone(tz);
				map.put(id, new SoftReference<DateFormat>(format));
			}
			return format.format(date);	
		}
	}
}