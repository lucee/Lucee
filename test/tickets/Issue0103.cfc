<!--- 
 *
 * Copyright (c) 2015, Lucee Associaction Switzerland. All rights reserved.
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
 ---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	qry=query(a:[1,2,3,4],b:[5,6,7,8]);
	//public function beforeTests(){}
	
	//public function afterTests(){}
	
	//public function setUp(){}

	public void function testQueryGetRow(){
		var row=QueryGetRow(qry,3)
		assertEquals('a,b',row.keyList().listSort('text'));
		assertEquals('3',row.a);
		assertEquals('7',row.b);
	}
	public void function testGetRow(){
		var row=qry.getRow(3)
		assertEquals('a,b',row.keyList().listSort('text'));
		assertEquals('3',row.a);
		assertEquals('7',row.b);
	}
	public void function testQueryRowData(){
		var row=QueryRowData(qry,3)
		assertEquals('a,b',row.keyList().listSort('text'));
		assertEquals('3',row.a);
		assertEquals('7',row.b);
		var people = QueryNew( "name,dob,age", "varchar,date,int", [
			[ "Susi", CreateDate( 1970, 1, 1 ), 0 ],
			[ "Urs" , CreateDate( 1995, 1, 1 ), 0 ],
			[ "Fred", CreateDate( 1960, 1, 1 ), 0 ],
			[ "Jim" , CreateDate( 1988, 1, 1 ), 0 ]
		]);
		expect(queryRowData(people,2)).toBeTypeOf('struct');
		expect(queryRowData(people,2).name).toBe('Urs');
	}
	public void function testRowData(){
		var row=qry.RowData(3)
		assertEquals('a,b',row.keyList().listSort('text'));
		assertEquals('3',row.a);
		assertEquals('7',row.b);
	}
} 
</cfscript>