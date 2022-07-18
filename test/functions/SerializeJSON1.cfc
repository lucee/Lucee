<!---
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.*
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
component extends="org.lucee.cfml.test.LuceeTestCase" {

	//public function setUp() {}


	private function createQuery() {
		var q=queryNew("a,b");
		var row=queryAddRow(q);
		querySetCell(q,"a",1,row);
		querySetCell(q,"b",2,row);
		var row=queryAddRow(q);
		querySetCell(q,"a",10,row);
		querySetCell(q,"b",20,row);
		return q;
	}

	public void function testNoOption() {
		assertEquals(
			'{"COLUMNS":["A","B"],"DATA":[[1,2],[10,20]]}', 
			serializeJson(createQuery()));
	}
	public void function testNoOptionMember() {
		assertEquals(
			'{"COLUMNS":["A","B"],"DATA":[[1,2],[10,20]]}', 
			createQuery().toJson());
	}

	public void function testOptionRow() {
		assertEquals(
			'{"COLUMNS":["A","B"],"DATA":[[1,2],[10,20]]}', 
			serializeJson(createQuery(),"row"));
	}

	public void function testOptionRowMember() {
		assertEquals(
			'{"COLUMNS":["A","B"],"DATA":[[1,2],[10,20]]}', 
			createQuery().toJson("row"));
	}

	public void function testOptionColumn() {
		assertEquals(
			'{"ROWCOUNT":2,"COLUMNS":["A","B"],"DATA":{"A":[1,10],"B":[2,20]}}', 
			serializeJson(createQuery(),"column"));
	}

	public void function testOptionColumnMember() {
		assertEquals(
			'{"ROWCOUNT":2,"COLUMNS":["A","B"],"DATA":{"A":[1,10],"B":[2,20]}}', 
			createQuery().toJson("column"));
	}

	public void function testOptionStruct() {
		assertEquals(
			'[{"A":1,"B":2},{"A":10,"B":20}]', 
			serializeJson(createQuery(),"struct"));
	}

	public void function testOptionStructMember() {
		assertEquals(
			'[{"A":1,"B":2},{"A":10,"B":20}]', 
			createQuery().toJson("struct"));
	}

	public void function testStruct() {
		assertEquals('{"A":1}', serializeJson({A:1}));
	}

	public void function testStructMember() {
		assertEquals('{"A":1}', {A:1}.toJson());
	}

	public void function testArray() {
		assertEquals('[1]', serializeJson([1]));
	}

	public void function testArrayMember() {
		assertEquals('[1]', [1].toJson());
	}

}
</cfscript>