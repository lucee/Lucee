<!--- 
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
 ---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase"	{

	public function beforeTests(){
		variables.ts=getTimeZone();
		setTimezone("UTC");
	}
	
	public function afterTests(){
		setTimezone(ts);
	}
	

	public void function testTWithoutOffset(){
		assertEquals(parseDateTime("{ts '1900-01-30 15:19:50'}"),dateAdd("m",1,"{t '15:19:50'}"));
	}

	public void function testTSWithoutOffset(){
		assertEquals(parseDateTime("{ts '1900-02-28 15:19:50'}"),dateAdd("m",1,"{ts '1900-01-30 15:19:50'}"));
	}

	public void function testTWithOffset(){
		assertEquals(parseDateTime("{ts '1900-01-30 14:19:50'}"),dateAdd("m",1,"{t '15:19:50+1:0'}"));
		assertEquals(parseDateTime("{ts '1900-01-30 14:19:50'}"),dateAdd("m",1,"{t '15:19:50+01:0'}"));
		assertEquals(parseDateTime("{ts '1900-01-30 14:19:50'}"),dateAdd("m",1,"{t '15:19:50+1:00'}"));
		assertEquals(parseDateTime("{ts '1900-01-30 14:19:50'}"),dateAdd("m",1,"{t '15:19:50+01:00'}"));
	}

	public void function testTSWithOffset(){
		assertEquals(parseDateTime("{ts '1900-02-28 14:19:50'}"),dateAdd("m",1,"{ts '1900-01-30 15:19:50+1:0'}"));
		assertEquals(parseDateTime("{ts '1900-02-28 14:19:50'}"),dateAdd("m",1,"{ts '1900-01-30 15:19:50+01:0'}"));
		assertEquals(parseDateTime("{ts '1900-02-28 14:19:50'}"),dateAdd("m",1,"{ts '1900-01-30 15:19:50+1:00'}"));
		assertEquals(parseDateTime("{ts '1900-02-28 14:19:50'}"),dateAdd("m",1,"{ts '1900-01-30 15:19:50+01:00'}"));
	}
} 
</cfscript>