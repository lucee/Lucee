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
	function testListAvg(){
		var arr="1,2,3";


	assertEquals("V1,V2,V3", ListCompact(",V1,V2,V3,"));
	assertEquals("V1,V2,V3", ListCompact(",V1,V2,V3,",","));
	assertEquals("V1,V2,V3", ListCompact(list=",V1,V2,V3,",delimiter=","));
	assertEquals("V1,V2,V3", ListCompact(delimiter=",",list=",V1,V2,V3,"));
	assertEquals(ListCompact(",V1,V2,V3,",","), ListCompact(list=",V1,V2,V3,",delimiter=","));

	assertEquals(",V1,V2,V3,", ListCompact(",V1,V2,V3,","}"));
	assertEquals(",V1,V2,V3,", ListCompact(list=",V1,V2,V3,",delimiter="}"));
	assertEquals(",V1,V2,V3,", ListCompact(delimiter="}",list=",V1,V2,V3,"));
	assertEquals(ListCompact(",V1,V2,V3,","}"), ListCompact(list=",V1,V2,V3,",delimiter="}"));

	assertEquals(0, Len(ListCompact("","}")) ) ;
	assertEquals(0, Len(ListCompact(list="",delimiter="}")) ) ;
	assertEquals(0, Len(ListCompact(delimiter="}",list="")) ) ;
	assertEquals(Len(ListCompact("","}")) , Len(ListCompact(list="",delimiter="}")) ) ;

	assertEquals("V1,V2,V3", ListCompact(";V1,V2,V3,",",;"));
	assertEquals("V1,V2,V3", ListCompact(";V1,V2,V3,",",;",false));
	assertEquals(";V1,V2,V3,", ListCompact(";V1,V2,V3,",",;",true));
	assertEquals("V1,V2,V3", ListCompact(",;V1,V2,V3,;",",;",true));
	
	assertEquals("V1,V2,V3", ListTrim(",V1,V2,V3,"));
	assertEquals("I,love,Lucee", listTrim(",I,love,lucee,"));
	assertEquals(",I$love$lucee", listTrim(",I$love$lucee", "$"));
	assertEquals("I$love$lucee", listTrim("$I$love$lucee", "$"));
	assertEquals("I$love$lucee", listTrim("$,I$love$lucee", "$,"));
	

	}
}