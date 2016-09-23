<!--- 
 *
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.
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
	pageencoding "utf-8"; 
component extends="org.lucee.cfml.test.LuceeTestCase" {

	public void function testReplaceNoCase() {
		var str="LİELİELİELİE....";
		var expectedResult="Lİa very long string to replace ELİa very long string to replace ELİa very long string to replace ELİa very long string to replace E....";
	
		var repl="a very long string to replace E";
		var resultNoCase=replaceNoCase(str,"E",repl,"all");
		var result=replaceNoCase(str,"E",repl,"all");

		assertEquals(expectedResult,result);
		assertEquals(expectedResult,resultNoCase);
		assertEquals(result,resultNoCase);
	}
} 
</cfscript>
