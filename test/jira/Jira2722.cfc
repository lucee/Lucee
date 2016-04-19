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

	public void function testMimetypeParser(){
		var MimeType=createObject('java','lucee.commons.lang.mimetype.MimeType');
		
		// some regular cases
		assertEquals("application/json",MimeType.getInstance("application/json").toString());
		assertEquals("text/html; charset=utf-8",MimeType.getInstance("text/html;charset=utf-8").toString());
		assertEquals("text/html; charset=utf-8",MimeType.getInstance("text/html;charset=utf-8;").toString());
		assertEquals("text/html; charset=utf-8; susi=sorglos",MimeType.getInstance("text/html;charset=utf-8;susi=sorglos").toString());
		
		
		// some special cases
		assertEquals("application/*",MimeType.getInstance("application").toString());
		assertEquals("*/*",MimeType.getInstance("*").toString());
		assertEquals("*/*",MimeType.getInstance("").toString());
		
		// some invalid cases (that make the functionality break)
		assertEquals("*/*",MimeType.getInstance("*/").toString());
		assertEquals("*/*",MimeType.getInstance("/").toString());
		assertEquals("*/*",MimeType.getInstance("/;").toString());
		assertEquals("*/*",MimeType.getInstance("*;*").toString());
		assertEquals("*/*",MimeType.getInstance("/;=").toString());
		assertEquals("*/*",MimeType.getInstance("*;=").toString());
		assertEquals("*/*",MimeType.getInstance(";*").toString());
		
		
		/*try{
			// error
			fail("");
		}
		catch(local.exp){}*/
	}
} 
</cfscript>