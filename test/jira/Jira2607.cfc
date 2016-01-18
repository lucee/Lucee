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

	public void function testLocal(){
		setting showdebugoutput="false";
		local.local1 = "test";
		local.fn = function() {
			assertEquals("test",local1);
		};
        local.fn();
	}

	public void function testArguments(){
		_testArguments("test");
	}
	
	private void function _testArguments(arg1){
		local.fn = function() {
			assertEquals("test",arg1);
		};
        local.fn();
	}
	
	

	public void function testVariables(){
		variables._test2607="test";
		local.fn = function() {
			assertEquals("test",_test2607);
		};
        local.fn();
	}
} 
</cfscript>