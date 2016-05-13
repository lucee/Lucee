/*
 * Copyright (c) 2016, Lucee Association Switzerland. All rights reserved.
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
 */
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	//public function beforeTests(){}
	
	//public function afterTests(){}
	
	//public function setUp(){}


	public void function testRounding(){
		
		assertEquals("$0.00",DollarFormat(""));
		assertEquals("$1.00",DollarFormat("1"));
		// assertEquals("$11.98",DollarFormat("11.984")); ACF handles dollarFormat and lsCurrencyFormat not the same way!!!!
		//assertEquals("$11.99",DollarFormat("11.985"));
		assertEquals("$11.99",DollarFormat("11.986"));
		assertEquals("$1.33",DollarFormat("1.3333333"));
		assertEquals("$123.46",DollarFormat("123.46"));
		assertEquals("$2.00",DollarFormat("1.999999"));
		assertEquals("$1.77",DollarFormat("1.774"));
		assertEquals("$1.78",DollarFormat("1.776"));
	}


	public void function testFail(){
		try {
			assertEquals("$1.00",DollarFormat("one Dollar"));
			fail("must throw:invalid call of the function dollarFormat, first Argument (number) is invalid, Cant cast String [one Dollar] to a number");
		}
		catch(local.e){}
	}

	public void function testLocales(){
		var org=GetLocale();
		assertEquals("$200,000.00",DollarFormat(200000));
		setLocale('english (us)');
		assertEquals("$200,000.00",DollarFormat(200000));
		setLocale('english (uk)');
		assertEquals("$200,000.00",DollarFormat(200000));
		setLocale('german (swiss)');
		assertEquals("$200,000.00",DollarFormat(200000));

		setLocale(org);

	}


} 
