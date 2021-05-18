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
	
	public void function testArraySome() localMode="true" {
		_arraySome (false);
	}

	public void function testArraySomeParallel() localMode="true" {
		_arraySome (true);
	}

	private void function _arraySome (boolean parallel) localMode="true" {
		
		arr=['a','b','c'];
		//arr[5]='e';
		
		// base test
		res=ArraySome (arr, function(value ){return value =='b';},parallel);
		assertEquals(true,res);
		
		res=ArraySome (arr, function(value ){return value =='d';},parallel);
		assertEquals(false,res);
		

		// closure output
		savecontent variable="c" {
			res=ArraySome (['a'], function(){
							echo(serialize(arguments));
 							return false;
 
                        },parallel);
		}
		assertEquals('{"1":"a","2":1,"3":["a"]}',c);

		// member function
		res=arr.some(function(value ){return value =='b';},parallel);
		assertEquals(true,res);
	}

	public void function testListSome() localMode="true" {
		_arrayList(false);
	}

	public void function testListSomeParallel() localMode="true" {
		_arrayList(true);
	}

	private void function _arrayList(boolean parallel) localMode="true" {
		list=",,a,,b,c,,";
		
		// base test
		res=ListSome(list, function(value ){return value =='b';},',',false,true,parallel);
		assertEquals(true,res);
		
		res=ListSome(list, function(value ){return value =='d';},',',false,true,parallel);
		assertEquals(false,res);
		
		// closure output
		savecontent variable="c" {
			res=ListSome("a,c", function(){
							echo(serialize(arguments));
 							return false;

                        },',',false,parallel);
		}
		assertEquals('{"1":"a","2":1,"3":"a,c","4":","}{"1":"c","2":2,"3":"a,c","4":","}',c);

		var list2 = "I@love@lucee@";
		var result = listSome(list2,function(item){
			return item=="lucee";
		},'@');
		assertEquals(true, result);


		// member function
		res=list.some(function(value ){return value =='b';},',',false,true,parallel);
		assertEquals(true,res);

		var list3 = "I,love,lucee,";
		var result2 = list3.some(function(item){
			return item=="testcase";
		});
		assertEquals(false, result2);
	}


	public void function testStructSome() localMode="true" {
		_structSome(false);
	}


	public void function testStructSomeParallel() localMode="true" {
		_structSome(true);
	}

	private void function _structSome(boolean parallel) localMode="true" {
		
		sct={a:1,b:2,c:3};
		//arr[5]='e';
		
		// base test
		res=StructSome(sct, function(key,value ){return key =='b';},parallel);
		assertEquals(true,res);
		
		res=StructSome(sct, function(key,value ){return key =='d';},parallel);
		assertEquals(false,res);
		

		// closure output
		savecontent variable="c" {
			res=StructSome({a:1}, function(){
							echo(serialize(arguments));
 							return false;
 
                        },parallel);
		}
		assertEquals('{"1":"A","2":1,"3":{"A":1}}',c);

		// member function
		res=sct.some(function(key,value ){return key =='b';},parallel);
		assertEquals(true,res);
		
	}



	public void function testQuerySome() localMode="true" {
		_querySome(false);
	}


	public void function testQuerySomeParallel() localMode="true" {
		_querySome(true);
	}

	private void function _querySome(boolean parallel) localMode="true" {
		qry=query(a:["a1","a2"],b:["b1","b2"]);
		
		// base test
		res=QuerySome(qry, function(row,rowNumber){return rowNumber == 1;},parallel);
		assertEquals(true,res);
		
		res=QuerySome(qry, function(row,rowNumber){return rowNumber == 4;},parallel);
		assertEquals(false,res);
		

		// closure output
		savecontent variable="c" {
			res=QuerySome(qry, function(){
							echo(serialize(arguments));
 							return false;
 
                        },parallel);
		}
		assertEquals('{"1":["a":"a1","b":"b1"],"2":1,"3":query("a":["a1","a2"],"b":["b1","b2"])}{"1":["a":"a2","b":"b2"],"2":2,"3":query("a":["a1","a2"],"b":["b1","b2"])}',c);

		var people = QueryNew( "name,dob,age", "varchar,date,int", [
			[ "Susi", CreateDate( 1970, 1, 1 ), 0 ],
			[ "Urs" , CreateDate( 1995, 1, 1 ), 0 ],
			[ "Fred", CreateDate( 1960, 1, 1 ), 0 ],
			[ "Jim" , CreateDate( 1988, 1, 1 ), 0 ]
		]);
		var result = querySome(people,function(row, rowNumber, qryData){
		    return ((DateDiff('yyyy', row.dob, Now()) > 0) && (DateDiff('yyyy', row.dob, Now()) <= 100))
		});
		assertEquals(result, true);

		// member function
		res=qry.some(function(row,rowNumber){return rowNumber == 1;},parallel);
		assertEquals(true,res);

		var result2 = people.Some(function(row, rowNumber, qryData){
		    return ((DateDiff('yyyy', row.dob, Now()) > 0) && (DateDiff('yyyy', row.dob, Now()) <= 50))
		});
		assertEquals(true, result2);
		
	}


	public void function testSome() localMode="true" {
		arr=["a","b"];
		it=arr.iterator();

		res=collectionSome(it, function(value ){return value =='b';});
		assertEquals(true,res);
		
		it=arr.iterator();
		res=collectionSome(it, function(value ){return value =='c';});
		assertEquals(false,res);
		
		it=arr.iterator();
		savecontent variable="c" {
			res=collectionSome(it, function(){
							echo(serialize(arguments));
 							return false;
 
                        });
		}
		assertEquals('{"1":"a"}{"1":"b"}',c);
	}
} 
</cfscript>