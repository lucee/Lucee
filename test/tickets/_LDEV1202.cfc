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


LDEV-273

 ---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	

	public void function testCFMLElvis() {
		var c=new LDEV1202.Test2();
		assertEquals("",c.elvis());
	}

	public void function testLuceeElvis() {
		var c=new LDEV1202.Test();
		assertEquals("",c.elvis());
	}

	public void function testCFMLIsNull() {
		var c=new LDEV1202.Test2();
		assertTrue(c.isItNull());
	}

	public void function testLuceeIsNull() {
		var c=new LDEV1202.Test();
		assertTrue(c.isItNull());
	}
} 
</cfscript>