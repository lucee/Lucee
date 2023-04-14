<!--- 
 *
 * Copyright (c) 2016, Lucee Association Switzerland. All rights reserved.
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
	
	
	//public function afterTests(){}
	
	public function setUp(){}



	function testTouchOfNotExistingFile() localmode=true {
	    name="testtouch.txt";
	    
	    try{
	        assertFalse(fileExists(name));

	        fileTouch(name);
	        assertTrue(fileExists(name));

	        file action="info" file=name variable="res";
	        assertEquals(0,res.size);
	        
	    }
	    finally {
	        if(fileExists(name))
	            fileDelete(name);
	    }
	}
	function testTouchOfExistingFile() localmode=true {
	    name="testtouch.txt";
	    
	    try{
	        assertFalse(fileExists(name));

	        fileWrite(name,'Susi');
	        
	        fileTouch(name);
	        assertTrue(fileExists(name));

	        file action="info" file=name variable="res2";
	        assertEquals(4,res2.size);
	    }
	    finally {
	        if(fileExists(name))
	            fileDelete(name);
	    }
	}
} 
</cfscript>