/*
 * Copyright (c) 2018, Lucee Assosication Switzerland. All rights reserved.*
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


	public void function testAttributeName() localMode=true {

		attr = {
			name : "qDir"
		};

		directory directory=expandPath('.') attributeCollection=attr;
	}


	public void function testAttributeAlias() localMode=true {

		attr = {
			variable : "qDir"
		};

		directory directory=expandPath('.') attributeCollection=attr;
	}

}
