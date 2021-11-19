<!--- 
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.*
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


	public void function testArrayMap() localMode="true" {
		_arrayMap(false);
	}

	public void function testArrayMapParallel() localMode="true" {
		_arrayMap(true);
	}

	private void function _arrayMap(boolean parallel) localMode="true" {
		arr=['a','b','c'];
		arr[5]='e';
		
		// base test
		res=ArrayMap(arr, function( value ){
 							return value EQ 'b';
 
                        },parallel);
		assertEquals("false,true,false,,false",arrayToList(res));
		
		// output test
		savecontent variable="c" {
			res=ArrayMap([1], function( value ){
							echo(serialize(arguments));
 							return value EQ 'b';
 
                        },parallel);
		}
		assertEquals('{"value":1,"2":1,"3":[1]}',c);

		var list = "I,love,lucee,";
		var result = listMap(list,function(element,index){
			return element;
		});
		assertEquals('I,love,lucee', result);

		// member function test
		res=arr.map(function( value ){
 							return value EQ 'b';
 
                        },parallel);

		assertEquals("false,true,false,,false",arrayToList(res));

		var list2 = "I;love@lucee,";
		var result2 = list2.Map(function(element){
			return element;
		},'@');
		assertEquals('I;love@lucee,' , result2);
	}


	public void function testListMap() localMode="true" {
		_listMap(false);
	}

	public void function testListMapParallel() localMode="true" {
		_listMap(true);
	}

	private void function _listMap(boolean parallel) localMode="true" {
		list=",,a,,b,c,,";

		// base test
		res=ListMap(list, function( value ){
 							return value EQ 'b';
 
                        },',',false,true,parallel);
		assertEquals("false,true,false",res);
		
		// output test
		savecontent variable="c" {
			res=ListMap("a,b", function( value ){
							echo(serialize(arguments));
 							return value EQ 'b';
 
                        },',',false,true,parallel);
		}
		assertEquals('{"value":"a","2":1,"3":"a,b","4":","}{"value":"b","2":2,"3":"a,b","4":","}',c);

		// member function test
		res=list.map( function( value ){
 							return value EQ 'b';
 
                        },',',false,true,parallel);
		assertEquals("false,true,false",res);
	}


	public void function testStructMap() localMode="true" {
		_structMap(false);
	}

	public void function testStructMapParallel() localMode="true" {
		_structMap(true);
	}

	private void function _structMap(boolean parallel) localMode="true" {
		sct=structNew("linked");
		sct.a=1;
		sct.b=2;
		sct.c=3;

		// base test
		res=StructMap(sct, function(key, value ){
 							return key&":"&value;
 
                        },parallel);
		assertEquals('["A":"A:1","B":"B:2","C":"C:3"]',serialize(res));
		
		// test content produced
		savecontent variable="c" {
			res=StructMap({a:1}, function(key, value ){
							echo(serialize(arguments));
 							return key&":"&value;
 
                        },parallel);
		}
		assertEquals('{"key":"A","value":1,"3":{"A":1}}',c);

		// test member name
		res=sct.map(function(key, value ){
 							return key&":"&value;
 
                        },parallel);

		assertEquals('["A":"A:1","B":"B:2","C":"C:3"]',serialize(res));

	}

	public void function testQueryMap() localMode="true" {
		_queryMap(false);
	}

	public void function testQueryMapParallel() localMode="true" {
		_queryMap(true);
	}

	private void function _queryMap(boolean parallel) localMode="true" {
		qry=query(a:["a1","a2"],b:["b1","b2"]);
		
		// base test
		res=QueryMap(qry, function(row ){
							return {a:row.a&":",b:row.b&":"};
 
                        },queryNew(qry.columnlist),parallel);

		assertEquals('query("a":["a1:","a2:"],"b":["b1:","b2:"])',serialize(res));
		

		// test content produced
		savecontent variable="c" {
			res=QueryMap(query(a:["a1"]), function(row){
							echo(serialize(arguments));
 							return row;
 
                        },queryNew(qry.columnlist),parallel);
		}
		assertEquals('{"row":["a":"a1"],"2":1,"3":query("a":["a1"])}',c);

		// test member name
		res=qry.Map(function(row ){
							return {a:row.a&":",b:row.b&":"};
 
                        },queryNew(qry.columnlist),parallel);

		assertEquals('query("a":["a1:","a2:"],"b":["b1:","b2:"])',serialize(res));

	}




	public void function testMap() localMode="true" {
		_map(false);
	}

	public void function testMapParallel() localMode="true" {
		_map(true);
	}

	private void function _map(boolean parallel) localMode="true" {
		arr=["a"];
		it=arr.iterator();



		res=collectionMap(it, function(value ){
 							return value;
 
                        },parallel);

		assertEquals('["a"]',serialize(res));
		
		it=arr.iterator();

		savecontent variable="c" {
			res=collectionMap(it, function(value ){
							echo(serialize(arguments));
 							return value;
 
                        },parallel);
		}
		assertEquals('{"value":"a"}',c);
	}
} 
</cfscript>