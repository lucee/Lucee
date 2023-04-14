<!--- 
 *
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.*
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
		assertEquals(3,yesNoFormat(true).len());
		assertEquals("Yes",yesNoFormat(true));
		assertEqUals("YesX",yesNoFormat(true)&"X");		
	}
	public void function testBooleanFalse(){
		assertEquals(2,yesNoFormat(false).len());
		assertEquals("No",yesNoFormat(false));
		assertEquals("NoX",yesNoFormat(false)&"X");		
	}

	public void function testEmtyString(){
		assertEquals(2,yesNoFormat("").len());
		assertEquals("No",yesNoFormat(""));
		assertEquals("NoX",yesNoFormat("")&"X");		
	}

	public void function testMemberFunc(){
		b=true;
		assertEquals(3,b.yesNoFormat().len());
		assertEquals("Yes",b.yesNoFormat());
		assertEqUals("YesX",b.yesNoFormat()&"X");		
	}

} 
</cfscript>