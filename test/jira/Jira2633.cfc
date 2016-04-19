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
	
	public void function testVariablesSister(){
		rtn=new Jira2633.Test().getSisterVariables();
		rtn=rtn();
		
		assertEquals("2633",rtn.c); // local test (inside the outer closure)
		
		rtn.a();
		rtn.c=rtn.b();
		assertEquals("2633",rtn.c);
	}
	
	
	public void function testThisSister(){
		rtn=new Jira2633.Test().getSisterThis();
		rtn=rtn();
		
		assertEquals("2633",rtn.c); // local test (inside the outer closure)
		
		rtn.a();
		rtn.c=rtn.b();
		assertEquals("2633",rtn.c);
	}
	
	public void function testUndefinedSister(){
		rtn=new Jira2633.Test().getSisterUndefined();
		rtn=rtn();
		
		assertEquals("2633",rtn.c); // local test (inside the outer closure)
		
		rtn.a();
		rtn.c=rtn.b();
		assertEquals("2633",rtn.c);
	}
	
	
	
	
	
	
	
	
	
	
	
	public void function testVariablesLevel1(){
		c=new Jira2633.Test().getVariables();
		assertEquals("test->variables",c());
		
	}
	
	public void function testVariablesLevel2(){
		c=new Jira2633.Test().getVariables(2);
		c=c();
		assertEquals("test->variables",c());
	}
	
	
	public void function testThisLevel1(){
		c=new Jira2633.Test().getThis();
		assertEquals("test->this",c());
	}
	
	public void function testThisLevel2(){
		c=new Jira2633.Test().getThis(2);
		c=c();
		assertEquals("test->this",c());
	}
	
	public void function testUndefinedLevel1(){
		c=new Jira2633.Test().getUndefined();
		assertEquals("test->local",c());
	}
	
	public void function testUndefinedLevel2(){
		c=new Jira2633.Test().getUndefined(2);
		c=c();
		assertEquals("test->local",c());
	}
} 
</cfscript>