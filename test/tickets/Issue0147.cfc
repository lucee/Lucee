<!--- 
 *
 * Copyright (c) 2015, Lucee Associaction Switzerland. All rights reserved.
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
	wsUrl = "https://sb1.geolearning.com/geonext/testhudexchangelearn/webservices/geonext.asmx?wsdl";
	argSct.username = "scrubbed";
	argSct.password = "scrubbed";
	//public function beforeTests(){}
	
	//public function afterTests(){}
	
	//public function setUp(){}

	public void function testLucee(){
		assertTrue(structKeyExists(server,'lucee'));
		assertTrue(ListFind(structKeyList(server),"lucee")>0);
		var v=server.lucee.version;
	}

	public void function testRailo(){
		assertFalse(structKeyExists(server,'railo'));
		assertFalse(ListFind(structKeyList(server),"railo")>0);
		var v=server.railo.version; // that is working, but it should be the only one
	}
} 
</cfscript>