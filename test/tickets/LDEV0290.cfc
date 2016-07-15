<!--- 
 *
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
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

	public void function testInstantiateClassFromLocalJar(){
		var Test=createObject("java","test.Test","LDEV0290/test.jar");
		assertEquals("Susi Susi",Test.doubleIt("Susi"));
		assertEquals("Susi Susi Susi",Test.tribleIt("Susi"));
	}

	public void function testInstantiateClassFromServletLibFolder(){
		// only works if the jar "LDEV0290/test2.jar" is in lib folder
		var Test=createObject("java","from.classpath.Test");
		assertEquals("Susi Susi",Test.doubleIt("Susi"));
		assertEquals("Susi Susi Susi",Test.tribleIt("Susi"));
	}

} 
</cfscript>