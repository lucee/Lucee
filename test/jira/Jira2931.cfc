/**
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
 **/
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	

	public function testEmptyListFirst() {

		var list = "";

		assertEquals("", listFirst(list, ",;"));
	}

	public function testEmptyListRest() {

		var list = "";

		assertEquals("", listRest(list, ",;"));
	}
	

	public function testOriginal1() {

		var list = "A,B,C,D,E,F,G";

		assert(listFirst(list, ",;") == "A");
	}

	public function testOriginal2() {

		var list = "A,B,C,D,E,F,G";

		assert(listRest(list, ",;") == "B,C,D,E,F,G");
	}

	public function testOriginal3() {

		var list = ",,A,B,,C,D,E,F,G,";

		assert(listFirst(list, ",;", false) == "A");
	}

	public function testOriginal4() {

		var list = ",,A,B,,C,D,E,F,G,";

		assertEquals("B,,C,D,E,F,G,", listRest(list, ",;"));
	}
	
	public function testOriginal5() {

		var list = ",,A,B,,C,D,E,F,G,";

		assertEquals("", listFirst(list, ",;", true));
	}

	public function testOriginal6() {

		var list = ",,A,B,,C,D,E,F,G,";

		assertEquals(",A,B,,C,D,E,F,G,", listRest(list, ",;", true));
	}

	public function testOriginal7() {

		var list = ",,A,B,,C,D,E,F,G,";

		assertEquals("A", listFirst(list, ",;", false));
	}

	public function testOriginal8() {

		var list = ",,A,B,,C,D,E,F,G,";

		assertEquals("B,,C,D,E,F,G,", listRest(list, ",;", false));
	}
	
	public function testCount1() {

		var list = "A,B,C,D,E,F,G";

		assertEquals("A,B,C", listFirst(list, ",;", false, 3));
	}

	public function testCount2() {

		var list = "A,B,C,D,E,F,G";

		assertEquals("A,B,C", listFirst(list, ",;", true, 3));
	}

	public function testCount3() {

		var list = "A,B,C,D,E,F,G";

		assertEquals("D,E,F,G", listRest(list, ",;", false, 3));
	}

	public function testCount4() {

		var list = "A,B,C,D,E,F,G";

		assertEquals("D,E,F,G", listRest(list, ",;", true, 3));
	}

	public function testCount5() {

		var list = ",,A,B,,C,D,E,F,G,";

		assertEquals("D,E,F,G,", listRest(list, ",;", false, 3));
	}

	public function testCount6() {

		var list = ",,A,B,,C,D,E,F,G,";

		assertEquals("B,,C,D,E,F,G,", listRest(list, ",;", true, 3));
	}

	public function testCount7() {

		var list = "http://localhost:8888/lucee-tests/index.cfm";

		assertEquals("http://localhost:8888", listFirst(list, "/", true, 3));
	}

	public function testCount8() {

		var list = "http://localhost:8888/lucee-tests/index.cfm";

		assertEquals("lucee-tests/index.cfm", listRest(list, "/", true, 3));
	}

	public function testCount9() {

		var list = "A,B,C,D,E,F,G";

		assertEquals(list, listFirst(list, "/", true, 100));
	}

	public function testCount10() {

		var list = "A,B,C,D,E,F,G";

		assertEquals("", listRest(list, "/", true, 100));
	}


}