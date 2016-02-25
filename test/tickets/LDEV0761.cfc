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
component extends="org.lucee.cfml.test.LuceeTestCase"	{

	LF=chr(10);
	CR=chr(13);
	CRLF=CR&LF;

	str="firstname,lastname<>Susi,Sorglos<>Sami,Chlaus";
	CSVParser=createObject("java","lucee.runtime.text.csv.CSVParser");
	

	public void function testLF(){
		var qry=CSVParser.toQuery(replace(str,"<>",LF,"all"), ',', '"', nullValue(), true );
		assertEquals(2,qry.recordcount);
		assertEquals("firstname,lastname",qry.columnlist);
		assertEquals("Susi",qry.firstname[1]);
		assertEquals("Chlaus",qry.lastname[2]);
	}

	public void function testCRLF(){
		var qry=CSVParser.toQuery(replace(str,"<>",CRLF,"all"), ',', '"', nullValue(), true );
		assertEquals(2,qry.recordcount);
		assertEquals("firstname,lastname",qry.columnlist);
		assertEquals("Susi",qry.firstname[1]);
		assertEquals("Chlaus",qry.lastname[2]);
	}

	public void function testCR() {
		var qry=CSVParser.toQuery(replace(str,"<>",CR,"all"), ',', '"', nullValue(), true );
		assertEquals(2,qry.recordcount);
		assertEquals("firstname,lastname",qry.columnlist);
		assertEquals("Susi",qry.firstname[1]);
		assertEquals("Chlaus",qry.lastname[2]);
	}
} 
</cfscript>