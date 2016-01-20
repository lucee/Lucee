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
 --->﻿<!--- !!!!!!!!!!!!!! make sure not changing the encoding of this template --->
<cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	processingdirective pageEncoding="UTF-8";
	//public function beforeTests(){}
	
	//public function afterTests(){}
	
	//public function setUp(){}

	public void function testUTF8(){
		assertEquals('"öäü"',serializejson(var:"öäü",charset:'utf-8'));
	}
	public void function testISO8859_1(){
		//assertEquals('"öäü"',serializejson(var:"öäü",charset:'iso-8859-1'));
	}
	public void function testUS_ASCII(){
		assertEquals('"\u00f6\u00e4\u00fc-\u00e9"',serializejson(var:"öäü-é",charset:'us-ascii'));
	}
} 
</cfscript>