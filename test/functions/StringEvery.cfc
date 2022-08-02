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
	

	public void function testStringEvery(){
		local.testString = "123456789";
		
		var res="";
		callback = function(chr){
				res=chr;
				return false;
		}
		StringEvery( local.testString, callback )
		assertEquals( "1", res); 

		var res="";
		callback = function(chr){
				res=chr;
				return true;
		}
		StringEvery( local.testString, callback )
		assertEquals( "9", res); 

		var res="";
		callback_1 = function(chr){
				res=chr;
				return false;
		}
		local.testString.every( callback_1 );
		assertEquals( "1", res);
        }       
} 
</cfscript>