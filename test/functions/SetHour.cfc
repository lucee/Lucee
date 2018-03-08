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
		t=createDateTime(2000,1,1,0,0,0);
		assertEquals("{ts '2000-01-01 00:00:00'}",t&"");
		t.setHour(1);
		assertEquals("{ts '2000-01-01 01:00:00'}",t&"");
		
		assertEquals("{ts '2000-01-01 02:00:00'}",t.setHour(2)&"");
		assertEquals("{ts '2000-01-01 02:00:00'}",t&"");
		
		assertEquals("{ts '2000-01-01 01:00:00'}",t.setHour(1,getTimeZone())&"");
	}

	/* support for this is removed public void function testFunction() localmode="true" {
		t=createDateTime(2000,1,1,0,0,0);
		assertEquals("{ts '2000-01-01 00:00:00'}",t&"");
		setHour(t,25);
		assertEquals("{ts '2000-01-02 01:00:00'}",t&"");
	}*/
	
	public void function testMethod() localmode="true" {
		setTimeZone("UTC");
		t=createDateTime(2000,1,1,0,0,0,0,"UTC");
		assertEquals("{ts '2000-01-01 00:00:00'}",t&"");
		t.setHours(1);
		assertEquals("{ts '2000-01-01 01:00:00'}",t&"");
	}

} 