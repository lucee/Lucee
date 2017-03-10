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
import java.util.Map;
import java.util.TimeZone;

import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ListUtil;

public class TimeZoneUtil {

	private static final Map<String,TimeZone> IDS=new HashMap<String,TimeZone>();
	
	static {
		String[] ids=TimeZone.getAvailableIDs();
		for(int i=0;i<ids.length;i++){
			IDS.put(ids[i].toLowerCase(), TimeZone.getTimeZone(ids[i]));
		}
		TimeZone def = TimeZone.getDefault();
		if(def!=null)IDS.put(def.getID(),def);
		IDS.put("jvm", TimeZone.getDefault());
		IDS.put("default", TimeZone.getDefault());
		IDS.put("", TimeZone.getDefault());

		// MS specific Timezone definions
		set("Dateline Standard Time",TimeZoneConstants.ETC_GMT_PLUS_12); // (GMT-12:00) International Date Line West
		set("Samoa Standard Time",TimeZoneConstants.PACIFIC_MIDWAY); // 	(GMT-11:00) Midway Island, Samoa
		set("Hawaiian Standard Time",TimeZoneConstants.HST); // (GMT-10:00) Hawaii
		set("Alaskan Standard Time",TimeZoneConstants.AST); // (GMT-09:00) Alaska
		set("Pacific Standard Time",TimeZoneConstants.PST); // (GMT-08:00) Pacific Time (US and Canada); Tijuana
		set("Mountain Standard Time",TimeZoneConstants.MST); // (GMT-07:00) Mountain Time (US and Canada)
		set("Mexico Standard Time",TimeZoneConstants.MEXICO_GENERAL); // (GMT-06:00) Guadalajara, Mexico City, Monterrey
		set("Mexico Standard Time 2",TimeZoneConstants.AMERICA_CHIHUAHUA); // (GMT-07:00) Chihuahua, La Paz, Mazatlan
		set("U.S. Mountain Standard Time",TimeZoneConstants.MST); // (GMT-07:00) Arizona
		set("Central Standard Time",TimeZoneConstants.CST); // (GMT-06:00) Central Time (US and Canada
		set("Canada Central Standard Time",TimeZoneConstants.CANADA_CENTRAL); // (GMT-06:00) Saskatchewan
		set("Central America Standard Time",TimeZoneConstants.CST); // (GMT-06:00) Central America
		set("Eastern Standard Time",TimeZoneConstants.EST); // (GMT-05:00) Eastern Time (US and Canada)
		set("U.S. Eastern Standard Time",TimeZoneConstants.EST); // (GMT-05:00) Indiana (East)
		set("S.A. Pacific Standard Time",TimeZoneConstants.AMERICA_BOGOTA); // (GMT-05:00) Bogota, Lima, Quito
		set("Atlantic Standard Time",TimeZoneConstants.CANADA_ATLANTIC); // (GMT-04:00) Atlantic Time (Canada)
		set("S.A. Western Standard Time",TimeZoneConstants.AMERICA_ANTIGUA); // (GMT-04:00) Caracas, La Paz
		set("Pacific S.A. Standard Time",TimeZoneConstants.AMERICA_SANTIAGO); // (GMT-04:00) Santiago
		set("Newfoundland and Labrador Standard Time",TimeZoneConstants.CNT); // (GMT-03:30) Newfoundland and Labrador
		set("E. South America Standard Time",TimeZoneConstants.BET); // (GMT-03:00) Brasilia
		set("S.A. Eastern Standard Time",TimeZoneConstants.AMERICA_ARGENTINA_BUENOS_AIRES); // (GMT-03:00) Buenos Aires, Georgetown
		set("Greenland Standard Time",TimeZoneConstants.AMERICA_GODTHAB); // (GMT-03:00) Greenland
		set("Mid-Atlantic Standard Time",TimeZoneConstants.AMERICA_NORONHA); // (GMT-02:00) Mid-Atlantic
		set("Azores Standard Time",TimeZoneConstants.ATLANTIC_AZORES); // (GMT-01:00) Azores
		set("Cape Verde Standard Time",TimeZoneConstants.ATLANTIC_CAPE_VERDE); // (GMT-01:00) Cape Verde Islands
		set("Central Europe Standard Time",TimeZoneConstants.CET); // (GMT+01:00) Belgrade, Bratislava, Budapest, Ljubljana, Prague
		set("Central European Standard Time",TimeZoneConstants.CET); // (GMT+01:00) Sarajevo, Skopje, Warsaw, Zagreb
		set("Romance Standard Time",TimeZoneConstants.EUROPE_BRUSSELS); // (GMT+01:00) Brussels, Copenhagen, Madrid, Paris
		set("W. Europe Standard Time",TimeZoneConstants.CET); // (GMT+01:00) Amsterdam, Berlin, Bern, Rome, Stockholm, Vienna
		set("W. Central Africa Standard Time",null); // (GMT+01:00) West Central Africa
		set("E. Europe Standard Time",TimeZoneConstants.ART); // (GMT+02:00) Bucharest
		set("Egypt Standard Time",TimeZoneConstants.EGYPT); // (GMT+02:00) Cairo
		set("FLE Standard Time",TimeZoneConstants.EET); // (GMT+02:00) Helsinki, Kiev, Riga, Sofia, Tallinn, Vilnius
		set("GTB Standard Time",TimeZoneConstants.EUROPE_ATHENS); // (GMT+02:00) Athens, Istanbul, Minsk
		set("Israel Standard Time",TimeZoneConstants.ASIA_JERUSALEM); // (GMT+02:00) Jerusalem
		set("South Africa Standard Time",TimeZoneConstants.AFRICA_JOHANNESBURG); // (GMT+02:00) Harare, Pretoria
		set("Russian Standard Time",TimeZoneConstants.EUROPE_MOSCOW); // (GMT+03:00) Moscow, St. Petersburg, Volgograd
		set("Arab Standard Time",TimeZoneConstants.ASIA_KUWAIT); // (GMT+03:00) Kuwait, Riyadh
		set("E. Africa Standard Time",TimeZoneConstants.AFRICA_NAIROBI); // (GMT+03:00) Nairobi
		set("Arabic Standard Time",TimeZoneConstants.ASIA_BAGHDAD); // (GMT+03:00) Baghdad
		set("Iran Standard Time",TimeZoneConstants.ASIA_TEHRAN); // (GMT+03:30) Tehran
		set("Arabian Standard Time",TimeZoneConstants.ASIA_MUSCAT); // (GMT+04:00) Abu Dhabi, Muscat
		set("Caucasus Standard Time",TimeZoneConstants.ASIA_YEREVAN); // (GMT+04:00) Baku, Tbilisi, Yerevan
		set("Transitional Islamic State of Afghanistan Standard Time",TimeZoneConstants.ASIA_KABUL); // (GMT+04:30) Kabul
		set("Ekaterinburg Standard Time",TimeZoneConstants.ASIA_YEKATERINBURG); // (GMT+05:00) Ekaterinburg
		set("West Asia Standard Time",TimeZoneConstants.ASIA_KARACHI); // (GMT+05:00) Islamabad, Karachi, Tashkent
		set("India Standard Time",TimeZoneConstants.IST); // (GMT+05:30) Chennai, Kolkata, Mumbai, New Delhi
		set("Nepal Standard Time",TimeZoneConstants.ASIA_KATMANDU); // (GMT+05:45) Kathmandu
		set("Central Asia Standard Time",TimeZoneConstants.ASIA_DHAKA); //(GMT+06:00) Astana, Dhaka 
		set("Sri Lanka Standard Time",TimeZoneConstants.ASIA_COLOMBO); // (GMT+06:00) Sri Jayawardenepura
		set("N. Central Asia Standard Time",TimeZoneConstants.ASIA_ALMATY); // (GMT+06:00) Almaty, Novosibirsk
		set("Myanmar Standard Time",TimeZoneConstants.ASIA_RANGOON); // (GMT+06:30) Yangon Rangoon
		set("S.E. Asia Standard Time",TimeZoneConstants.ASIA_BANGKOK); // (GMT+07:00) Bangkok, Hanoi, Jakarta
		set("North Asia Standard Time",TimeZoneConstants.ASIA_KRASNOYARSK); // (GMT+07:00) Krasnoyarsk
		set("China Standard Time",TimeZoneConstants.CTT); // (GMT+08:00) Beijing, Chongqing, Hong Kong SAR, Urumqi
		set("Singapore Standard Time",TimeZoneConstants.ASIA_SINGAPORE); // (GMT+08:00) Kuala Lumpur, Singapore
		set("Taipei Standard Time",TimeZoneConstants.ASIA_TAIPEI); // (GMT+08:00) Taipei
		set("W. Australia Standard Time",TimeZoneConstants.AUSTRALIA_PERTH); // (GMT+08:00) Perth
		set("North Asia East Standard Time",TimeZoneConstants.ASIA_IRKUTSK); // (GMT+08:00) Irkutsk, Ulaanbaatar
		set("Korea Standard Time",TimeZoneConstants.ASIA_SEOUL); // (GMT+09:00) Seoul
		set("Tokyo Standard Time",TimeZoneConstants.ASIA_TOKYO); // (GMT+09:00) Osaka, Sapporo, Tokyo
		set("Yakutsk Standard Time",TimeZoneConstants.ASIA_YAKUTSK); // (GMT+09:00) Yakutsk
		set("A.U.S. Central Standard Time",TimeZoneConstants.ACT); // (GMT+09:30) Darwin
		set("Cen. Australia Standard Time",TimeZoneConstants.ACT); // (GMT+09:30) Adelaide
		set("A.U.S. Eastern Standard Time",TimeZoneConstants.AET); // (GMT+10:00) Canberra, Melbourne, Sydney
		set("E. Australia Standard Time",TimeZoneConstants.AET); // (GMT+10:00) Brisbane
		set("Tasmania Standard Time",TimeZoneConstants.AUSTRALIA_TASMANIA); // (GMT+10:00) Hobart
		set("Vladivostok Standard Time",TimeZoneConstants.ASIA_VLADIVOSTOK); // (GMT+10:00) Vladivostok
		set("West Pacific Standard Time",TimeZoneConstants.PACIFIC_GUAM); // (GMT+10:00) Guam, Port Moresby
		set("Central Pacific Standard Time",TimeZoneConstants.ASIA_MAGADAN); // (GMT+11:00) Magadan, Solomon Islands, New Caledonia
		set("Fiji Islands Standard Time",TimeZoneConstants.PACIFIC_FIJI); // (GMT+12:00) Fiji Islands, Kamchatka, Marshall Islands
		set("New Zealand Standard Time",TimeZoneConstants.NZ); // (GMT+12:00) Auckland, Wellington
		set("Tonga Standard Time",TimeZoneConstants.PACIFIC_TONGATAPU); // (GMT+13:00) Nuku'alofa
		set("CEST",TimeZoneConstants.CET);
		set("ACDT",TimeZoneConstants.ACT);
		set("ACST",TimeZoneConstants.AUSTRALIA_EUCLA);
		set("ACST",TimeZoneConstants.AUSTRALIA_TASMANIA);
		set("AEST",TimeZoneConstants.AUSTRALIA_QUEENSLAND);
		set("ET",TimeZoneConstants.ET);
		set("EDT",TimeZoneConstants.ET);
		set("EST",TimeZoneConstants.ET);
		set("MT",TimeZoneConstants.MT);
		set("MST",TimeZoneConstants.MT);
		set("MDT",TimeZoneConstants.MT);
		set("CT",TimeZoneConstants.CT);
		set("CST",TimeZoneConstants.CT);
		set("CDT",TimeZoneConstants.CT);
		set("PT",TimeZoneConstants.PT);
		set("PST",TimeZoneConstants.PT);
		set("PDT",TimeZoneConstants.PT);
		
	}
	
	private static void set(String name, TimeZone tz) {
		if(tz==null) return;
		name=StringUtil.replace(name.trim().toLowerCase(), " ", "", false);
		IDS.put(name.toLowerCase(), tz);
	}

	/**
	 * return the string format of the Timezone
	 * @param timezone
	 * @return
	 */
	public static String toString(TimeZone timezone){
		return timezone.getID();
	}

	private static String getSupportedTimeZonesAsString() {
		return ListUtil.arrayToList(TimeZone.getAvailableIDs(),", ");
	}
	
	/**
	 * translate timezone string format to a timezone
	 * @param strTimezone
	 * @return
	 */
	public static TimeZone toTimeZone(String strTimezone,TimeZone defaultValue){
		if(strTimezone==null) return defaultValue;
		strTimezone=StringUtil.replace(strTimezone.trim().toLowerCase(), " ", "", false);
		TimeZone tz = IDS.get(strTimezone);
		if(tz!=null) return tz;
		
		//parse GMT followd by a number
		float gmtOffset=Float.NaN;
		if(strTimezone.startsWith("gmt")) gmtOffset=getGMTOffset(strTimezone.substring(3).trim(),Float.NaN);
		else if(strTimezone.startsWith("etc/gmt")) gmtOffset=getGMTOffset(strTimezone.substring(7).trim(),Float.NaN);
		else if(strTimezone.startsWith("utc")) gmtOffset=getGMTOffset(strTimezone.substring(3).trim(),Float.NaN);
		else if(strTimezone.startsWith("etc/utc")) gmtOffset=getGMTOffset(strTimezone.substring(7).trim(),Float.NaN);
		
		if(!Float.isNaN(gmtOffset)) {
			strTimezone="etc/gmt"+(gmtOffset>=0?"+":"")+Caster.toString(gmtOffset);
			tz =  IDS.get(strTimezone);
			if(tz!=null) return tz;
			
		}
		return defaultValue;
	}
	
	
	private static float getGMTOffset(String str, float defaultValue) {
		int index;
		String left=null,right=null;
		if((index=str.indexOf(':'))!=-1) {
			left = str.substring(0,index);
			right=str.substring(index+1);
		}
		else if(str.startsWith("-")) {
			if(str.length()>=4 && str.indexOf('.')==-1){
				left = str.substring(0,str.length()-2);
				right=str.substring(str.length()-2);
			}
		}
		else if(str.length()>=3 && str.indexOf('.')==-1) {
			left = str.substring(0,str.length()-2);
			right=str.substring(str.length()-2);
		}
		if(left!=null) {
			int l = Caster.toIntValue(left,Integer.MIN_VALUE);
			int r = Caster.toIntValue(right,Integer.MIN_VALUE);
			if(l==Integer.MIN_VALUE || r==Integer.MIN_VALUE || r>59) return defaultValue;
			return l+(r/60f);
		}
		
		
		float f=Caster.toFloatValue(str,Float.NaN);
		if(Float.isNaN(f)) return defaultValue;
		return f;
	}

	public static TimeZone toTimeZone(String strTimezone) throws ExpressionException{
		TimeZone tz = toTimeZone(strTimezone, null);
		if(tz!=null) return tz;
		throw new ExpressionException("can't cast value ("+strTimezone+") to a TimeZone","supported TimeZones are:"+getSupportedTimeZonesAsString());
	}
}