/*
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.*
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
	
	public void function testRemoveAllSimple(){
		var fields = [1];
		assertEquals(1,arrayLen(fields));
		for( field in fields ) {
			arraydelete(fields, field);
		}
		assertEquals(0,arrayLen(fields));
	}

	/* TODO add this when impl is improved
	public void function testRemovePart(){
		var fields = [1,2,3,4,5,6,7,8];
		assertEquals(8,arrayLen(fields));
		for( field in fields ) {
			var even=field/2;
			even=int(even)==even;
			if(even)arraydelete(fields, field);
		}
		assertEquals(4,arrayLen(fields));
	}

	public void function testRemoveAll(){
		var fields = [1,2,3,4,5,6,7,8];
		assertEquals(8,arrayLen(fields));
		for( field in fields ) {
			arraydelete(fields, field);
		}
		assertEquals(0,arrayLen(fields));
	}

	public void function testIterate(){
		var fields = [1,2,3,4,5,6,7,8];
		assertEquals(8,arrayLen(fields));
		var list="";
		for( field in fields ) {
			listAppend(list,field);
		}
		assertEquals('1,2,3,4,5,6,7,8',list);
	}*/
} 



