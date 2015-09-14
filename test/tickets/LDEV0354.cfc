/**
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
 */
component extends="org.lucee.cfml.test.LuceeTestCase"	{

	public void function testIsEmptyStruct(){
		assertTrue(isEmpty({}));
		assertFalse(isEmpty({a:1}));
	}

	public void function testIsEmptyArray(){
		assertTrue(isEmpty([]));
		assertFalse(isEmpty([1]));
	}

	public void function testIsEmptyString(){
		assertTrue(isEmpty(""));
		assertFalse(isEmpty("true"));
		assertFalse(isEmpty("false"));
	}

	public void function testIsEmptyBoolean(){
		assertFalse(isEmpty(true));
		assertFalse(isEmpty(false));
	}

	public void function testIsEmptyNumber(){
		assertFalse(isEmpty(1));
		assertFalse(isEmpty(0));
	}
}