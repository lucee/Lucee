/**
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
 **/


component extends="org.lucee.cfml.test.LuceeTestCase"	{

	public function setUp(){
		variables.arr=['Text 1'];
		variables.arr[3]="Text 3";
	}
	
	

	
	public void function testIndexAccess(){
		
		assertEquals(isNull(abc),true);
		assertEquals(isNull(variables.abc),true);
		assertEquals(isNull(url.abc),true);
		assertEquals(isNull(variables.abc.def.ghi.jkl),true);
		assertEquals(isNull(abc.def.ghi.jkl),true);
		
		
	assertEquals(isNull(v1),true);  
	v1=1;
	assertEquals(isNull(v1),false);  
	v1=nullValue();
	assertEquals(isNull(v1),true);  
	
	assertEquals(isNull(variables.v2),true);  
	variables.v2=1;
	assertEquals(isNull(variables.v2),false);  
	variables.v2=nullvalue();
	assertEquals(isNull(variables.v2),true);  
	
	assertEquals(isNull(this.t1),true);  
	this.t1=1;
	assertEquals(isNull(this.t1),false);  
	this.t1=nullvalue();
	assertEquals(isNull(this.t1),true);
     
	assertEquals(isNull(this.t2.a.b.c),true);  
	this.t2.a.b.c=1;
	assertEquals(isNull(this.t2.a.b.c),false);  
	this.t2.a.b.c=nullvalue();
	assertEquals(isNull(this.t2.a.b.c),true);
     
	assertEquals(isNull(local.v3),true);  
	local.v3=1;
	assertEquals(isNull(local.v3),false);  
	local.v3=nullvalue();
	assertEquals(isNull(local.v3),true);  
	
	assertEquals(isNull(a.b.c.d.e),true);  
	a.b.c.d.e=1;
	assertEquals(isNull(a.b.c.d.e),false);  
	a.b.c.d.e=nullValue();
	assertEquals(isNull(a.b.c.d.e),true);  
	
	assertEquals(isNull(variables.a1.b.c.d.e),true);  
	variables.a1.b.c.d.e=1;
	assertEquals(isNull(variables.a1.b.c.d.e),false);  
	variables.a1.b.c.d.e=nullValue();
	assertEquals(isNull(variables.a1.b.c.d.e),true);  
	
	dyn="abx";
	assertEquals(isNull(a.b[dyn].d.e),true);  
	a.b[dyn].d.e=1;
	assertEquals(isNull(a.b[dyn].d.e),false);  
	a.b[dyn].d.e=nullValue();
	assertEquals(isNull(a.b[dyn].d.e),true);  
	
	dyn="abx";
	assertEquals(isNull(variables.a2.b[dyn].d.e),true);  
	variables.a2.b[dyn].d.e=1;
	assertEquals(isNull(variables.a2.b[dyn].d.e),false);  
	variables.a2.b[dyn].d.e=nullValue();
	assertEquals(isNull(variables.a2.b[dyn].d.e),true);  
	
	
	variables.arr=[1];
	variables.arr[3]=3;
	assertEquals(isNull(variables.arr[1]),false);
	assertEquals(isNull(variables.arr[2]),true);
	assertEquals(isNull(variables.arr[3]),false);
  	
  	one=1;
  	two=2;
  	three=3;
  	
  	assertEquals(isNull(variables.arr[one]),false);
	assertEquals(isNull(variables.arr[two]),true);
	assertEquals(isNull(variables.arr[three]),false);
	}
	
}