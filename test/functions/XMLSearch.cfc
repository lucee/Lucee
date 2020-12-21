<!--- 
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.*
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

	variables.str='
	<aaa>
		<bbb ccc="ddd">eee</bbb>
		<fff ggg="123">iii</fff>
	</aaa>
';

	public void function testMemberFunction(){
		var x = xmlParse(variables.str);
		var text = x.search("/aaa/fff/text()")[1].xmlValue;
		assertEquals("iii",text);		
	}

	public void function testFunction(){
		var x = xmlParse(variables.str);
		var text = xmlsearch(x,"/aaa/fff/text()")[1].xmlValue;
		assertEquals("iii",text);		
	}

	public void function testNamespace(){
		var str = "<test:Response xmlns:test='https://test.com'> <test:Success> <test:user>testUser</test:user> </test:Success> </test:Response>";
		var xml = xmlParse(str);

		var searched = xmlSearch(xml, "test:Response/test:Success/test:user");
		var str=toString(searched[1]);

		assertEquals('<?xml version="1.0" encoding="UTF-8"?><test:user xmlns:test="https://test.com">testUser</test:user>',str);		
	}




	public void function testNumber(){
		var x = xmlParse(variables.str);
		var res = xmlsearch(x,"/aaa/fff/@ggg")[1].xmlName;
		assertEquals("ggg",res);
		
		var res = xmlsearch(x,"/aaa/fff/@ggg+1");
		assertEquals(124,res);
	}

	public void function testBoolean(){
		var x = xmlParse(variables.str);
		var res = xmlsearch(x,"/aaa/fff/@ggg")[1].xmlName;
		assertEquals("ggg",res);
		
		var res = xmlsearch(x,"/aaa/fff/@ggg=123");
		assertTrue(res);

		var res = xmlsearch(x,"/aaa/fff/@ggg=124");
		assertFalse(res);
	}
} 
</cfscript>
