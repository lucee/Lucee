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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.collections4.map.ReferenceMap;

import lucee.runtime.engine.ThreadLocalPageContext;


public class JSONDateFormat {

	private static ReferenceMap<String,DateFormat> map=new ReferenceMap<String,DateFormat>();
	//private static DateFormat format=null;
	private static Locale locale=Locale.ENGLISH;
	
	public synchronized static  String format(Date date, TimeZone tz) {
		tz=ThreadLocalPageContext.getTimeZone(tz);
		String id=locale.hashCode()+"-"+tz.getID();
		DateFormat format = (DateFormat) map.get(id);
		if(format==null){
			format=new SimpleDateFormat("MMMM, dd yyyy HH:mm:ss Z",locale);
			format.setTimeZone(tz);
			map.put(id, format);
		}
		
		return format.format(date);
	}
}