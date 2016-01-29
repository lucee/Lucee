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
	
	//public function beforeTests(){}
	
	//public function afterTests(){}
	
	//public function setUp(){}

	public void function testStringColumnNames(){
		var q=queryNew("a,b");
		var meta=getMetaData(q);
		
		assertEquals(2,arrayLen(meta));
		assertEquals("a",meta[1].name);
		assertEquals("b",meta[2].name);
		
	}
	public void function testArrayColumnNames(){
		var q=queryNew(["a","b,c"]);
		var meta=getMetaData(q);
		
		assertEquals(2,arrayLen(meta));
		assertEquals("a",meta[1].name);
		assertEquals("b,c",meta[2].name);
		
	}
	
	public void function testStringColumnNamesStringColumnTypes (){
		var q=queryNew("a,b","varchar,varchar");
		var meta=getMetaData(q);
		assertEquals(2,arrayLen(meta));
		assertEquals("a",meta[1].name);
		assertEquals("b",meta[2].name);
		
		assertEquals("varchar",meta[1].typeName);
		assertEquals("varchar",meta[2].typeName);
	}
	
	public void function testArrayColumnNamesStringColumnTypes (){
		var q=queryNew(["a","b,c"],"varchar,varchar");
		var meta=getMetaData(q);
		assertEquals(2,arrayLen(meta));
		assertEquals("a",meta[1].name);
		assertEquals("b,c",meta[2].name);
		
		assertEquals("varchar",meta[1].typeName);
		assertEquals("varchar",meta[2].typeName);
	}
	
	public void function testArrayColumnNamesArrayColumnTypes (){
		var q=queryNew(["a","b,c"],["varchar","varchar"]);
		var meta=getMetaData(q);
		assertEquals(2,arrayLen(meta));
		assertEquals("a",meta[1].name);
		assertEquals("b,c",meta[2].name);
		
		assertEquals("varchar",meta[1].typeName);
		assertEquals("varchar",meta[2].typeName);
	}
	
	public void function testStringColumnNamesStringColumnTypesData (){
		var q=queryNew("a,b","varchar,numeric",[["Susi",20],["Urs",24]]);
		var meta=getMetaData(q);
		
		assertEquals(2,arrayLen(meta));
		assertEquals("a",meta[1].name);
		assertEquals("b",meta[2].name);
		
		assertEquals("varchar",meta[1].typeName);
		assertEquals("numeric",meta[2].typeName);
		
		assertEquals(2,q.recordcount);
		assertEquals("Susi",q.a[1]);
		assertEquals("Urs",q.a[2]);
		assertEquals(20,q.b[1]);
		assertEquals(24,q.b[2]);
	}
} 
</cfscript>