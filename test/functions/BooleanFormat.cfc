<!--- 
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.*
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

	public void function testBooleanTrue(){
		assertEquals(4,booleanFormat(true).len());
		assertEquals("true",booleanFormat(true));
		assertEqUals("trueX",booleanFormat(true)&"X");		
	}
	public void function testBooleanFalse(){
		assertEquals(5,booleanFormat(false).len());
		assertEquals("false",booleanFormat(false));
		assertEquals("falseX",booleanFormat(false)&"X");		
	}

	public void function testEmtyString(){
		assertEquals(5,booleanFormat("").len());
		assertEquals("false",booleanFormat(""));
		assertEquals("falseX",booleanFormat("")&"X");		
	}

	public void function testMemberFunc(){
		var b=true;
		assertEquals(4,b.booleanFormat().len());
		assertEquals("true",b.booleanFormat());
		assertEqUals("trueX",b.booleanFormat()&"X");		
	}

} 
</cfscript>