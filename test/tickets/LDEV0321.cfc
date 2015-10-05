<!--- 
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.*
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

    public void function testExceptionStructKeyExists(){
    	try {
			1/0;
		}
		catch (any local.e) {
			assertEquals(true,structKeyExists(e, "type"));
		}
    }

    public void function testExceptionKeyExists(){
    	try {
			1/0;
		}
		catch (any local.e) {
			assertEquals(true,e.keyExists("type"));
		}
    }

    public void function testParam(){
    	try {
			1/0;
		}
		catch (any local.e) {
			param struct e;
		}
    }

    public void function testRequiresStruct(){
    	try {
			1/0;
		}
		catch (any local.e) {
			assertEquals(true,requiresStruct(e));
		}
    }

    public void function testReturnStruct(){
    	try {
			1/0;
		}
		catch (any local.e) {
			assertEquals(true,isStruct(returnsStruct(e)));
		}
    }
    public void function testDuplicate(){
    	try {
			1/0;
		}
		catch (any local.e) {
			assertEquals(true,isStruct(duplicate(e)));
		}
    }


    private function requiresStruct(required struct struct){
		return true;
	}

	private struct function returnsStruct(required any struct){
		return struct;
	}

}
</cfscript>