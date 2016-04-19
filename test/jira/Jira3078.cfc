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

	//public function setUp(){}

	public void function test(){
		var a = [1,2,3];

		assertEquals(2,a.contains(2));
		assertEquals(2,a.containsNoCase(2));
		assertEquals(2,arrayContains(a, 2));
		assertEquals(2,arrayContainsNoCase(a, 2));
	}

	private void function testNull1(){
		var a = [1,2,nullValue()];

		assertEquals(2,a.contains(2));
		assertEquals(2,a.containsNoCase(2));
		assertEquals(2,arrayContains(a, 2));
		assertEquals(2,arrayContainsNoCase(a, 2));
	}

	private void function testNull2(){
		var a = [1,nullValue(),3];

		assertEquals(2,a.contains(2));
		assertEquals(2,a.containsNoCase(2));
		assertEquals(2,arrayContains(a, 2));
		assertEquals(2,arrayContainsNoCase(a, 2));
	}
} 
</cfscript>