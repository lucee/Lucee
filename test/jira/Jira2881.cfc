<!--- 
 *
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
 * 
 ---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	//public function beforeTests(){}
	
	//public function afterTests(){}
	
	//public function setUp(){}

	public void function testParseDateTime() {
		var tz=getTimeZone();
		setTimeZone("UTC");
		assertEquals("{ts '1997-01-01 00:00:00'}",parseDateTime("1997")&"");
		assertEquals("{ts '1997-07-01 00:00:00'}",parseDateTime("1997-07")&"");
		assertEquals("{ts '1997-07-16 00:00:00'}",parseDateTime("1997-07-16")&"");
		assertEquals("{ts '1997-07-16 18:20:00'}",parseDateTime("1997-07-16T19:20+01:00")&"");
		assertEquals("{ts '1997-07-16 18:20:30'}",parseDateTime("1997-07-16T19:20:30+01:00")&"");
		assertEquals("{ts '1997-07-16 18:20:30'}",parseDateTime("1997-07-16T19:20:30.45+01:00")&"");
		setTimeZone(tz);
	}
} 
</cfscript>