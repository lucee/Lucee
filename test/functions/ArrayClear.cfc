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

	public void function testArrayClear(){
		arr=arrayNew(1);
		ArrayAppend( arr, 1 );
		ArrayAppend( arr, 2 );
		ArrayAppend( arr, 3 );
		arrayClear(arr);
		assertEquals(0,arrayLen(arr));

		arr=arrayNew(1);
		arr[10]=3;
		ArrayResize(arr, 20);
		ArrayAppend( arr, 1 );
		ArrayAppend( arr, 2 );
		ArrayAppend( arr, 3 );
		arrayClear(arr);
		assertEquals(0,arrayLen(arr));
		
		/*assertEquals("","");
		
		try{
			// error
			fail("");
		}
		catch(local.exp){}*/
	}

	public void function testArrayClearMemberFunction(){
		var arr = arrayNew(1);
		ArrayAppend( arr, 1 );
		ArrayAppend( arr, 2 );
		ArrayAppend( arr, 3 );
		arr.clear();
		assertEquals(0,arrayLen(arr));

		arr = arrayNew(1);
		arr[10] = 3;
		ArrayResize(arr, 20);
		ArrayAppend( arr, 1 );
		ArrayAppend( arr, 2 );
		ArrayAppend( arr, 3 );
		arr.clear();
		assertEquals(0,arrayLen(arr));
	}
} 
</cfscript>