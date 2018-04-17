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
	function testListLast(){

		assertEquals("",ListFirst(''));
		assertEquals("abba",ListFirst('abba,xx,xxxx,xxxx,xxx,bb'));
		assertEquals("abba",ListFirst('abba.bb','.'));
		assertEquals("abba",ListFirst('...abba.bb..','.'));
		assertEquals("",ListFirst('...abba.bb..','.',true));
		assertEquals("abba",ListFirst(',,abba,bb,,'));
		assertEquals("",ListFirst(',,,,,'));
		assertEquals("a",ListFirst('a;b,c',';,:'));
		assertEquals("a",ListFirst(',,,a,b,c',',',false));
		assertEquals("",ListFirst(',,,a,b,c',',',true));

	}
}