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
	
	//public function beforeTests(){}
	
	//public function afterTests(){}
	
	//public function setUp(){}


	public void function testBase(){
		var base =LDEV0835.Base::getData();
		var a =LDEV0835.A::getData();
		var b =LDEV0835.B::getData();
		assertEquals("Base:Base",base);
	}
	public void function testA(){
		var base =LDEV0835.Base::getData();
		var a =LDEV0835.A::getData();
		var b =LDEV0835.B::getData();
		assertEquals("A:A",a);
	}
	public void function testB(){
		var base =LDEV0835.Base::getData();
		var a =LDEV0835.A::getData();
		var b =LDEV0835.B::getData();
		assertEquals("B:Base",b);
	}


} 



