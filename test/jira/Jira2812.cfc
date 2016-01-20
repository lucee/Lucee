/**
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
 **/
component extends="org.lucee.cfml.test.LuceeTestCase"	{


	function testLowerIfAllUppercase() {

		assertEquals( compare( ucFirst( "SUSI SORGOLIS", true, true ), "Susi Sorgolis" ), 0 );
		assertEquals( compare( ucFirst( "SUSI Q. SORGOLIS", true, true ), "Susi Q. Sorgolis" ), 0 );
		assertEquals( compare( ucFirst( "Susi Q. Sorgolis", true, true ), "Susi Q. Sorgolis" ), 0 );
		assertEquals( compare( ucFirst( "susi q. sorgolis", true, true ), "Susi Q. Sorgolis" ), 0 );
		assertEquals( compare( ucFirst( "Ronald McDonald", true, true ), "Ronald McDonald" ), 0 );		
		assertEquals( compare( ucFirst( "ronald mcDonald", true, true ), "Ronald McDonald" ), 0 );		
		assertEquals( compare( ucFirst( "ronald mcdonald", true, true ), "Ronald Mcdonald" ), 0 );		
	}


	function testOldFunctionality() {

		assertEquals( compare( ucFirst( "lucee technologies" ), "Lucee technologies" ), 0 );
		assertEquals( compare( ucFirst( "lucee technologies", true ), "Lucee Technologies" ), 0 );
		assertEquals( compare( ucFirst( "lucee 		technologies", true ), "Lucee Technologies" ), 0 );
		assertEquals( compare( ucFirst( "the 		lucee   company", true ), "The Lucee Company" ), 0 );
		assertEquals( compare( ucFirst( "michael offner-streit", false ), "Michael offner-streit" ), 0 );
		assertEquals( compare( ucFirst( "michael			offner-streit", true ), "Michael Offner-Streit" ), 0 );
		assertEquals( compare( ucFirst( "international  business 		machines (i.b.m.)", true ), "International Business Machines (I.B.M.)" ), 0 );
		assertEquals( compare( ucFirst( "jon doe  jr.", true ), "Jon Doe Jr." ), 0 );
	}
	
}