/*
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.*
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
component extends="org.lucee.cfml.test.LuceeTestCase"	{

	public void function testMemberFunction() localmode="true" {
		t = createDateTime(2000,1,1,0,0,0);
		assertEquals("{ts '2000-01-01 00:00:00'}",t&"");
		t.setYear(2016);
		assertEquals("{ts '2016-01-01 00:00:00'}",t&"");
		
		assertEquals("{ts '2017-01-01 00:00:00'}",t.setYear(2017)&"");
		assertEquals("{ts '2017-01-01 00:00:00'}",t&"");
		
		assertEquals("{ts '2059-01-01 00:00:00'}",t.setYear(2059,getTimeZone())&"");
	}

	public void function testFunction() localmode="true" {
		t = createDateTime(2000,1,1,0,0,0);
		assertEquals("{ts '2000-01-01 00:00:00'}",t&"");

		setYear(t,2019);
		assertEquals("{ts '2019-01-01 00:00:00'}",t&"");

		assertEquals("{ts '2059-01-01 00:00:00'}",setYear(t,2059,getTimeZone())&"");

		date = "{ts '2000-01-01 00:00:00'}";
		assertEquals("{ts '2001-01-01 00:00:00'}",setYear(date,2001)&"");
	}
	
} 