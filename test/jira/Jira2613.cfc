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

	public void function testListLen(){
		assertEquals(3,ListLen("a,,b,c,",",",false));
		assertEquals(5,ListLen("a,,b,c,",",",true));
	}
	
	
	public void function testListGetAtTrueMultipleDel(){
		assertEquals("",listGetAt("<>a<><>b<>c<>",1,"<>",true));
		assertEquals("a",listGetAt("a<><>b<>c<>",1,"<>",true));
		assertEquals("",listGetAt("a<><>b<>c<>",2,"<>",true));
		assertEquals("",listGetAt("a<><>b<>c<>",3,"<>",true));
		assertEquals("",listGetAt("a<><>b<>c<>",4,"<>",true));
		assertEquals("b",listGetAt("a<><>b<>c<>",5,"<>",true));
		assertEquals("b",listGetAt("a<><>b<>c<>",5,"<>",true));
		assertEquals("b",listGetAt("a<><>b<>c<>",5,"<>",true));
		assertEquals("",listGetAt("a<><>b<>c<>",6,"<>",true));
		assertEquals("c",listGetAt("a<><>b<>c<>",7,"<>",true));
		assertEquals("",listGetAt("a<><>b<>c<>",8,"<>",true));
		assertEquals("",listGetAt("a<><>b<>c<>",9,"<>",true));
	}
	
	public void function testListGetAtTrueSingleDel(){
		assertEquals("",listGetAt(",a,,b,c,",1,",",true));
		assertEquals("a",listGetAt("a,,b,c,",1,",",true));
		assertEquals("",listGetAt("a,,b,c,",2,",",true));
		assertEquals("b",listGetAt("a,,b,c,",3,",",true));
		assertEquals("c",listGetAt("a,,b,c,",4,",",true));
		assertEquals("",listGetAt("a,,b,c,",5,",",true));
		assertEquals("d",listGetAt("a,,b,c,d",5,",",true));
		assertEquals("b",listGetAt(",,a,,b,c,d",5,",",true));
	}
	
	public void function testListGetAtFalseSingleDel(){
		assertEquals("a",listGetAt("a,,b,c,",1,",",false));
		assertEquals("b",listGetAt("a,,b,c,",2,",",false));
		assertEquals("c",listGetAt("a,,b,c,",3,",",false));
		assertEquals("c",listGetAt(",,a,,b,c,",3,",",false));
		
		try{
			listGetAt("a,,b,c,",4,",",false);
			fail("must throw exception invalid index");
		}
		catch(local.exp){}
	}
	
	
	
	public void function testListGetAtFalseMultipleDel(){
		assertEquals("a",listGetAt("a<><>b<>c<>",1,"<>",false));
		assertEquals("b",listGetAt("a<><>b<>c<>",2,"<>",false));
		assertEquals("c",listGetAt("a<><>b<>c<>",3,"<>",false));
		assertEquals("c",listGetAt("<><>a<><>b<>c<>",3,"<>",false));
		
		try{
			listGetAt("a<><>b<>c<>",4,"<>",false);
			fail("must throw exception invalid index");
		}
		catch(local.exp){}
	}
	
	
	
	
	private void function testListGetAtLongDel(){
		assertEquals("c",listGetAt(listLongDel,3,"<>",false));
		assertEquals("b",listGetAt(listLongDel,3,"<>",false));
	}
	

	private void function testListGetAt5(){
		assertEquals("",listGetAt(list,5,",",true));
		try{
			assertEquals("",listGetAt(list,5,",",false));
			fail("must throw exception invalid index");
		}
		catch(local.exp){}
	}
} 
</cfscript>