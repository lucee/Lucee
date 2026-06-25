component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-6428 Reduce member function runtime execution overhead", function() {

			// These tests pin the behaviour of `Casting.getValue` on the queryColumn
			// special-case branch -- only reached via the CFML expression interpreter
			// path (evaluate, dynamic dispatch), NOT compiled bytecode. Compiled paths
			// for valueList/valueArray/quotedValueList go through CastOther, a
			// different code path. The interpreter path through Casting.java:68 was
			// previously untested.

			describe( "queryColumn cast via interpreted expression (Casting.getValue)", function() {

				it( "evaluate( 'valueList(q.col)' ) resolves to delimited column values", function() {
					var q = queryNew( "a", "varchar", [ [ "x" ], [ "y" ], [ "z" ] ] );
					expect( evaluate( "valueList(q.a)" ) ).toBe( "x,y,z" );
				});

				it( "evaluate( 'valueList(q.col, delim)' ) honours custom delimiter", function() {
					var q = queryNew( "a", "varchar", [ [ "1" ], [ "2" ], [ "3" ] ] );
					expect( evaluate( "valueList(q.a, ';')" ) ).toBe( "1;2;3" );
				});

				it( "evaluate( 'valueArray(q.col)' ) returns array of column values", function() {
					var q = queryNew( "a", "integer", [ [ 1 ], [ 2 ], [ 3 ] ] );
					var result = evaluate( "valueArray(q.a)" );
					expect( arrayLen( result ) ).toBe( 3 );
					expect( result[ 1 ] ).toBe( 1 );
					expect( result[ 2 ] ).toBe( 2 );
					expect( result[ 3 ] ).toBe( 3 );
				});

				it( "evaluate( 'quotedValueList(q.col)' ) returns quoted delimited values", function() {
					var q = queryNew( "a", "varchar", [ [ "foo" ], [ "bar" ] ] );
					expect( evaluate( "quotedValueList(q.a)" ) ).toBe( "'foo','bar'" );
				});

				it( "queryColumn cast handles single-row query", function() {
					var q = queryNew( "a", "varchar", [ [ "only" ] ] );
					expect( evaluate( "valueList(q.a)" ) ).toBe( "only" );
				});

				it( "queryColumn cast handles empty query", function() {
					var q = queryNew( "a", "varchar" );
					expect( evaluate( "valueList(q.a)" ) ).toBe( "" );
					expect( arrayLen( evaluate( "valueArray(q.a)" ) ) ).toBe( 0 );
				});
			});

			describe( "queryColumn cast via compiled bytecode (CastOther) -- regression guard", function() {

				it( "valueList(q.col) inline resolves correctly", function() {
					var q = queryNew( "a", "varchar", [ [ "x" ], [ "y" ], [ "z" ] ] );
					expect( valueList( q.a ) ).toBe( "x,y,z" );
				});

				it( "valueArray(q.col) inline resolves correctly", function() {
					var q = queryNew( "a", "integer", [ [ 1 ], [ 2 ], [ 3 ] ] );
					var result = valueArray( q.a );
					expect( arrayLen( result ) ).toBe( 3 );
				});

				it( "quotedValueList(q.col) inline resolves correctly", function() {
					var q = queryNew( "a", "varchar", [ [ "foo" ], [ "bar" ] ] );
					expect( quotedValueList( q.a ) ).toBe( "'foo','bar'" );
				});
			});

			describe( "member-method dispatch -- MemberUtil.call -> BIFCall -> Casting.getValue", function() {

				it( "qry.valueList(column) member call resolves", function() {
					var q = queryNew( "a", "varchar", [ [ "x" ], [ "y" ], [ "z" ] ] );
					expect( q.valueList( "a" ) ).toBe( "x,y,z" );
				});

				it( "struct member call (struct.keyArray) dispatches through MemberUtil", function() {
					var s = { foo=1, bar=2 };
					var keys = s.keyArray();
					expect( arrayLen( keys ) ).toBe( 2 );
				});

				it( "array member call (array.len) dispatches through MemberUtil", function() {
					var a = [ 10, 20, 30 ];
					expect( a.len() ).toBe( 3 );
				});

				it( "member call with named args dispatches through BIFCall named-arg path (line 113)", function() {
					var s = { foo=1, bar=2 };
					// structKeyExists named-arg via member
					expect( s.keyExists( key="foo" ) ).toBeTrue();
					expect( s.keyExists( key="missing" ) ).toBeFalse();
				});
			});

			describe( "evaluate() named-arg dispatch -- BIFCall.getValue:113 path", function() {

				it( "evaluate with named args resolves through Casting wrapper in BIFCall", function() {
					expect( evaluate( "structKeyExists(struct={a:1,b:2}, key='a')" ) ).toBeTrue();
					expect( evaluate( "structKeyExists(struct={a:1,b:2}, key='zz')" ) ).toBeFalse();
				});
			});

			describe( "exception messages must match pre-LDEV-6428 .143 baseline", function() {

				// These tests lock in the exact exception messages produced on .143 (the
				// pre-LDEV-6428 baseline). Captured from D:/tmp/ldev6428-throws/run-143.log
				// on 2026-06-25. Any deviation indicates a behavioural change in error
				// reporting that downstream users may grep against.

				it( "wrong arg type (replace with struct) keeps the 'second Argument [sub1] is invalid' message", function() {
					try {
						"zac,claude,joshi".replace( { x = 1 }, "+" );
						fail( "expected exception" );
					}
					catch ( any e ) {
						expect( e.type ).toBe( "expression" );
						expect( e.message ).toBe( "Invalid call of the function [replace], second Argument [sub1] is invalid, When passing three parameters or more, the second parameter must be a simple value." );
					}
				});

				it( "missing required arg (arr.append()) keeps the 'too few arguments for function [ArrayAppend] call' message", function() {
					try {
						var arr = [ 1, 2, 3 ];
						arr.append();
						fail( "expected exception" );
					}
					catch ( any e ) {
						expect( e.type ).toBe( "expression" );
						expect( e.message ).toBe( "too few arguments for function [ArrayAppend] call" );
					}
				});

				it( "too many args (arr.len(...extras...)) keeps the 'too many arguments for function [arraylen] call' message", function() {
					try {
						var arr = [ 1, 2, 3 ];
						arr.len( "extra1", "extra2", "extra3", "extra4" );
						fail( "expected exception" );
					}
					catch ( any e ) {
						expect( e.type ).toBe( "expression" );
						expect( e.message ).toBe( "too many arguments for function [arraylen] call" );
					}
				});

				it( "receiver type mismatch (numeric.keyArray) keeps the 'function [keyArray] does not exist in the Numeric' message shape", function() {
					// The "Available functions are [...]" list is runtime-derived from the
					// FLD member-function registry plus any-typed BIFs. Installed extensions
					// can add or remove entries from this list, so we lock the message shape
					// (prefix + bracketed list + ending) rather than the exact contents.
					try {
						var n = 123;
						n.keyArray();
						fail( "expected exception" );
					}
					catch ( any e ) {
						expect( e.type ).toBe( "expression" );
						expect( e.message ).toMatch( "^The function \[keyArray\] does not exist in the Numeric\. Available functions are \[.+\]\.$" );
					}
				});

				it( "invalid sort type keeps the 'invalid sort type [...]' message", function() {
					try {
						var arr = [ 3, 1, 2 ];
						arr.sort( "not_a_valid_sort_type" );
						fail( "expected exception" );
					}
					catch ( any e ) {
						expect( e.type ).toBe( "expression" );
						expect( e.message ).toBe( "invalid sort type [not_a_valid_sort_type], sort types are [text, textNoCase, numeric]" );
					}
				});

				it( "wrong arg type to struct.keyExists keeps the 'Can't cast Complex Object Type [Array] to String' message", function() {
					try {
						var s = { foo = 1, bar = 2 };
						s.keyExists( [ 1, 2, 3 ] );
						fail( "expected exception" );
					}
					catch ( any e ) {
						expect( e.type ).toBe( "expression" );
						expect( e.message ).toBe( "Can't cast Complex Object Type [Array] to String" );
					}
				});

				it( "evaluate(valueList(q.missing_col)) keeps the 'Column [MISSING_COL] not found' message", function() {
					try {
						var q = queryNew( "name", "varchar", [ [ "alice" ], [ "bob" ] ] );
						evaluate( "valueList(q.missing_col)" );
						fail( "expected exception" );
					}
					catch ( any e ) {
						expect( e.type ).toBe( "database" );
						expect( e.message ).toBe( "Column [MISSING_COL] not found in query, Columns are [NAME]" );
					}
				});

				it( "member call on null receiver keeps the 'variable [NULLVAR] doesn't exist' message", function() {
					try {
						var nullVar = javacast( "null", "" );
						nullVar.len();
						fail( "expected exception" );
					}
					catch ( any e ) {
						expect( e.type ).toBe( "expression" );
						expect( e.message ).toBe( "variable [NULLVAR] doesn't exist" );
					}
				});
			});
		});
	}
}
