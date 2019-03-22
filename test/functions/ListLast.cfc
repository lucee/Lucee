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

	function testListLastCount(){
		assertEquals("b,c,d",ListLast(list:',a,,b,,c,,d,',count:3));
		assertEquals(",d,",ListLast(list:',a,,b,,c,,d,',count:3,includeEmptyFields:true));
	}

	function testListLastMemberFunction(){
		var list = 'abba,xx,xxxx,xxxx,xxx,bb,'
		assertEquals("bb",list.ListLast(","));
		assertEquals("",list.ListLast(includeEmptyFields=true));
		var list2 = 'abba||xx||xxxx||xxx||bb||';
		assertEquals("bb",list2.ListLast(delimiters="||"));
		assertEquals("",list2.ListLast(delimiters="||", includeEmptyFields=true));
	}
}