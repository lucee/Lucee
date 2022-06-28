/**
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.
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
	function testNumber() {
		assertTrue(isJson('{a:1}'));
	}
	function testBoolean() {
		assertTrue(isJson('{a:true}'));
	}
	function testNull() {
		assertTrue(isJson('{a:null}'));
	}

	function testString() {
		assertTrue(isJson('{a:"Susi"}'));
	}
	
	function testVariableGet1() {
		assertFalse(isJson('{a:susi}'));
	}
	function testVariableGet2() {
		assertTrue(isJson('{a:"##susi##"}'));
	}
	function testVariableGet3() {
		assertTrue(isJson('{a:"##susi##jjj"}'));
	}
	function testVariableGet4() {
		assertFalse(isJson('{a:[a,b,c]}'));
	}

	function testVariableSet() {
		assertFalse(isJson('{a:susi=1}'));
	}
	
	function testValueString() {
		assertTrue(isJson('"This is a \"string\"."'));
		assertEquals(deserializeJson('"This is a \"string\"."'), "This is a ""string"".");
	}

	function testValueNumber() {
		assertTrue(isJson('1234567890'));
		assertEquals(deserializeJson('1234567890'), 1234567890);
	}

	function testValueDecimal() {
		assertTrue(isJson('-123.45'));
		assertEquals(deserializeJson('-123.45'), -123.45);
	}

	function testValueScientificNotation() {
		assertTrue(isJson('10e1'));
		assertEquals(deserializeJson('10e1'), 100);
	}

	function testValueTrue() {
		assertTrue(isJson('true'));
		assertEquals(deserializeJson('true'), true);
	}

	function testValueFalse() {
		assertTrue(isJson('false'));
		assertEquals(deserializeJson('false'), false);
	}

	function testValueNull() {
		assertTrue(isJson('null'));
		assertTrue(isNull(deserializeJson('null')));
	}

	function testValueEmoji() {
		assertTrue(isJson('"#chr(128515)#"'));  //unicode smiley
		assertEquals(deserializeJson('"#chr(128515)#"'), chr(128515));
	}

	function testArray() {
		assertTrue(isJson('[1, "2", 10e1]'));
		assertEquals(deserializeJson('[1, "2", 10e1]'), [1, "2", 100]);
	}

	function testEmptyArray() {
		assertTrue(isJson('[]'));
		assertEquals(deserializeJson('[]'), []);
	}

	function testEmptyObject() {
		assertTrue(isJson('{}'));
		assertEquals(deserializeJson('{}'), {});
	}

} 
