<!--- 
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
 ---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	public function setUp(){
		//Create a new query recordset to test
		variables.q = queryNew("id,test");
		//Test that queryAddRow works
		queryAddRow(q,[1,"test1"]);
	}

	public void function testBIF() localmode="modern" {
		
		//Fails for 'Can't cast Complex Object Type Struct to String'
		queryaddRow(q,
		{id:2,test:"test2"}
		);
		//Fails for 'Can't cast Complex Object Type Array to String'
		queryaddRow(q,[3,"test3"]);

		assertEquals(2,q.id[2]);
		assertEquals("test2",q.test[2]);
		assertEquals(3,q.id[3]);
		assertEquals("test3",q.test[3]);
	}

	public void function testMemberFunction() localmode="modern" {
		
		//Fails for 'Can't cast Complex Object Type Struct to String'
		q.addRow(
		{id:2,test:"test2"}
		);
		//Fails for 'Can't cast Complex Object Type Array to String'
		q.addRow([3,"test3"]);

		assertEquals(2,q.id[2]);
		assertEquals("test2",q.test[2]);
		assertEquals(3,q.id[3]);
		assertEquals("test3",q.test[3]);


		/*assertEquals("","");*/
	}
} 
</cfscript>