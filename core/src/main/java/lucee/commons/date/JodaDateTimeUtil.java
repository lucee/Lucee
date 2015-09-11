/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.commons.date;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;


public class JodaDateTimeUtil extends DateTimeUtil {
	
	public static Map zones=new HashMap();
	public static JREDateTimeUtil jreUtil=new JREDateTimeUtil();
	
	JodaDateTimeUtil() {	
	}
	
	@Override
	long _toTime(TimeZone tz, int year, int month, int day, int hour,int minute, int second, int milliSecond) {
		try{
			return new DateTime(year, month, day, hour, minute, second, milliSecond, getDateTimeZone(tz)).getMillis();
		}
		catch(Throwable t){
			t.printStackTrace();
			return jreUtil._toTime(tz, year, month, day, hour, minute, second, milliSecond);
		}
	}
	
	@Override
	public int getYear(TimeZone tz,lucee.runtime.type.dt.DateTime dt){
		return new DateTime(dt.getTime(),getDateTimeZone(tz)).getYear();	
	}
	
	@Override
	public int getWeekOfYear(Locale locale,TimeZone tz,lucee.runtime.type.dt.DateTime dt){
		return new DateTime(dt.getTime(),getDateTimeZone(tz)).getWeekOfWeekyear();	
	}

	@Override
	public int getMonth(TimeZone tz,lucee.runtime.type.dt.DateTime dt){
		return new DateTime(dt.getTime(),getDateTimeZone(tz)).getMonthOfYear();
	}
	
	@Override
	public int getDay(TimeZone tz,lucee.runtime.type.dt.DateTime dt){
		return new DateTime(dt.getTime(),getDateTimeZone(tz)).getDayOfMonth();
	}

	@Override
	public int getHour(TimeZone tz,lucee.runtime.type.dt.DateTime dt){
		return new DateTime(dt.getTime(),getDateTimeZone(tz)).getHourOfDay();
	}
	
	@Override
	public int getMinute(TimeZone tz,lucee.runtime.type.dt.DateTime dt){
		return new DateTime(dt.getTime(),getDateTimeZone(tz)).getMinuteOfHour();
	}
	
	@Override
	public int getSecond(TimeZone tz,lucee.runtime.type.dt.DateTime dt){
		return new DateTime(dt.getTime(),getDateTimeZone(tz)).getSecondOfMinute();
	}
	
	@Override
	public int getMilliSecond(TimeZone tz,lucee.runtime.type.dt.DateTime dt){
		return new DateTime(dt.getTime(),getDateTimeZone(tz)).getMillisOfSecond();
	}

	@Override
	public int getDaysInMonth(TimeZone tz, lucee.runtime.type.dt.DateTime date) {
		DateTime dt = new DateTime(date.getTime(),getDateTimeZone(tz));
		return daysInMonth(dt.getYear(), dt.getMonthOfYear());
	}

	@Override
	public int getDayOfYear(Locale locale,TimeZone tz, lucee.runtime.type.dt.DateTime dt) {
		return new DateTime(dt.getTime(),getDateTimeZone(tz)).getDayOfYear();
	}

	@Override
	public int getDayOfWeek(Locale locale,TimeZone tz, lucee.runtime.type.dt.DateTime dt) {
		int dow=new DateTime(dt.getTime(),getDateTimeZone(tz)).getDayOfWeek()+1;
		if(dow==8) return 1;
		return dow;
	}

	@Override
	public int getFirstDayOfMonth(TimeZone tz, lucee.runtime.type.dt.DateTime dt) {
		return jreUtil.getFirstDayOfMonth(tz, dt);
	}

	@Override
	public long getMilliSecondsInDay(TimeZone tz,long time) {
		return new DateTime(time,getDateTimeZone(tz)).getMillisOfDay();
	}

	private DateTimeZone getDateTimeZone(TimeZone tz) {
		DateTimeZone dtz=(DateTimeZone) zones.get(tz);
		if(dtz==null){
			dtz=DateTimeZone.forTimeZone(tz);
			zones.put(tz, dtz);
		}
		return dtz;
	}

	@Override
	public String toString(lucee.runtime.type.dt.DateTime date, TimeZone tz) {
		//return jreUtil.toString(date, tz);
		/*DateTime dt = new DateTime(date.getTime(),getDateTimeZone(tz));
		return "{ts '"+dt.getYear()+
    	"-"+dt.getMonthOfYear()+
    	"-"+dt.getDayOfMonth()+
    	" "+dt.getHourOfDay()+
    	":"+dt.getMinuteOfHour()+
    	":"+dt.getSecondOfMinute()+"'}";*/
		
		StringBuilder sb=new StringBuilder();
		DateTime dt = new DateTime(date.getTime(),getDateTimeZone(tz));
    	sb.append("{ts '");
    	JREDateTimeUtil.toString(sb,dt.getYear(),4);
    	sb.append("-");
    	JREDateTimeUtil.toString(sb,dt.getMonthOfYear(),2);
    	sb.append("-");
    	JREDateTimeUtil.toString(sb,dt.getDayOfMonth(),2);
    	sb.append(" ");
    	JREDateTimeUtil.toString(sb,dt.getHourOfDay(),2);
    	sb.append(":");
    	JREDateTimeUtil.toString(sb,dt.getMinuteOfHour(),2);
    	sb.append(":");
    	JREDateTimeUtil.toString(sb,dt.getSecondOfMinute(),2);
    	sb.append("'}");
    	 
    	return sb.toString();
	}
}