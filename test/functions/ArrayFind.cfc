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

	public void function testArrayFind(){
		
		valueEquals(ArrayFind(listToArray('abba,bb'),'bb'), "2");
		valueEquals(ArrayFind(listToArray('abba,bb,AABBCC,BB'),'BB'),"4");
		valueEquals(ArrayFind(listToArray('abba,bb,AABBCC'),'ZZ'), "0");
		
		var arr=["hello","world"];
    
    	// UDF
    	var res=ArrayFind(arr,doFind);
    	valueEquals(res,2);
    
    	// Closure
    doFind=function (value){
        return value EQ "world";
    };
    res=ArrayFind(arr,doFind);
    valueEquals(res,2);
		
		/*assertEquals("","");
		
		try{
			// error
			fail("");
		}
		catch(local.exp){}*/
	}
	
	private function doFind(value){
        return value EQ "world";
    }
    
	private function valueEquals(left,right) {
		assertEquals(arguments.right,arguments.left);
	}
} 
</cfscript>