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
	

	public void function testStringFilter(){
        local.myStr = "diamonds";
        callback=function(chr){ 
            return chr!="d"; 
        } 
        assertEquals('iamons', StringFilter( local.myStr, callback ));
    
        local.myStr = "123456789";
        callback=function(chr){ 
            return chr>5; 
        }
        assertEquals('6789', local.myStr.filter(callback));
    }
} 
</cfscript>