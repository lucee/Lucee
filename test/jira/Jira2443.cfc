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
	private function returnNull(){ }
	private function returnSusi(){return "Susi";}
	
	
	
	public void function testFunction(){
		assertEquals("Else",returnNull()?:"Else");
		assertEquals("Susi",returnSusi()?:"Else"); 
	}
	
	public void function testFunctionEvaluate(){
		assertEquals("Else",evaluate('returnNull()?:"Else"'));
		assertEquals("Susi",evaluate('returnSusi()?:"Else"')); 
	}

	public void function testVariables(){
		var d="b";
		var a.b.c="abc";
		
		assertEquals("Else",_does._not._exists?:"Else");
		assertEquals("abc",a.b.c?:"Else");
		assertEquals("abc",a[d].c?:"Else");
	}
	
	public void function testVariablesWithEvaluate(){
		var d="b";
		var a.b.c="abc";
		
		assertEquals("Else",evaluate('_does._not._exists?:"Else"'));
		assertEquals("abc",evaluate('a.b.c?:"Else"'));
		assertEquals("abc",evaluate('a[d].c?:"Else"'));
	}
} 
</cfscript>