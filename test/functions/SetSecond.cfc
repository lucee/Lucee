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
		t = createDateTime( year=2000, month=1, day=2, hour=3, minute=4, second=5, millisecond=6);
		assertEquals("{ts '2000-01-02 03:04:05'}",t&"");
		t.setSecond(100);
		assertEquals("{ts '2000-01-02 03:05:40'}",t&"");
		
		assertEquals("{ts '2000-01-02 03:05:20'}",t.setSecond(20)&"");
		assertEquals("{ts '2000-01-02 03:05:20'}",t&"");
		
		assertEquals("{ts '2000-01-02 03:05:01'}",t.setSecond(1,getTimeZone())&"");
	}

	public void function testFunction() localmode="true" {
		t = createDateTime( year=2000, month=1, day=2, hour=3, minute=4, second=5, millisecond=6);
		assertEquals("{ts '2000-01-02 03:04:05'}",t&"");
		setSecond(t,100);
		assertEquals("{ts '2000-01-02 03:05:40'}",t&"");

		date = "{ts '2000-05-04 00:00:00'}";
		assertEquals("{ts '2000-05-04 00:00:50'}",setSecond(date,50,getTimeZone())&"");
		assertEquals("{ts '2000-05-04 00:01:05'}",setSecond(date,65)&"");
	}
	
} 