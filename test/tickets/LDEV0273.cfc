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
component extends="org.lucee.cfml.test.LuceeTestCase" skip=true	{
	
	public void function testInstantiateClassFromLocalJar(){
		var test=new LDEV0273.Test();
		assertEquals("var:false",test.exclaim());
		assertEquals(":true",test.exclaim(nullValue()));
		assertEquals("passed:false",test.exclaim('passed'));

		assertEquals("def:false",test.exclaim2());
		assertEquals(":true",test.exclaim2(nullValue()));
		assertEquals("passed:false",test.exclaim2('passed'));
	}
} 
</cfscript>