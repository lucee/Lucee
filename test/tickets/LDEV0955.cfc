<!--- 
 *
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
 * 
 ---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase"	{
		
	variables.character=createObject('java','java.lang.Character').init('1');
	variables.charArray="1".toCharArray();

	public void function testIsCharNumeric() {
		assertTrue(isNumeric(variables.character));
		assertTrue(isNumeric(variables.charArray[1]));
	}

	public void function testConvert2StringIsNumeric() {
		assertTrue(isNumeric(variables.character&""));
		assertTrue(isNumeric(variables.charArray[1]&""));
	}

	public void function testConvert2Number() {
		assertTrue(isNumeric(variables.character+1));
		assertTrue(isNumeric(variables.charArray[1]+1));
	}
} 
</cfscript>