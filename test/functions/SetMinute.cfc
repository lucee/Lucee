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
		t.setMinute(20);
		assertEquals("{ts '2000-01-01 00:20:00'}",t&"");
		
		assertEquals("{ts '2000-01-01 00:10:00'}",t.setMinute(10)&"");
		assertEquals("{ts '2000-01-01 00:10:00'}",t&"");
		
		assertEquals("{ts '2000-01-01 00:01:00'}",t.setMinute(1,getTimeZone())&"");
	}

	public void function testFunction() localmode="true" {
		t = createDateTime(2000,1,1,0,0,0);
		assertEquals("{ts '2000-01-01 00:00:00'}",t&"");
		setMinute(t,100);
		assertEquals("{ts '2000-01-01 01:40:00'}",t&"");

		date = "{ts '2000-05-04 00:00:00'}";
		assertEquals("{ts '2000-05-04 00:30:00'}",setMinute(date,30,getTimeZone())&"");
		assertEquals("{ts '2000-05-04 01:00:00'}",setMinute(date,60)&"");
	}

} 