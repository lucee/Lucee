<!--- 
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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

	public void function testQuery(){
		local.q=query(a:[1,2,3]);
		query dbtype="query" name="local.qoq"  {
			echo('select * from q where a=4');
		}
		assertTrue(isQuery(qoq));
	}

	public void function testStruct(){
		local.q=query(a:[1,2,3]);
		query dbtype="query" name="local.qoq" returntype="struct" columnkey='vorname' {
			echo('select * from q where a=4');
		}
		assertTrue(isStruct(qoq));
	}

	public void function testArray(){
		local.q=query(a:[1,2,3]);
		query dbtype="query" name="local.qoq" returntype="array" columnKey="a" {
			echo('select * from q where a=4');
		}
		assertTrue(isArray(qoq));
	}
} 
</cfscript>