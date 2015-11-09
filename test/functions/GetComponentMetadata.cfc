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
	
	public function setUp(){
		variables.meta = getComponentMetadata("GetComponentMetadata.Test");
	}

	public void function testFunctions(){
		
		
		
		assertEquals(true,structKeyExists(meta,'functions'));
		assertEquals(3,arrayLen(meta.functions));
		
		assertEquals(true,isDefined('meta.extends.functions'));
		assertEquals(1,arrayLen(meta.extends.functions));
		
		var func=meta.extends.functions[1];
		assertEquals('public',func.access);
		assertEquals(false,func.closure);
		assertEquals('',func.description);
		assertEquals('hintAComponentPublic',func.hint);
		assertEquals('AComponentPublic',func.name);
		assertEquals(false,func.output);
		assertEquals('AComponent.cfc',ListLast(func.owner,'\/'));
		assertEquals('wddx',func.returnFormat);
		assertEquals('void',func.returntype);
		
		assertEquals(1,arraylen(func.parameters));
		
		var param=func.parameters[1];
		assertEquals('abc',param.default);
		assertEquals('info',param.hint);
		assertEquals('a',param.name);
		assertEquals(true,param.required);
		assertEquals('string',param.type);
		
	}
} 
</cfscript>