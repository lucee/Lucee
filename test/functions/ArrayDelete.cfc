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

	public void function testArrayDelete(){
		var arr=[1,2,3,4];
		assertEquals("1,2,3,4",arrayToList(arr));
		arrayDelete(arr,2)
		assertEquals("1,3,4",arrayToList(arr));
		arrayDelete(arr,1);
		assertEquals("3,4",arrayToList(arr));
		assertEquals(false,arrayDelete(arr,1));
		assertEquals(true,arrayDelete(arr,3));
		
		
		arr=['SUSI'];
		assertEquals("SUSI",arrayToList(arr));
		arrayDelete(arr,'SUSI');
		assertEquals("",arrayToList(arr));

		arr=[1,1,1];
		assertEquals("1,1,1",arrayToList(arr));
		arrayDelete(arr,1)
		assertEquals("1,1",arrayToList(arr));
	
		arr=['SUSI','susi'];
		assertEquals("SUSI,susi",arrayToList(arr));
		arrayDelete(arr,'SUSI');
		assertEquals("susi",arrayToList(arr));
		
		n=now();
		arr=[now()];
		arrayDelete(arr,n);
		assertEquals("",arrayToList(arr));
		
		
		/*assertEquals("","");
		
		try{
			// error
			fail("");
		}
		catch(local.exp){}*/
	}
} 
</cfscript>