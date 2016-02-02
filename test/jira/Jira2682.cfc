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

	public void function testLocalFileSystem(){
		if(find("Mac",server.os.name)) {
			assertEquals("/Users/mic/test.txt",getCanonicalPath("/Users\mic//test.txt"));
		}
		else if(find("Windows",server.os.name)) {
			assertEquals("d:\a\b\test.txt",getCanonicalPath("d:/a\b//test.txt"));
		}
	}
	
	public void function testZipFileSystem(){
		if(find("Mac",server.os.name)) {
			assertEquals("zip:///Users/mic/test.txt!/aa/bb/ccc.txt",getCanonicalPath("zip:///Users\mic/test.txt!/aa/bb\ccc.txt"));
		}
		else if(find("Windows",server.os.name)) {
			assertEquals("zip://d:\mic\test.txt!/aa/bb/ccc.txt",getCanonicalPath("zip://d:/mic/test.txt!/aa/bb\ccc.txt"));
		}
	}
	
	public void function testRamFileSystem(){
		assertEquals("ram:///aa/bb/cc.txt",getCanonicalPath("ram:///aa//bb\cc.txt"));
	}
} 
</cfscript>