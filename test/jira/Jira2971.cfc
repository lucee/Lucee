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
	
	public void function testArrayFilter() localMode="true" {
		_arrayFilter(false);
	}

	public void function testArrayFilterParallel() localMode="true" {
		_arrayFilter(true);
	}

	private void function _arrayFilter(boolean parallel) localMode="true" {
		arr=['a','b','c'];
		//arr[5]='e';
		res=ArrayFilter(arr, function( value ){
 							return value EQ 'b';
 
                        },parallel);

		assertEquals("b",arrayToList(res));
		savecontent variable="c" {
			res=ArrayFilter([1], function( value ){
							echo(serialize(arguments));
 							return true;
 
                        },parallel);
		}
		assertEquals('{"value":1,"2":1,"3":[1]}',c);
	}

	public void function testListFilter() localMode="true" {
		_listFilter(false);
	}

	public void function testListFilterParallel() localMode="true" {
		_listFilter(true);
	}

	private void function _listFilter(boolean parallel) localMode="true" {
		list=",,a,,b,c,,";

		res=ListFilter(list, function( value ){
 							return value EQ 'b';
 
                        },',',false,true,parallel);

		assertEquals("b",res);

		savecontent variable="c" {
			res=ListFilter("a,b", function(){
							echo(serialize(arguments));
 							return true;
 
                        },',',false,true,parallel);
		}
		assertEquals('{"1":"a","2":1,"3":"a,b","4":","}{"1":"b","2":2,"3":"a,b","4":","}',c);


		// member functions
		res=list.listFilter(function( value ){
 							return value EQ 'b';
 
                        },',',false,true,parallel);

		assertEquals("b",res);
	}

	public void function testQueryFilter() localMode="true" {
		_queryFilter(false);
	}

	public void function testQueryFilterParallel() localMode="true" {
		_queryFilter(true);
	}

	private void function _queryFilter(boolean parallel) localMode="true" {
		qry=query(a:["a1","a2"],b:["b1","b2"]);
		//arr[5]='e';
		res=QueryFilter(qry, function( row ){
 							return row.a EQ 'a2';
 
                        },parallel);

		assertEquals('query("a":["a2"],"b":["b2"])',serialize(res));
		
		qry=query(a:["a1"]);
		savecontent variable="c" {
			res=QueryFilter(qry, function(){
							echo(serialize(arguments));
 							return true;
 
                        },parallel);
		}
		assertEquals('{"1":{"a":"a1"},"2":1,"3":query("a":["a1"])}',c);
	}


	public void function testStructFilter() localMode="true" {
		_structFilter(false);
	}

	public void function testStructFilterParallel() localMode="true" {
		_structFilter(true);
	}

	private void function _structFilter(boolean parallel) localMode="true" {
		sct=structNew("linked");
		sct.a=1;
		sct.b=2;
		sct.c=3;

		res=StructFilter(sct, function(key, value ){
 							return key=='b';
 
                        },parallel);

		assertEquals('["B":2]',serialize(res));
		savecontent variable="c" {
			res=StructFilter({a:1}, function(key, value ){
							echo(serialize(arguments));
 							return key == 'a';
 
                        },parallel);
		}
		assertEquals('{"key":"A","value":1,"3":{"A":1}}',c);
	}




	public void function testFilter() localMode="true" {
		_filter(false);
	}

	public void function testFilterParallel() localMode="true" {
		_filter(true);
	}

	private void function _filter(boolean parallel) localMode="true" {
		arr=["a"];
		it=arr.iterator();



		res=collectionFilter(it, function(value ){
 							return value == 'a';
 
                        },parallel);

		assertEquals('["a"]',serialize(res));
		
		it=arr.iterator();

		savecontent variable="c" {
			res=collectionFilter(it, function(value ){
							echo(serialize(arguments));
 							return value == 'a';
 
                        },parallel);
		}
		assertEquals('{"value":"a"}',c);
	}
} 
</cfscript>