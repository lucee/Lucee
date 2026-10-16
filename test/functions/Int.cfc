<!--- 
 *
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.*
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
 --->
<cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase" {

	public void function testInt() localmode="true" {
		// basic positive numbers
		assertEquals(1, int(1.0));
		assertEquals(1, int(1.99));

		// floor semantics: int(n) == n for whole numbers
		assertEquals(2, int(2.0));

		// negative numbers must floor away from zero
		assertEquals(-2, int(-1.7));
		assertEquals(-1, int(-0.1));

		// 32-bit boundary: must NOT clamp to Integer.MAX_VALUE
		assertEquals(2147483648, int(2147483648));
		assertEquals(2147483648, int(2147483648.9));

		// large values: epoch timestamp in milliseconds
		assertEquals(1771804800000, int(1771804800000));
		assertEquals(1771804800000, int(1771804800000.9));

		// negative large values
		assertEquals(-2147483649, int(-2147483648.5));
	}

	public void function testFloor() localmode="true" {
		// basic positive numbers
		assertEquals(1, floor(1.0));
		assertEquals(1, floor(1.99));

		// negative numbers
		assertEquals(-2, floor(-1.7));

		// large values
		assertEquals(1771804800000, floor(1771804800000.9));
	}

}
</cfscript>