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
		assertEquals("/localhost:9999/lucee-tests/index.cfm",listRest("http://localhost:9999/lucee-tests/index.cfm", "/", true));
		assertEquals("lucee-tests/index9.cfm",listRest("http://localhost:8888/lucee-tests/index9.cfm", "/", true, 3));
		assertEquals("index1.cfm",listRest("http://localhost:8888/lucee-tests/index1.cfm", "/", false, 3));

		assertEquals("bb/cc",listRest("aa//bb/cc", "/", false, 1));
		assertEquals("bb/cc",listRest("aa////bb/cc", "/", false, 1));
		assertEquals("/bb/cc",listRest("aa//bb/cc", "/", true, 1));

		assertEquals("///bb/cc",listRest("aa////bb/cc", "/", true, 1));

		assertEquals("dd",listRest("aa//bb/cc/dd", "/", false, 3));
		assertEquals("dd",listRest("aa////bb/cc/dd", "/", false, 3));
		assertEquals("dd",listRest("aa//bb////cc/dd", "/", false, 3));
		assertEquals("dd",listRest("aa////bb/////cc/////dd", "/", false, 3));

		assertEquals("cc/dd",listRest("aa//bb/cc/dd", "/", true, 3));
		assertEquals("bb/cc/dd",listRest("aa///bb/cc/dd", "/", true, 3));


		assertEquals("b/c",listRest("//a/b/c", "/"));
		assertEquals("c",listRest("//a/b/c", "/",false,2));
		assertEquals("d",listRest("//a/b/c/d", "/",false,3));

		assertEquals("/a/b/c",listRest("//a/b/c", "/",true));
		assertEquals("a/b/c",listRest("//a/b/c", "/",true,2));
		assertEquals("b/c/d",listRest("//a/b/c/d", "/",true,3));
		

	}

	function testListRestmember(){
		assertEquals("",''.ListRest());
		assertEquals("b/c","//a/b/c".listRest("/"));
		assertEquals("/a/b/c","//a/b/c".listRest("/", true));
		assertEquals("/a/b/c","//a/b/c".listRest("/", 3));
		assertEquals("b/c","//a/b/c".listRest("/", true, 3));
		assertEquals("localhost:8888/lucee-tests/index.cfm","http://localhost:8888/lucee-tests/index.cfm".listRest("/"));
		assertEquals("/localhost:9999/lucee-tests/index.cfm","http://localhost:9999/lucee-tests/index.cfm".listRest("/", true));
		assertEquals("lucee-tests/index9.cfm","http://localhost:8888/lucee-tests/index9.cfm".listRest("/", true, 3));
		assertEquals("index1.cfm","http://localhost:8888/lucee-tests/index1.cfm".listRest("/", false, 3));

		assertEquals("bb/cc","aa//bb/cc".listRest("/", false, 1));
		assertEquals("bb/cc","aa////bb/cc".listRest("/", false, 1));
		assertEquals("/bb/cc","aa//bb/cc".listRest("/", true, 1));

		assertEquals("///bb/cc","aa////bb/cc".listRest("/", true, 1));

		assertEquals("dd","aa//bb/cc/dd".listRest("/", false, 3));
		assertEquals("dd","aa////bb/cc/dd".listRest("/", false, 3));
		assertEquals("dd","aa//bb////cc/dd".listRest("/", false, 3));
		assertEquals("dd","aa////bb/////cc/////dd".listRest("/", false, 3));

		assertEquals("cc/dd","aa//bb/cc/dd".listRest("/", true, 3));
		assertEquals("bb/cc/dd","aa///bb/cc/dd".listRest("/", true, 3));


		assertEquals("b/c","//a/b/c".listRest("/"));
		assertEquals("c","//a/b/c".listRest("/",false,2));
		assertEquals("d","//a/b/c/d".listRest("/",false,3));


		assertEquals("/a/b/c","//a/b/c".listRest("/",true));
		assertEquals("a/b/c","//a/b/c".listRest("/",true,2));
		assertEquals("b/c/d","//a/b/c/d".listRest("/",true,3));
	}
}