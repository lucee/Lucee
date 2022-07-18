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
	
	public void function testListEvery() localMode="true" {
		_listEvery(false);
	}


	public void function testListEveryParallel() localMode="true" {
		_listEvery(true);
	}

	private void function _listEvery(boolean parallel) localMode="true" {
		
		list=",,a,,b,c,,";
		//arr[5]='e';
		
		// base test
		res=ListEvery(list, function(value ){return true;},',',false,parallel);
		assertEquals(true,res);
		
		res=ListEvery(list, function(value ){return value =='b';},',',false,parallel);
		assertEquals(false,res);
		

		// closure output
		savecontent variable="c" {
			res=ListEvery(",a,,b,", function(){
							echo(serialize(arguments));
 							return true;
 
                        },',',false,parallel);
		}
		assertEquals('{"1":"a","2":1,"3":",a,,b,","4":","}{"1":"b","2":2,"3":",a,,b,","4":","}',c);

		savecontent variable="c" {
			res=ListEvery(",a,,b,", function(value){
							echo(">"&value);
 							return true;
 
                        },',',true,true,parallel);
		}
		assertEquals(">>a>>b>",c);

		savecontent variable="c" {
			res=ListEvery(",a,,b,", function(value){
							echo(">"&value);
 							return true;
 
                        },',',false,true,parallel);
		}
		assertEquals(">a>b",c);


		// member function
		res=ListEvery(List,function(value ){return true;},',',false,true,parallel);
		assertEquals(true,res);


		res=List.listEvery(closure:function(value ){return true;},delimiter:',',includeEmptyFields:false,multiCharacterDelimiter:true,parallel:parallel);
		assertEquals(true,res);
	}
	

	public void function testArrayEvery() localMode="true" {
		_arrayEvery(false);
	}


	public void function testArrayEveryParallel() localMode="true" {
		_arrayEvery(true);
	}

	private void function _arrayEvery(boolean parallel) localMode="true" {
		
		arr=['a','b','c'];
		//arr[5]='e';
		
		// base test
		res=ArrayEvery(arr, function(value ){return true;},parallel);
		assertEquals(true,res);
		
		res=ArrayEvery(arr, function(value ){return value =='b';},parallel);
		assertEquals(false,res);
		

		// closure output
		savecontent variable="c" {
			res=ArrayEvery(['a'], function(){
							echo(serialize(arguments));
 							return true;
 
                        },parallel);
		}
		assertEquals('{"1":"a","2":1,"3":["a"]}',c);

		// member function
		res=arr.every(function(value ){return true;},parallel);
		assertEquals(true,res);
	}


	public void function testStructEvery() localMode="true" {
		_structEvery(false);
	}


	public void function testStructEveryParallel() localMode="true" {
		_structEvery(true);
	}

	private void function _structEvery(boolean parallel) localMode="true" {
		
		sct={a:1,b:2,c:3};
		//arr[5]='e';
		
		// base test
		res=StructEvery(sct, function(key,value ){return true;},parallel);
		assertEquals(true,res);
		
		res=StructEvery(sct, function(key,value ){return key =='b';},parallel);
		assertEquals(false,res);
		

		// closure output
		savecontent variable="c" {
			res=StructEvery({a:1}, function(){
							echo(serialize(arguments));
 							return true;
 
                        },parallel);
		}
		assertEquals('{"1":"A","2":1,"3":{"A":1}}',c);

		// member function
		res=sct.every(function(key,value ){return true;},parallel);
		assertEquals(true,res);
		
	}


	public void function testQueryEvery() localMode="true" {
		_queryEvery(false);
	}


	public void function testQueryEveryParallel() localMode="true" {
		_queryEvery(true);
	}

	private void function _queryEvery(boolean parallel) localMode="true" {
		qry=query(a:["a1","a2"],b:["b1","b2"]);
		
		// base test
		res=QueryEvery(qry, function(){return true;},parallel);
		assertEquals(true,res);
		
		res=QueryEvery(qry, function(struct row, number rowNumber,query qry){return rowNumber == 2;},parallel);
		assertEquals(false,res);
		

		// closure output
		savecontent variable="c" {
			res=QueryEvery(qry, function(){
							echo(serialize(arguments));
 							return true;
 
                        },parallel);
		}
		assertEquals('{"1":["a":"a1","b":"b1"],"2":1,"3":query("a":["a1","a2"],"b":["b1","b2"])}{"1":["a":"a2","b":"b2"],"2":2,"3":query("a":["a1","a2"],"b":["b1","b2"])}',c);

		var people = QueryNew( "name,dob,age", "varchar,date,int", [
			[ "Susi", CreateDate( 1970, 1, 1 ), 0 ],
			[ "Urs" , CreateDate( 1995, 1, 1 ), 0 ],
			[ "Fred", CreateDate( 1960, 1, 1 ), 0 ],
			[ "Jim" , CreateDate( 1988, 1, 1 ), 0 ]
		]);
		var result = queryEvery(people,function(row, rowNumber, qryData){
		    return ((DateDiff('yyyy', row.dob, Now()) > 0) && (DateDiff('yyyy', row.dob, Now()) <= 100))
		});
		assertEquals(true, result);

		// member function
		res=qry.every(function(key,value ){return true;},parallel);
		assertEquals(true,res);

		var result2 = people.every(function(row, rowNumber, qryData){
		    return ((DateDiff('yyyy', row.dob, Now()) > 0) && (DateDiff('yyyy', row.dob, Now()) <= 50))
		});
		assertEquals(false, result2);
		
	}


	public void function testEvery() localMode="true" {
		arr=["a","b"];
		it=arr.iterator();

		res=collectionEvery(it, function(value ){return value =='b' || value== 'a';});
		assertEquals(true,res);
		
		it=arr.iterator();
		res=collectionEvery(it, function(value ){return value =='b';});
		assertEquals(false,res);
		
		it=arr.iterator();
		savecontent variable="c" {
			res=collectionEvery(it, function(){
							echo(serialize(arguments));
 							return true;
 
                        });
		}
		assertEquals('{"1":"a"}{"1":"b"}',c);
	}
} 
</cfscript>