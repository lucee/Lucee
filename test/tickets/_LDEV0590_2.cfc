/*
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
 */
 component extends="org.lucee.cfml.test.LuceeTestCase"	{
	

	function testScoped() {
		arguments.one=123;
		
		var anonF = function () {
			var res="";
			try {
				res=variables.one; // must fail
			} 
			catch (any e) {}
			assertEquals("",res);
		};

		return anonF();
	}

	function testUnscoped() {
		arguments.one=123;
		
		var anonF = function () {
			var res="";
			try {
				res=one; // must NOT fail
			} 
			catch (any e) {}
			assertEquals(123,res);
		};

		return anonF();
	}
} 



