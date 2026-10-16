/**
 * Copyright (c) 2016, Lucee Association Switzerland. All rights reserved.
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
component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {

		describe( "isNull() with partial null support (default)", function() {

			beforeEach( function() {
				setNullSupport( false );
			});

			describe( "unscoped variable access (scope cascading)", function() {

				it( "returns true for undefined variable", function() {
					expect( isNull( undefinedVar ) ).toBeTrue();
				});

				it( "returns true for nested undefined property chain", function() {
					expect( isNull( a.b.c.d.e ) ).toBeTrue();
				});

				it( "returns false for defined variable", function() {
					var b = 1;
					expect( isNull( b ) ).toBeFalse();
				});

				it( "returns true for undefined property on defined variable", function() {
					var b = 1;
					expect( isNull( b.c ) ).toBeTrue();
				});

				it( "returns true for nullValue()", function() {
					var x = nullValue();
					expect( isNull( x ) ).toBeTrue();
				});

				it( "returns false when local null does not shadow with partial null support", function() {
					variables.shadowTest = 1;
					var shadowTest = nullValue();
					expect( isNull( shadowTest ) ).toBeFalse(); // null removes key, so cascade finds variables
				});

				it( "returns false when unscoped finds variables value", function() {
					variables.cascadeTest = 1;
					expect( isNull( cascadeTest ) ).toBeFalse();
				});

			});

			describe( "scoped variable access (no cascading)", function() {

				it( "returns true for undefined local variable", function() {
					expect( isNull( local.undefinedLocalVar ) ).toBeTrue();
				});

				it( "returns true for undefined variables scope variable", function() {
					expect( isNull( variables.undefinedVarScopeVar ) ).toBeTrue();
				});

				it( "returns true for nested undefined in variables scope", function() {
					expect( isNull( variables.a.b.c.d.e ) ).toBeTrue();
				});

				it( "returns true for local variable set to nullValue()", function() {
					var x = nullValue();
					expect( isNull( local.x ) ).toBeTrue();
				});

				it( "returns true for variables scope set to nullValue()", function() {
					variables.nullVar = nullValue();
					expect( isNull( variables.nullVar ) ).toBeTrue();
				});

			});

			describe( "function return values", function() {

				it( "returns false for existing key in function return", function() {
					expect( isNull( getStruct().a ) ).toBeFalse();
				});

				// LDEV-1264: isNull() should handle undefined keys on function returns but currently throws
				it( title="returns true for nested undefined key in function return", skip=true, body=function() {
					expect( isNull( getStruct().a.b ) ).toBeTrue();
				});

				it( title="returns true for undefined key in function return", skip=true, body=function() {
					expect( isNull( getStruct().b ) ).toBeTrue();
				});

			});

		});

		describe( "isNull() with full null support", function() {

			beforeEach( function() {
				setNullSupport( true );
			});

			afterEach( function() {
				setNullSupport( false );
			});

			describe( "unscoped variable access (scope cascading)", function() {

				it( "returns true for undefined variable", function() {
					expect( isNull( undefinedVar ) ).toBeTrue();
				});

				it( "returns true for nested undefined property chain", function() {
					expect( isNull( a.b.c.d.e ) ).toBeTrue();
				});

				it( "returns false for defined variable", function() {
					var b = 1;
					expect( isNull( b ) ).toBeFalse();
				});

				it( "returns true for variable assigned null keyword", function() {
					var x = null;
					expect( isNull( x ) ).toBeTrue();
				});

				it( "returns true for nullValue()", function() {
					var x = nullValue();
					expect( isNull( x ) ).toBeTrue();
				});

				it( "returns true when local null shadows variables value", function() {
					variables.shadowTestFull = 1;
					var shadowTestFull = null;
					expect( isNull( shadowTestFull ) ).toBeTrue();
				});

				it( "returns false when unscoped finds variables value", function() {
					variables.cascadeTestFull = 1;
					expect( isNull( cascadeTestFull ) ).toBeFalse();
				});

			});

			describe( "scoped variable access (no cascading)", function() {

				it( "returns true for undefined local variable", function() {
					expect( isNull( local.undefinedLocalVar ) ).toBeTrue();
				});

				it( "returns true for undefined variables scope variable", function() {
					expect( isNull( variables.undefinedVarScopeVar ) ).toBeTrue();
				});

				it( "returns true for local variable set to null", function() {
					var x = null;
					expect( isNull( local.x ) ).toBeTrue();
				});

				it( "returns true for variables scope set to null", function() {
					variables.nullVarFull = null;
					expect( isNull( variables.nullVarFull ) ).toBeTrue();
				});

			});

			describe( "function return values", function() {

				it( "returns false for existing key in function return", function() {
					expect( isNull( getStruct().a ) ).toBeFalse();
				});

				// LDEV-1264: isNull() should handle undefined keys on function returns but currently throws
				it( title="returns true for undefined key in function return", skip=true, body=function() {
					expect( isNull( getStruct().b ) ).toBeTrue();
				});

			});

		});

	}

	private function setNullSupport( required boolean enabled ) {
		application action="update" nullsupport="#arguments.enabled#";
	}

	private struct function getStruct() {
		return { a: 1 };
	}

}
