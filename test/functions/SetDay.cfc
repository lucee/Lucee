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
		t.setDay(2);
		assertEquals("{ts '2000-01-02 03:04:05'}",t&"");
		
		assertEquals("{ts '2000-01-03 03:04:05'}",t.setDay(3)&"");
		assertEquals("{ts '2000-01-03 03:04:05'}",t&"");
		
		assertEquals("{ts '2000-01-04 03:04:05'}",t.setDay(4,getTimeZone())&"");
	}
	public void function testFunction() localmode="true" {
		t = createDateTime( year=2000, month=1, day=2, hour=3, minute=4, second=5, millisecond=6);
		assertEquals("{ts '2000-01-02 03:04:05'}",t&"");
		setDay(t,32);
		assertEquals("{ts '2000-02-01 03:04:05'}",t&"");
		date = "{ts '2000-01-08 00:00:00'}";
		assertEquals("{ts '2000-01-11 00:00:00'}",setDay(date,11,getTimeZone())&"");
	}

} 