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

	public void function testStruct() localmode="modern" {
		src = {"test & test":'test & test'};
		
		wddx action="cfml2wddx" input="#src#" output="wddx" validate='yes';
		wddx action="wddx2cfml" input="#wddx#" output="trg";
		
		assertEquals(serialize(src),serialize(trg));
	}
	public void function testQuery() localmode="modern" {
		src = query("a&1":[1,2,3]);

		wddx action="cfml2wddx" input="#src#" output="wddx" validate='yes';
		wddx action="wddx2cfml" input="#wddx#" output="trg";
		
		assertEquals(serialize(src),serialize(trg));
	}

	public void function testComponent() localmode="modern" {
		src = new "lucee.Component"();
		src["a&1"]=1;
		wddx action="cfml2wddx" input="#src#" output="wddx" validate='yes';
		
		wddx action="wddx2cfml" input="#wddx#" output="trg";
		assertEquals(serialize(src),serialize(trg));
	}


	private void function test() localmode="modern" {
		tmp = {"test & test":'test & test'};
		
		
		wddx action="cfml2wddx" input="#tmp#" output="wddxInvoicedata" validate='yes';

		//dump(var="#wddxInvoicedata#");
		//dump("#tmp#");

		wddx action="wddx2cfml" input="#wddxInvoicedata#" output="sameInvoicedata";
		//dump("#sameInvoicedata#");
		

		assertEquals("","");
		
		try{
			// error
			fail("");
		}
		catch(local.exp){}
	}
} 
</cfscript>