component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax" {

	function beforeAll() {
		variables.uri = createURI( "LDEV6786" );
	}

	// returns the message of the error thrown by the template
	private string function run_( required string file ) {
		try {
			return _InternalRequest( template: "#variables.uri#/#arguments.file#" ).fileContent;
		}
		catch ( any e ) {
			return e.message;
		}
	}

	function run( testResults, testBox ) {

		describe( "LDEV-6786 rest parameters", function() {

			it( "collects the extra arguments into an array", function() {
				function f( a, b, ...rest ) {
					return rest;
				}
				expect( f( 1, 2, 3, 4, 5 ) ).toBe( [ 3, 4, 5 ] );
				expect( f( 1, 2, 3 ) ).toBe( [ 3 ] );
			});

			it( "is an empty array if there are no extra arguments", function() {
				function g( a, ...rest ) {
					return rest;
				}
				expect( g( 1 ) ).toBe( [] );
				expect( g() ).toBe( [] );
			});

			it( "as the only parameter it gathers all arguments", function() {
				function sum( ...numbers ) {
					var total = 0;
					for ( var n in numbers ) total += n;
					return total;
				}
				expect( sum( 1, 2, 3, 4 ) ).toBe( 10 );
				expect( sum() ).toBe( 0 );
			});

			it( "the normal parameters keep working, including defaults and types", function() {
				function h( string a, numeric b = 5, ...rest ) {
					return a & ":" & b & ":" & arrayToList( rest );
				}
				expect( h( "x" ) ).toBe( "x:5:" );
				expect( h( "x", 1 ) ).toBe( "x:1:" );
				expect( h( "x", 1, 2, 3 ) ).toBe( "x:1:2,3" );
			});

			it( "is available in the arguments scope", function() {
				function k( a, ...rest ) {
					return arguments.rest;
				}
				expect( k( 1, 2, 3 ) ).toBe( [ 2, 3 ] );
			});

			it( "keeps the order, nested values and null free values", function() {
				function m( ...items ) {
					return items;
				}
				var res = m( "a", [ 1 ], { x: 1 }, 4 );
				expect( res ).toHaveLength( 4 );
				expect( res[ 1 ] ).toBe( "a" );
				expect( res[ 2 ] ).toBe( [ 1 ] );
				expect( res[ 3 ] ).toBe( { x: 1 } );
				expect( res[ 4 ] ).toBe( 4 );
			});

			it( "works for closures", function() {
				var c = function( a, ...rest ) {
					return rest;
				};
				expect( c( 1, 2, 3 ) ).toBe( [ 2, 3 ] );
			});

			it( "works for lambdas", function() {
				var l = ( a, ...rest ) => rest;
				expect( l( 1, 2, 3 ) ).toBe( [ 2, 3 ] );
				var l2 = ( ...all ) => arrayLen( all );
				expect( l2( 1, 2, 3, 4 ) ).toBe( 4 );
			});

			it( "works for component functions", function() {
				var cfc = new LDEV6786.Variadic();
				expect( cfc.join( "-", "a", "b", "c" ) ).toBe( "a-b-c" );
				expect( cfc.join( "-" ) ).toBe( "" );
				expect( cfc.count( 1, 2, 3 ) ).toBe( 3 );
			});

			it( "works with argumentCollection", function() {
				function ac( a, ...rest ) {
					return rest;
				}
				// positional
				expect( ac( argumentCollection = [ 1, 2, 3 ] ) ).toBe( [ 2, 3 ] );
				// named, the rest parameter is empty if it is not passed
				expect( ac( argumentCollection = { a: 1 } ) ).toBe( [] );
				expect( ac( a = 1 ) ).toBe( [] );
				// named, the rest parameter passed by name
				expect( ac( a = 1, rest = [ 7, 8 ] ) ).toBe( [ 7, 8 ] );
			});

			it( "existing functions without rest parameter are not changed", function() {
				function plain( a, b ) {
					return arrayLen( arguments );
				}
				expect( plain( 1, 2, 3, 4 ) ).toBe( 4 );
			});

			it( "works for callbacks that pass more arguments than needed", function() {
				// arrayMap passes value, index and the array
				var res = arrayMap( [ 10, 20 ], function( v, ...rest ) {
					return arrayLen( rest );
				});
				expect( res ).toBe( [ 2, 2 ] );
			});

			describe( "errors", function() {

				it( "the rest parameter must be the last parameter", function() {
					expect( run_( "err-not-last.cfm" ) ).toInclude( "last parameter" );
				});

				it( "the rest parameter needs a name", function() {
					expect( run_( "err-no-name.cfm" ) ).toInclude( "name" );
				});

				it( "only one rest parameter is allowed", function() {
					expect( run_( "err-twice.cfm" ) ).toInclude( "last parameter" );
				});

			});

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}
