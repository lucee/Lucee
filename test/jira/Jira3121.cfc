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
 --->﻿<cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	public function beforeTests(){
		setLocale("hu_hu");
	}

	function afterAll(){
		setLocale("en_us");
	}
	
	//public function afterTests(){}
	
	//public function setUp(){}

	public void function testOrderedQuery(){

		local.src = Query(
							"id": [1,2,3,4,5],
							"name": ["Computer", "Mouse", "Áccented name", "Keyboard", "Ánother accented"],
							"category": [1,2,1,1,1]);

		query dbType="query" name="local.trg"  {
			echo("
						SELECT DISTINCT *
						FROM src
						WHERE category = 1
						ORDER BY name");
		}
	}
	public void function testUnOrderedQuery(){
		local.src = Query(
							"id": [4,5,1,3,2],
							"name": ["Áccented name", "Ánother accented", "Computer", "Keyboard", "Mouse"],
							"category": [1,1,1,1,2]);
		query dbType="query" name="local.trg"  {
			echo("
						SELECT DISTINCT *
						FROM src
						WHERE category = 1");
		}
	}
} 
</cfscript>