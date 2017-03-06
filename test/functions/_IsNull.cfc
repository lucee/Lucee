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
	
	private function test() {
		return {a:1};
	}

	private function test2() {
		return {test:test};
	}

dump();
dump(isNull(test().b));
dump(test().b?:'other');



	function testDataMember() {
		var b=1;
		assertTrue(isNull(a.b.c.d.e));
		assertTrue(isNull(variables.a.b.c.d.e));
		
		assertFalse(isNull(b));
		assertTrue(isNull(b.c));

	}

	function testUDFMember() {
		assertFalse(isNull(test().a));
		assertTrue(isNull(test().a.b));
		assertTrue(isNull(test().b));
	}
/*
	function testUDFMember2() {
		assertFalse(isNull(variables.test().a));
		assertTrue(isNull(variables.test().a.b));
		assertTrue(isNull(variables.test().b));

	}
	function testUDFMember3() {
		assertFalse(isNull(test2().test().a));
		assertTrue(isNull(test2().test().a.b));
		assertTrue(isNull(test2().test().b));

	}*/


} 