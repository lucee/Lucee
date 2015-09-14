<!---
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.*
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
component extends="org.lucee.cfml.test.LuceeTestCase" {

	//public function setUp() {}

	public void function test() {

		var struct = { "name": "Ruth's Chris' Steak House", "type": "Steak House", rating: 4.9 };

		assertStructEquals(struct, evaluate(serialize(struct)));

		var array = [1, 2, 4, 8, 16];

		assertArrayEquals(array, evaluate(serialize(array)));

		var compName = getComponentMetaData(this).name;
		var cfc = new "#compName#"();

		if (!isInstanceOf( evaluate( serialize(cfc) ), compName))
			fail("Serialization of Component failed");

		var jSB = createObject("java", "java.lang.StringBuilder").init();
		jSB.append("Lucee!");

		var eval = evaluate(serialize(jSB));
		if ((eval.getClass().getName() != "java.lang.StringBuilder") || (eval.toString() != "Lucee!"))
			fail("Serialization of Java failed")

	}

}
</cfscript>