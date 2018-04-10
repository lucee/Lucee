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


		assertEquals("",ListLast(''));
		assertEquals("bb",ListLast('abba,xx,xxxx,xxxx,xxx,bb'));
		assertEquals("bb",ListLast('abba.bb','.'));
		assertEquals("bb",ListLast('...abba.bb..','.'));
		assertEquals("",ListLast('...abba.bb..','.',true));
		assertEquals("bb",ListLast(',,abba,bb,,'));
		assertEquals("",ListLast(',,,,,'));
		assertEquals("c",ListLast('a;b,c',';,:'));
		assertEquals("c",ListLast('a,b,c,,,',',',false));
		assertEquals("",ListLast('a,b,c,,,',',',true));

	}
}