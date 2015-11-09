/**
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
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

	public void function testTag(){
		local.static=Issue0275.TestTag::getTheStaticScope2();
		local.static=Issue0275.TestTag::getTheStaticScope();

		assertEquals(1,local.static.static1);
		assertEquals(2,local.static.static2);
		assertEquals(3,local.static.static3);
		assertEquals(4,local.static.static4);
		assertTrue(isCustomFunction(local.static.GETTHESTATICSCOPE));
		assertTrue(isCustomFunction(local.static.GETTHESTATICSCOPE2));

	}
	public void function testScript(){
		local.static=Issue0275.TestScript::getTheStaticScope2();
		local.static=Issue0275.TestScript::getTheStaticScope();

		assertEquals(1,local.static.static1);
		assertEquals(2,local.static.static2);
		assertTrue(isCustomFunction(local.static.GETTHESTATICSCOPE));
		assertTrue(isCustomFunction(local.static.GETTHESTATICSCOPE2));

	}
}