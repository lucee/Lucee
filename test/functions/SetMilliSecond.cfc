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
		t.setMilliSecond(10);
		assertEquals("00:00:00:10",TimeFormat(t,'HH:mm:ss:ll'));
		
		assertEquals("00:00:00:180",TimeFormat(t.setMilliSecond(180),'HH:mm:ss:ll'));
		assertEquals("00:00:00:180",TimeFormat(t,'HH:mm:ss:ll'));
		
		assertEquals("00:00:00:01",TimeFormat(t.setMilliSecond(1,getTimeZone()),'HH:mm:ss:ll'));
	}

	public void function testFunction() localmode="true" {
		t=createDateTime(2000,1,1,0,0,0);
		assertEquals("00:00:00:00",TimeFormat(t,'HH:mm:ss:ll'));
		setMilliSecond(t,1200);
		assertEquals("00:00:01:200",TimeFormat(t,'HH:mm:ss:ll'));
	}
} 