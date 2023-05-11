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
component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf"	{

	public void function testCFDocument(){
		try {
			document format="pdf" pagetype="A4" orientation="portrait" filename="test1038.pdf" overwrite="true" {
				echo("<p>This is where mickey mouse lives</p>");
			}
			pdf action="protect" encrypt="AES_128" source="test1038.pdf" newUserPassword="PDFPassword";
		}
		finally {
			if(fileExists("test1038.pdf")) fileDelete("test1038.pdf");
		}
	}

} 
</cfscript>