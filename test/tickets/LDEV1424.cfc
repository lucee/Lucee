component extends="org.lucee.cfml.test.LuceeTestCase" labels="for" {

	function run( testResults, testBox ) {
		describe( "LDEV-1424 for ( value, index in collection )", function() {

			it( title="array: value and index (1 based)", body=function( currentSpec ) {
				var res = [];
				for ( var v, i in [ "cat", "dog", "bird" ] ) {
					arrayAppend( res, i & "=" & v );
				}
				expect( arrayToList( res ) ).toBe( "1=cat,2=dog,3=bird" );
			});

			it( title="array: the index is a number", body=function( currentSpec ) {
				var types = "";
				for ( var v, i in [ "a" ] ) {
					types = isNumeric( i ) & "|" & ( i == 1 );
				}
				expect( types ).toBe( "true|true" );
			});

			it( title="empty array does not execute the body", body=function( currentSpec ) {
				var n = 0;
				for ( var v, i in [] ) n++;
				expect( n ).toBe( 0 );
			});

			it( title="struct: value and key", body=function( currentSpec ) {
				var sct = [ "one": 1, "two": 2, "three": 3 ];
				var res = [];
				for ( var v, k in sct ) {
					arrayAppend( res, uCase( k ) & "=" & v );
				}
				expect( arrayToList( res ) ).toBe( "ONE=1,TWO=2,THREE=3" );
			});

			it( title="struct: values can be complex", body=function( currentSpec ) {
				var sct = { "a": { x: 1 }, "b": [ 1, 2 ] };
				var res = {};
				for ( var v, k in sct ) res[ k ] = isStruct( v ) ? "struct" : "array";
				expect( res.a ).toBe( "struct" );
				expect( res.b ).toBe( "array" );
			});

			it( title="query: row struct and row number", body=function( currentSpec ) {
				var qry = queryNew( "id,name", "integer,varchar", [ [ 1, "a" ], [ 2, "b" ], [ 3, "c" ] ] );
				var res = [];
				for ( var row, rowNr in qry ) {
					arrayAppend( res, rowNr & ":" & row.id & row.name );
				}
				expect( arrayToList( res ) ).toBe( "1:1a,2:2b,3:3c" );
			});

			it( title="list (string): item and position", body=function( currentSpec ) {
				var res = [];
				for ( var v, i in "x,y,z" ) arrayAppend( res, i & v );
				expect( arrayToList( res ) ).toBe( "1x,2y,3z" );
			});

			it( title="nested loops each have their own index", body=function( currentSpec ) {
				var res = [];
				for ( var outer, o in [ "a", "b" ] ) {
					for ( var inner, i in [ "x", "y", "z" ] ) {
						arrayAppend( res, o & outer & i & inner );
					}
				}
				expect( arrayToList( res ) ).toBe( "1a1x,1a2y,1a3z,2b1x,2b2y,2b3z" );
			});

			it( title="break", body=function( currentSpec ) {
				var res = [];
				for ( var v, i in [ "a", "b", "c", "d" ] ) {
					if ( i == 3 ) break;
					arrayAppend( res, v );
				}
				expect( arrayToList( res ) ).toBe( "a,b" );
			});

			it( title="continue", body=function( currentSpec ) {
				var res = [];
				for ( var v, i in [ "a", "b", "c", "d" ] ) {
					if ( i mod 2 == 0 ) continue;
					arrayAppend( res, v & i );
				}
				expect( arrayToList( res ) ).toBe( "a1,c3" );
			});

			it( title="break and continue in nested loops", body=function( currentSpec ) {
				var res = [];
				for ( var a, i in [ 1, 2, 3 ] ) {
					for ( var b, j in [ 1, 2, 3 ] ) {
						if ( j == 2 ) continue;
						if ( i == 2 ) break;
						arrayAppend( res, a & b );
					}
				}
				expect( arrayToList( res ) ).toBe( "11,13,31,33" );
			});

			it( title="return from inside the loop", body=function( currentSpec ) {
				expect( _find( [ "a", "b", "c" ], "b" ) ).toBe( 2 );
				expect( _find( [ "a", "b", "c" ], "x" ) ).toBe( 0 );
			});

			it( title="with var both variables are local", body=function( currentSpec ) {
				var r = _scoping();
				expect( r.local ).toBe( true );
				expect( r.variables ).toBe( false );
			});

			it( title="var on both variables", body=function( currentSpec ) {
				var r = _scopingBoth();
				expect( r.local ).toBe( true );
				expect( r.variables ).toBe( false );
			});

			it( title="var on the value also declares the index as local", body=function( currentSpec ) {
				var res = _noVar();
				expect( res ).toBe( "b2" );
			});

			it( title="local scope prefix", body=function( currentSpec ) {
				var res = [];
				for ( local.v, local.i in [ "p", "q" ] ) arrayAppend( res, local.i & local.v );
				expect( arrayToList( res ) ).toBe( "1p,2q" );
			});

			it( title="the variables keep the last value after the loop", body=function( currentSpec ) {
				for ( v, i in [ "p", "q" ] ) {}
				expect( v ).toBe( "q" );
				expect( i ).toBe( 2 );
			});

			it( title="loop with a closure using the index", body=function( currentSpec ) {
				var fns = [];
				for ( var v, i in [ "a", "b" ] ) {
					var x = i;
					arrayAppend( fns, function() { return 1; } );
				}
				expect( arrayLen( fns ) ).toBe( 2 );
			});

			it( title="the single variable form is unchanged", body=function( currentSpec ) {
				var res = [];
				for ( var v in [ "a", "b" ] ) arrayAppend( res, v );
				expect( arrayToList( res ) ).toBe( "a,b" );

				var keys = [];
				for ( var k in [ "a": 1, "b": 2 ] ) arrayAppend( keys, uCase( k ) );
				expect( arrayToList( keys ) ).toBe( "A,B" );

				var rows = 0;
				for ( var row in queryNew( "id", "integer", [ [ 1 ], [ 2 ] ] ) ) rows += row.id;
				expect( rows ).toBe( 3 );
			});

			it( title="label with the new form", body=function( currentSpec ) {
				var res = [];
				outer: for ( var a, i in [ 1, 2, 3 ] ) {
					for ( var b, j in [ 1, 2, 3 ] ) {
						if ( j == 2 ) continue outer;
						arrayAppend( res, a & b );
					}
				}
				expect( arrayToList( res ) ).toBe( "11,21,31" );
			});

			it( title="three variables are a syntax error", body=function( currentSpec ) {
				expect( function() {
					new LDEV1424.Bad();
				}).toThrow();
			});
		});
	}

	private numeric function _find( required array arr, required string needle ) {
		for ( var v, i in arguments.arr ) {
			if ( v == arguments.needle ) return i;
		}
		return 0;
	}

	private struct function _scoping() {
		for ( var v, i in [ "a" ] ) {}
		return { local: structKeyExists( local, "i" ) && structKeyExists( local, "v" ), variables: structKeyExists( variables, "i" ) || structKeyExists( variables, "v" ) };
	}

	private struct function _scopingBoth() {
		for ( var v, var i in [ "a" ] ) {}
		return { local: structKeyExists( local, "i" ) && structKeyExists( local, "v" ), variables: structKeyExists( variables, "i" ) || structKeyExists( variables, "v" ) };
	}

	private string function _noVar() {
		for ( var v, i in [ "a", "b" ] ) {}
		return v & i;
	}

}
