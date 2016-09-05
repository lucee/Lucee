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


} 