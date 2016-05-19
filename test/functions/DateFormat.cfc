/*
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.*
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
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
 */
 component extends="org.lucee.cfml.test.LuceeTestCase" {


 	public function testDateFormatMember() localMode="modern" {
 		dt=CreateDateTime(2004,1,2,4,5,6);
		assertEquals("2004",dt.dateFormat("yyyy"));
		
 	}
 	public function testDateFormat() localMode="modern" {

		assertEquals("09-04-1900",dateFormat(100,'dd-mm-yyyy'));
		assertEquals("30-12-1899",dateFormat(0,'dd-mm-yyyy'));

		org=getLocale();
		setLocale("German (Swiss)")

		dt=CreateDateTime(2004,1,2,4,5,6);
		assertEquals("2004",dateFormat(dt,"yyyy"));
		assertEquals("04",dateFormat(dt,"yy"));
		assertEquals("4",dateFormat(dt,"y"));
		assertEquals("January",dateFormat(dt,"MMMM"));

		assertEquals("Jan",dateFormat(dt,"mmm"));
		assertEquals("01x",dateFormat(dt,"mm")&"x");
		assertEquals("1x",dateFormat(dt,"m")&"x");
		assertEquals("02-Jan-04",dateFormat(dt));
		assertEquals("Friday",dateFormat(dt,"dddd"));
		assertEquals("Fri",dateFormat(dt,"ddd"));
		assertEquals("02x",dateFormat(dt,"dd")&"x");
		assertEquals("2x",dateFormat(dt,"d")&"x");
		assertEquals("02.01.2004x",dateFormat(dt,"dd.mm.yyyy")&"x");

		assertEquals("1/2/04x",dateFormat(dt,"short")&"x");
		assertEquals("Jan 2, 2004x",dateFormat(dt,"medium")&"x");
		assertEquals("January 2, 2004x",dateFormat(dt,"long")&"x");
		assertEquals("Friday, January 2, 2004x",dateFormat(dt,"full")&"x");
		setLocale("French (Swiss)");
		assertEquals("Friday, January 2, 2004x",dateFormat(dt,"full")&"x");
		setLocale(org);


		assertEquals("x",dateFormat(' ','dd.mm.yyyy')&"x");
		x='susi';
		try {
			assertEquals("x",dateFormat(x,'dd.mm.yyyy')&"x");
		    fail("must throw:The value of the parameter 1, which is currently ""susi"", must be a class java.util.Date value. "); 
		}
		catch(e){}

		assertEquals("18.05.1903x",dateFormat('1234','dd.mm.yyyy')&"x");
		assertEquals("30-Dec-99",DateFormat(0));
		assertEquals("01.10.2005" ,DateFormat('2005/10/01 00:00:00', 'dd.mm.yyyy'));
		assertEquals("01.10.2005" ,DateFormat(ParseDateTime('2005/10/01 00:00:00'), 'dd.mm.yyyy'));
		assertEquals("01.10.2005" ,DateFormat(ParseDateTime('2005-10-01 00:00:00'), 'dd.mm.yyyy'));
		assertEquals("",DateFormat('', 'dd.mm.yyyy'));

		date2=ParseDateTime("{ts '2008-09-01 01:34:55'}");
		date=ParseDateTime("{ts '2008-09-01 01:34:55.123'}");

		assertEquals("080901",DateFormat(date, "yymmdd"));
		assertEquals("08",DateFormat(date, "yy"));
		assertEquals("09",DateFormat(date, "mm"));
		assertEquals("01",DateFormat(date, "dd"));

		assertEquals("080901013455123",DateFormat(date, "yymmdd") & Timeformat(date, "HHmmsslll"));
	}
}