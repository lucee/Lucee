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
		<fff ggg="hhh">iii</fff>
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

} 
</cfscript>