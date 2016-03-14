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

	public void function testBeforeAdvice(){
		local.meta = getComponentMetadata("Jira1668.MethodBeforeAdvice");
		
		assertEquals('MethodBeforeAdvice',listLast(meta.fullname,'.'));
		assertEquals('BeforeAdvice',listLast(meta.extends.BeforeAdvice.fullname,'.'));
		assertEquals('Advice',listLast(meta.extends.BeforeAdvice.extends.Advice.fullname,'.'));
		
		local.extends=ListSort(structKeyList(meta.extends),'textnocase');
		assertEquals('BeforeAdvice',extends);
	}
	public void function testTest(){
		
		local.meta = getComponentMetadata("Jira1668.test");
		assertEquals('Test',listLast(meta.fullname,'.'));
		assertEquals('MethodBeforeAdvice',listLast(meta.IMPLEMENTS.MethodBeforeAdvice.fullname,'.'));
		assertEquals('BeforeAdvice',listLast(meta.IMPLEMENTS.MethodBeforeAdvice.extends.BeforeAdvice.fullname,'.'));
		assertEquals('Advice',listLast(meta.IMPLEMENTS.MethodBeforeAdvice.extends.BeforeAdvice.extends.Advice.fullname,'.'));
		
		local.implements=ListSort(structKeyList(meta.implements),'textnocase');
		assertEquals('MethodBeforeAdvice',implements);
		
	}
} 
</cfscript>