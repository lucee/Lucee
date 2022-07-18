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
	
	public void function testListEach() localMode="true" {
		_listEach(false);
	}


	public void function testListEachParallel() localMode="true" {
		_listEach(true);
	}

	private void function _listEach(boolean parallel) localMode="true" {
		
		list=",,a,,b,c,,";
		//arr[5]='e';
		

		// closure output
		savecontent variable="c" {
			res=listEach(",a,,b,", function(){
							echo(serialize(arguments));
 							return true;
 
                        },',',false,true,parallel);
		}
		assertEquals('{"1":"a","2":1,"3":",a,,b,","4":","}{"1":"b","2":2,"3":",a,,b,","4":","}',c);

		savecontent variable="c" {
			res=listEach(",a,,b,", function(value){
							echo(">"&value);
 							return true;
 
                        },',',true,true,parallel);
		}
		assertEquals(">>a>>b>",c);

		savecontent variable="c" {
			res=listEach(",a,,b,", function(value){
							echo(">"&value);
 							return true;
 
                        },',',false,true,parallel);
		}
		assertEquals(">a>b",c);



		// member function
		savecontent variable="c" {
			res="a".listEach(function(){
							echo(serialize(arguments));
 							return true;
 
                        },',',false,true,parallel);
		}
		assertEquals('{"1":"a","2":1,"3":"a","4":","}',c);
	}
	

	public void function testArrayEach() localMode="true" {
		_arrayEach(false);
	}


	public void function testArrayEachParallel() localMode="true" {
		_arrayEach(true);
	}

	private void function _arrayEach(boolean parallel) localMode="true" {
		
		arr=['a','b','c'];
		//arr[5]='e';
		

		// closure output
		savecontent variable="c" {
			res=ArrayEach(['a'], function(){
							echo(serialize(arguments));
 							return true;
 
                        },parallel);
		}
		assertEquals('{"1":"a","2":1,"3":["a"]}',c);

		// member function
		arr=['a'];
		savecontent variable="c" {
			res=arr.each(function(){
							echo(serialize(arguments));
 							return true;
 
                        },parallel);
		}
		assertEquals('{"1":"a","2":1,"3":["a"]}',c);


	}

	public void function testQueryEach() localMode="true" {
		_queryEach(false);
	}


	public void function testQueryEachParallel() localMode="true" {
		_queryEach(true);
	}

	private void function _queryEach(boolean parallel) localMode="true" {
		qry=query(a:["a1","a2"],b:["b1","b2"]);
		//arr[5]='e';
		

		// closure output
		savecontent variable="c" {
			QueryEach(qry, function(){
							echo(serialize(arguments));
 							return true;
 
                        },parallel);
		}
		assertEquals('{"1":["a":"a1","b":"b1"],"2":1,"3":query("a":["a1","a2"],"b":["b1","b2"])}{"1":["a":"a2","b":"b2"],"2":2,"3":query("a":["a1","a2"],"b":["b1","b2"])}',c);

		// member function
		savecontent variable="c" {
			qry.each(function(){
							echo(serialize(arguments));
 							return true;
 
                        },parallel);
		}
		assertEquals('{"1":["a":"a1","b":"b1"],"2":1,"3":query("a":["a1","a2"],"b":["b1","b2"])}{"1":["a":"a2","b":"b2"],"2":2,"3":query("a":["a1","a2"],"b":["b1","b2"])}',c);


	}


	public void function testStructEach() localMode="true" {
		_structEach(false);
	}


	public void function testStructEachParallel() localMode="true" {
		_structEach(true);
	}

	private void function _structEach(boolean parallel) localMode="true" {
		
		sct={a:1};
		//arr[5]='e';
		

		// closure output
		savecontent variable="c" {
			res=structEach(sct, function(){
							echo(serialize(arguments));
 							return true;
 
                        },parallel);
		}
		assertEquals('{"1":"A","2":1,"3":{"A":1}}',c);

		// member function
		savecontent variable="c" {
			res=sct.each(function(){
							echo(serialize(arguments));
 							return true;
 
                        },parallel);
		}
		assertEquals('{"1":"A","2":1,"3":{"A":1}}',c);
	}


	public void function testEach() localMode="true" {
		arr=["a"];
		it=arr.iterator();

		savecontent variable="c" {
			res=each(it, function(){
							echo(serialize(arguments));
 							return true;
 
                        });
		}
		assertEquals('{"1":"a"}',c);
	}
} 
</cfscript>