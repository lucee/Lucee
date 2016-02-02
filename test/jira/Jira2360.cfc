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


	function testBIF() {

		var q = query( "name": ["Micha", "Gert", "Tanja", "Mark", "Igal"], "country": ["CH", "CH", "CH", "UK", "US"] );

		var expected = {"name": "Tanja", "country": "CH"};

		assertEquals( queryRowData(q, 3), expected );
	}
	

	function testMember() {

		var q = query( "name": ["Micha", "Gert", "Tanja", "Mark", "Igal"], "country": ["CH", "CH", "CH", "UK", "US"] );

		var expected = {"name": "Micha", "country": "CH"};

		assertEquals( q.rowData(1), expected );
	}


	/**
	* @mxunit:expectedException Expression
	*/
	function testOutOfBounds() {

		var q = query( "name": ["Micha", "Gert", "Tanja", "Mark", "Igal"], "country": ["CH", "CH", "CH", "UK", "US"] );

		var expected = {"name": "Tanja", "country": "CH"};

		assertEquals( q.rowData(6), expected );
	}
	
}