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
component extends='org.lucee.cfml.test.LuceeTestCase' {
	public void function trySavingLargeNestedStruct() {
		// Prove that it doesn't happen with nested structures
		var nestedStruct = {};
		var nestInMe = nestedStruct;
		// Make a big struct
		var nestedStruct = {};
		var v = ['a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'];
		for (var i in v) {
			for (var j in v) {
				for (var k in v) {
					for (var l in v) {
						nestedStruct[i][j][k][l] = {};
					}
				}

			}
		}
		debug('Nested struct len = '&len(serialize(nestedStruct)));
		ObjectSave(nestedStruct);
		debug('Nested struct saved without error');
	}
	
	
	public void function triggerUTFDataFormatException() {
		// Prove that it happens with objects nested deeply
		objTest = new Jira2698.TestObject( 500 );
		var res=ObjectSave(objTest);
		ObjectLoad(res);
	}
	
}