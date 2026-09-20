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
component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run() {

		describe("listDeleteAt basic tests", function() {

			it("base line tests", function() {
				assertEquals("a,c,d",       ListDeleteAt('a,b,c,d',2));
				assertEquals(",,a,b,d,,",   ListDeleteAt(',,a,b,c,d,,',3));
				assertEquals(",,b,c,d,,",   ListDeleteAt(',,a,b,c,d,,',3,',',true));
				assertEquals(",a,b,d,,",    ListDeleteAt(',a,b,c,d,,',3,',',false));
			});
		});

		describe("LDEV-5803 review theslistDeleteAt Functionality", function() {

			it("Deletes element at given position (default/comma delimiter)", function() {
				expect( listDeleteAt("a,b,c,d", 3) ).toBe("a,b,d");
			});

			it("Handles first position", function() {
				expect( listDeleteAt("a,b,c,d", 1) ).toBe("b,c,d");
			});

			it("Handles last position", function() {
				expect( listDeleteAt("a,b,c,d", 4) ).toBe("a,b,c");
			});

			it("Handles single-element list", function() {
				expect( listDeleteAt("a", 1) ).toBe("");
			});

			it("Handles out-of-bounds position (>length)", function() {
				expect( function(){
					listDeleteAt("a,b,c", 5);
				}).toThrow( "", "index must be an integer" );
			});

			it("LDEV-720 Handles zero or negative position", function() {
				expect( function(){
					listDeleteAt("a,b,c", 0);
				}).toThrow( "", "index must be greater than 0" );
				expect( function(){
					listDeleteAt("a,b,c", -1);
				}).toThrow( "", "index must be greater than 0" );
			});

			it("Handles alternate delimiters", function() {
				expect( listDeleteAt("a|b|c|d", 2, "|") ).toBe("a|c|d");
			});

			it("Handles multi-character delimiters", function() {
				expect( listDeleteAt("a--b--c", 2, "--") ).toBe("a--c");
			});

			it("Handles includeEmptyFields argument", function() {
				expect( listDeleteAt("a,,b", 2, ",", true) ).toBe("a,b");
			});

			it("Handles lists with empty elements", function() {
				expect( listDeleteAt(",a,b,,c,,", 5, ",", true) ).toBe(",a,b,,,");
			});

			it("Handles removing element from empty list", function() {
				expect( listDeleteAt("", 1) ).toBe("");
			});

			it("Deletes empty element at start", function() {
				expect( listDeleteAt(",a,b", 1, ",", true) ).toBe("a,b");
			});

			it("Deletes empty element at end", function() {
				expect( listDeleteAt("a,b,", 3, ",", true) ).toBe("a,b");
			});

			it("Handles all empty list", function() {
				expect( listDeleteAt(",,", 2, ",", true) ).toBe(",");
			});

			it("Deletes inner empty element", function() {
				expect( listDeleteAt("a,,b", 2, ",", true) ).toBe("a,b");
			});

		});
	}
}