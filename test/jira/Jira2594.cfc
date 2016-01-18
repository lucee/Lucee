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
		qry=query(a:[10,20,30]);
	}

	public void function testValueAccess(){
		
		assertEquals(10,qry.a[1]);
		assertEquals(20,qry.a[2]);
		assertEquals(30,qry.a[3]);
	}
	public void function testCurrentRow(){
		assertEquals(false,isNull(qry.a));
	}

	public void function testDefinedRow(){
		assertEquals(false,isNull(qry.a[1]));
		assertEquals(false,isNull(qry.a[2]));
		assertEquals(false,isNull(qry.a[3]));
	}
} 
</cfscript>