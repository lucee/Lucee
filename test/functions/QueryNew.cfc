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
	
	public void function test3Args() localmode="true" {
		local.qry = QueryNew( "name,age,whatever", "varchar,date,int", [
	    [ "Susi", CreateDate( 1970, 1, 1 ), 5 ],
	    [ "Urs" , CreateDate( 1995, 1, 1 ), 7 ],
	    [ "Fred", CreateDate( 1960, 1, 1 ), 9 ],
	    [ "Jim" , CreateDate( 1988, 1, 1 ), 11 ]
		]);
		local.meta=getMetaData(qry);

		assertEquals(4,qry.recordcount);
		assertEquals('name,age,whatever',qry.columnlist);
		assertEquals('5,7,9,11',valueList(qry.whatever));

		assertEquals(false,meta[1].isCaseSensitive);
		assertEquals('name',meta[1].name);
		assertEquals('VARCHAR',meta[1].typeName);
		
		assertEquals(false,meta[2].isCaseSensitive);
		assertEquals('age',meta[2].name);
		assertEquals('DATE',meta[2].typeName);
		
		assertEquals(false,meta[3].isCaseSensitive);
		assertEquals('whatever',meta[3].name);
		assertEquals('INTEGER',meta[3].typeName);

	}

	public void function test2Args() localmode="true" {
		local.qry = QueryNew( "name,age,whatever", "varchar,date,int");
		local.meta=getMetaData(qry);

		assertEquals(0,qry.recordcount);
		assertEquals('name,age,whatever',qry.columnlist);

		assertEquals(false,meta[1].isCaseSensitive);
		assertEquals('name',meta[1].name);
		assertEquals('VARCHAR',meta[1].typeName);
		
		assertEquals(false,meta[2].isCaseSensitive);
		assertEquals('age',meta[2].name);
		assertEquals('DATE',meta[2].typeName);
		
		assertEquals(false,meta[3].isCaseSensitive);
		assertEquals('whatever',meta[3].name);
		assertEquals('INTEGER',meta[3].typeName);

	}

	public void function test1Args() localmode="true" {
		local.qry = QueryNew( "name,age,whatever");
		local.meta=getMetaData(qry);

		assertEquals(0,qry.recordcount);
		assertEquals('name,age,whatever',qry.columnlist);

		assertEquals(false,meta[1].isCaseSensitive);
		assertEquals('name',meta[1].name);
		assertEquals('OBJECT',meta[1].typeName);
		
		assertEquals(false,meta[2].isCaseSensitive);
		assertEquals('age',meta[2].name);
		assertEquals('OBJECT',meta[2].typeName);
		
		assertEquals(false,meta[3].isCaseSensitive);
		assertEquals('whatever',meta[3].name);
		assertEquals('OBJECT',meta[3].typeName);

	}
	
	public void function testPopulateArray() localmode="true" {
		var qry = queryNew("name,age","varchar,numeric",{name:["user1","user2"],age:[15,20]});
		assertEquals("user1",qry.name[1]);
	}

} 