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
component extends="org.lucee.cfml.test.LuceeTestCase" {
	function testListRest(){
		assertEquals("",ListRest(''));
		assertEquals("b/c",listRest("//a/b/c", "/"));
		assertEquals("/a/b/c",listRest("//a/b/c", "/", true));
		assertEquals("/a/b/c",listRest("//a/b/c", "/", 3));
		assertEquals("b/c",listRest("//a/b/c", "/", true, 3));
		assertEquals("localhost:8888/lucee-tests/index.cfm",listRest("http://localhost:8888/lucee-tests/index.cfm", "/"));
		assertEquals("/localhost:8888/lucee-tests/index.cfm",listRest("http://localhost:8888/lucee-tests/index.cfm", "/", true));
		assertEquals("lucee-tests/index.cfm",listRest("http://localhost:8888/lucee-tests/index.cfm", "/", true, 3));
		assertEquals("index.cfm",listRest("http://localhost:8888/lucee-tests/index.cfm", "/", false, 3));
	}
}