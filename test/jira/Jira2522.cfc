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

	public function setUp(){
		
	}
	
	public void function test1(){
		assertEquals("true:false",cachedFunction("a;b","c"));
	}
	
	public void function test2(){
		assertEquals("false:true",cachedFunction("a","b;c"));
	}

	private string function cachedfunction(required param1, required param2)  cachedWithin="#createTimeSpan(0,0,0,1)#" {
		return (find(";",param1)>0)&":"&(find(";",param2)>0);
	}
		
} 
</cfscript>