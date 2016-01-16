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

	public void function testUnaryWithFunctions(){
		assertEquals(0,--getVariables().value1);
		assertEquals(2,++getVariables().value1);
		
		assertEquals(1,getVariables().value1--);
		assertEquals(1,getVariables().value1++);
		
		assertEquals(3,getVariables().value1+=2);
		assertEquals(-1,getVariables().value1-=2);
		assertEquals(2,getVariables().value1*=2);
		assertEquals(0.5,getVariables().value1/=2);
		assertEquals("12",getVariables().value1&=2);
	}

	public void function testUnaryUnscopedMultiple(){
		variables.a.b.value1=1;
		assertEquals(0,--a.b.value1);
		assertEquals(1,++a.b.value1);
		
		assertEquals(1,a.b.value1--);
		assertEquals(0,a.b.value1++);
		
		assertEquals(3,a.b.value1+=2);
		assertEquals(1,a.b.value1-=2);
		assertEquals(2,a.b.value1*=2);
		assertEquals(1,a.b.value1/=2);
		assertEquals("12",a.b.value1&=2);
	}

	public void function testUnaryScopedMultiple(){
		variables.a.b.value1=1;
		assertEquals(0,--variables.a.b.value1);
		assertEquals(1,++variables.a.b.value1);
		
		assertEquals(1,variables.a.b.value1--);
		assertEquals(0,variables.a.b.value1++);
		
		assertEquals(3,variables.a.b.value1+=2);
		assertEquals(1,variables.a.b.value1-=2);
		assertEquals(2,variables.a.b.value1*=2);
		assertEquals(1,variables.a.b.value1/=2);
		assertEquals("12",variables.a.b.value1&=2);
	}

	public void function testUnaryUnscopedSingle(){
		variables.value1=1;
		assertEquals(0,--value1);
		assertEquals(1,++value1);
		
		assertEquals(1,value1--);
		assertEquals(0,value1++);
		
		assertEquals(3,value1+=2);
		assertEquals(1,value1-=2);
		assertEquals(2,value1*=2);
		assertEquals(1,value1/=2);
		assertEquals("12",value1&=2);
	}

	private struct function getVariables(){
		return {'value1':1,'value2':2,'value3':3,'value4':4};
	}
} 
</cfscript>