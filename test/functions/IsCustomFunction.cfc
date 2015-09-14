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
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	//public function beforeTests(){}
	
	//public function afterTests(){}
	
	//public function setUp() localmode="true"{}
	private function realUDF() {
	   return 1;
	}

	public void function testNonFunction() localmode="true"{
		assertEquals(false,isCustomFunction(""));
		
	} 

	public void function testUDF() localmode="true"{
		assertEquals(true,isCustomFunction(realUDF));

		assertEquals(false,isCustomFunction(function (a,b,c){},"udf"));
		assertEquals(false,isCustomFunction((x) -> x * x ,"udf"));
		assertEquals(true,isCustomFunction(realUDF,"udf"));
		
	} 

	public void function testClosure() localmode="true"{
		assertEquals(true,isCustomFunction(function (a,b,c){}));
		assertEquals(true,isCustomFunction(function (a,b,c){},"closure"));
		assertEquals(false,isCustomFunction((x) -> x * x ,"closure"));
		assertEquals(false,isCustomFunction(realUDF,"closure"));

	} 

	public void function testLambda() localmode="true"{
		assertEquals(true,isCustomFunction((x) -> x * x ));

		assertEquals(false,isCustomFunction(function (a,b,c){},"lambda"));
		assertEquals(true,isCustomFunction((x) -> x * x ,"lambda"));
		assertEquals(false,isCustomFunction(realUDF,"lambda"));
	} 

} 
</cfscript>