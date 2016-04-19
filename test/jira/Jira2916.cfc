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

	public void function testScriptTagWithNoBody(){
		cfparam (name="local.a", default="AAA");
		assertEquals("AAA",a);
		
		// no delimiter between attributes
		cfparam (name="local.b" default="BBB");
		assertEquals("BBB",b);

		
		// use : instead of =
		cfparam (name:"local.c", default:"CCC");
		assertEquals("CCC",c);

		// and again without comma 
		cfparam (name:"local.d" default:"DDD");
		assertEquals("DDD",d);

		// used variable as value
		local.f="123";
		cfparam (name="local.g", default=f);
		assertEquals("123",g);
	}

	public void function testScriptTagWithBody(){
		local.q=query(a:[1,2,3]);

		cfquery (dbtype="query", name="local.q2") {
			echo("select * from q");
		}
		assertEquals("3",q2.recordcount);
	}


	public void function testScriptTagWithNoBodyOldStyle(){
		param name="local.a",default="AAA";
		assertEquals("AAA",a);
		
		// no delimiter between attributes
		param name="local.b" default="BBB";
		assertEquals("BBB",b);
	}

	public void function testScriptTagWithBodyOldSytle(){
		local.q=query(a:[1,2,3]);

		query dbtype="query" name="local.q2" {
			echo("select * from q");
		}
		assertEquals("3",q2.recordcount);
	}
		
} 
</cfscript>