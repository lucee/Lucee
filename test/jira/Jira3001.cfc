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

	public void function testhasPrefix(){
		
		assertTrue( "abcde".hasPrefix("ab") );
		assertTrue( "abcde".hasPrefix("") );
		assertTrue( "abcde".hasPrefix("AbC", true) );
		
		assertFalse( "abcde".hasPrefix("AbCdeFghi", true) );
		assertFalse( "abcde".hasPrefix("abcdefghi") );
		assertFalse( "abcde".hasPrefix("x") );
		assertFalse( "abcde".hasPrefix("AbC") );

		assertTrue( StringhasPrefix("abcdef", "abc") );
		assertTrue( StringhasPrefix("Abcdef", "abc", true) );
		assertFalse( StringhasPrefix("Abcdef", "cde") );

	}

	public void function testhasSuffix(){
		
		assertTrue( "abcde".hasSuffix("") );
		assertTrue( "abcde".hasSuffix("de") );
		assertTrue( "abcde".hasSuffix("de", false) );
		assertTrue( "abcde".hasSuffix("De", true) );
		
		assertFalse( "abcde".hasSuffix("ab") );
		assertFalse( "abcde".hasSuffix("abcdefghi") );
		assertFalse( "abcde".hasSuffix("x") );
		assertFalse( "abcde".hasSuffix("AbC") );
		assertFalse( "abcde".hasSuffix("AbC", true) );
		assertFalse( "abcde".hasSuffix("AbCdeFghi", true) );
		assertFalse( "abcde".hasSuffix("De") );
		assertFalse( "abcde".hasSuffix("De", false) );

		assertTrue( StringhasSuffix("abcdef", "def") );
		assertTrue( StringhasSuffix("abcdef", "Def", true) );
	}
} 
</cfscript>