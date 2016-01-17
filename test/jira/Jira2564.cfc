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

	public function setUp(){
		variables.qry=queryNew("str,nbr,dat","varchar,integer,date");
		queryAddrow(variables.qry);
	}

	public void function test(){
		setlocale("English (US)");

		var a = "1,5";
		assertEquals(false,IsNumeric(a)); // false
		assertEquals(true,LSIsNumeric(a)); // true ??
		assertEquals(15,LSParseNumber(a)); // 15 ??!
		try{
			assertEquals(ParseNumber(a)); // error
			fail("ParseNumber(a) should fail");
		}
		catch(local.exp){}
		
	}
} 

</cfscript>