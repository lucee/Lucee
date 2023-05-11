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
component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {
	
	//public function beforeTests(){}
	
	//public function afterTests(){}
	
	//public function setUp(){}

	public void function testQueryNoParams(){
		local.q=query(a:[1,2,3],b:['a','b','c']);
		

		local.q2=QueryExecute ("select b from q where a=3",[],{dbtype="query"});
		assertEquals("c",q2.b);
 		
 		// named arguments
		local.q2=QueryExecute (sql:"select b from q where a=1",options:{dbtype="query"});
		assertEquals("a",q2.b);
 	}

 	public void function testQueryArrayParams(){
		local.q=query(a:[1,2,3],b:['a','b','c']);
		

		local.q2=QueryExecute ("select b from q where a=?",[2],{dbtype="query"});
		assertEquals("b",q2.b);
 		
 		// named arguments
		local.q3=QueryExecute (sql:"select b from q where a=?",params:[1],options:{dbtype="query"});
		assertEquals("a",q3.b);
	} 		

 	public void function testQueryStructParams(){
 		local.q=query(a:[1,2,3],b:['a','b','c']);
		
		local.q1=QueryExecute (sql:"select b from q where a=:susi",params:{susi=3},options:{dbtype="query"});
		assertEquals("c",q1.b);

		local.q1=QueryExecute (sql:"select b from q where a=:susi",params:{susi={value=2,CFSQLType='CF_SQL_NUMERIC'}},options:{dbtype="query"});
		assertEquals("b",q1.b);

		local.q1=QueryExecute (sql:"select b from q where a=:susi and b=:susi",params:{susi={value=2,CFSQLType='CF_SQL_NUMERIC'}},options:{dbtype="query"});
		assertEquals(0,q1.recordcount);
	}
		
} 
</cfscript>