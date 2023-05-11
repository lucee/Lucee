<!--- 
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
component extends="org.lucee.cfml.test.LuceeTestCase"  labels="pdf"	{

	public void function testCFDocument(){
		try {
			document format="pdf" pagetype="A4" orientation="portrait" filename="test759.pdf" overwrite="true" {
				echo("<p>This is where mickey mouse lives</p>");
			}
			var info=fileInfo("test759.pdf");
			assertTrue(info.size>900);
		}
		finally {
			if(fileExists("test759.pdf"))fileDelete("test759.pdf");
		}
	}

} 
</cfscript>