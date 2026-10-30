component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax" {

	function beforeAll() {
		variables.uri = createURI( "LDEV2201" );
	}

	// returns the message of the error thrown by the template
	private function run_( required string file ) {
		try {
			return { fileContent: _InternalRequest( template: "#variables.uri#/#arguments.file#" ).fileContent };
		}
		catch ( any e ) {
			return { fileContent: e.message };
		}
	}

	function run( testResults, testBox ) {

		describe( "LDEV-2201 spread operator", function() {

			describe( "array literal", function() {

				it( "spreads an array into the array", function() {
					var a = [ 1, 2, 3 ];
					var b = [ 0, ...a, 4 ];
					expect( b ).toBe( [ 0, 1, 2, 3, 4 ] );
				});

				it( "spreads several arrays", function() {
					var a = [ 1, 2 ];
					var b = [ 3, 4 ];
					expect( [ ...a, ...b ] ).toBe( [ 1, 2, 3, 4 ] );
				});

				it( "a spread of an empty array adds nothing", function() {
					var a = [];
					expect( [ ...a ] ).toBe( [] );
					expect( [ 1, ...a, 2 ] ).toBe( [ 1, 2 ] );
				});

				it( "spreads a string into its characters", function() {
					expect( [ ..."12345", "foo" ] ).toBe( [ "1", "2", "3", "4", "5", "foo" ] );
				});

				it( "spread creates a copy, the source is not changed", function() {
					var a = [ 1, 2 ];
					var b = [ ...a ];
					b.append( 3 );
					expect( a ).toBe( [ 1, 2 ] );
					expect( b ).toBe( [ 1, 2, 3 ] );
				});

				it( "spreads the result of an expression", function() {
					expect( [ ...[ 1, 2 ].map( function( v ) { return v * 10; } ), 3 ] ).toBe( [ 10, 20, 3 ] );
				});

				it( "keeps nested arrays as single elements", function() {
					var a = [ [ 1, 2 ], [ 3 ] ];
					var b = [ ...a ];
					expect( b ).toHaveLength( 2 );
					expect( b[ 1 ] ).toBe( [ 1, 2 ] );
				});

			});

			describe( "struct literal", function() {

				it( "merges a struct into the struct", function() {
					var a = { a: 1, b: 2 };
					var s = { ...a, c: 3 };
					expect( s ).toBe( { a: 1, b: 2, c: 3 } );
				});

				it( "later keys win over earlier keys", function() {
					var a = { a: 1, b: 2 };
					var b = { b: 20, c: 30 };
					expect( { ...a, ...b } ).toBe( { a: 1, b: 20, c: 30 } );
					expect( { ...a, b: 99 } ).toBe( { a: 1, b: 99 } );
					expect( { b: 99, ...a } ).toBe( { a: 1, b: 2 } );
				});

				it( "spread creates a shallow copy, the source is not changed", function() {
					var a = { a: 1, inner: { x: 1 } };
					var b = { ...a };
					b.a = 2;
					expect( a.a ).toBe( 1 );
					// shallow, the nested struct is shared
					b.inner.x = 5;
					expect( a.inner.x ).toBe( 5 );
				});

				it( "works with an empty struct", function() {
					var a = {};
					expect( { ...a } ).toBe( {} );
					expect( { x: 1, ...a } ).toBe( { x: 1 } );
				});

				it( "works with the shorthand notation and ordered structs", function() {
					var a = 1;
					var b = { b: 2 };
					expect( { a, ...b } ).toBe( { a: 1, b: 2 } );
					var o = [ z: 1, ...b ];
					expect( isStruct( o ) ).toBeTrue();
					expect( structKeyList( o ) ).toBe( "z,b" );
				});

			});

			describe( "function call", function() {

				it( "spreads an array into positional arguments", function() {
					var f = function( a, b, c ) { return a & b & c; };
					expect( f( ...[ 1, 2, 3 ] ) ).toBe( "123" );
				});

				it( "mixes plain arguments and spread arguments", function() {
					var f = function( a, b, c, d ) { return a & b & c & d; };
					var mid = [ 2, 3 ];
					expect( f( 1, ...mid, 4 ) ).toBe( "1234" );
				});

				it( "spreads several arrays", function() {
					var f = function( a, b, c, d ) { return a & b & c & d; };
					expect( f( ...[ 1, 2 ], ...[ 3, 4 ] ) ).toBe( "1234" );
				});

				it( "works with a user defined function and arguments scope", function() {
					expect( variadic( ...[ 1, 2, 3 ] ) ).toBe( 3 );
					expect( variadic( 0, ...[ 1, 2, 3 ] ) ).toBe( 4 );
					expect( variadic( ...[] ) ).toBe( 0 );
				});

				it( "works for member functions of a component", function() {
					var cfc = new LDEV2201.Calc();
					var nums = [ 1, 2, 3, 4 ];
					expect( cfc.add( ...nums ) ).toBe( 10 );
				});

				it( "a spread struct is not supported, argumentCollection is used for named arguments", function() {
					var res = run_( "err-struct-in-call.cfm" );
					expect( res.fileContent ).toInclude( "argumentCollection" );
				});

			});

			describe( "errors", function() {

				it( "struct inside an array literal", function() {
					expect( run_( "err-struct-in-array.cfm" ).fileContent ).toInclude( "struct" );
				});

				it( "array inside a struct literal", function() {
					expect( run_( "err-array-in-struct.cfm" ).fileContent ).toInclude( "struct" );
				});

				it( "named arguments combined with a spread", function() {
					expect( run_( "err-named-and-spread.cfm" ).fileContent ).toInclude( "named arguments" );
				});

				it( "built-in functions", function() {
					expect( run_( "err-builtin.cfm" ).fileContent ).toInclude( "built-in" );
				});

				it( "null value", function() {
					expect( run_( "err-null.cfm" ).fileContent ).toInclude( "null" );
				});

			});

			it( "spread in a call works together with a rest parameter (LDEV-6786)", function() {
				function collect( a, ...rest ) {
					return rest;
				}
				var args = [ 2, 3, 4 ];
				expect( collect( 1, ...args ) ).toBe( [ 2, 3, 4 ] );
				expect( collect( ...args ) ).toBe( [ 3, 4 ] );
				expect( collect( 0, ...args, 5 ) ).toBe( [ 2, 3, 4, 5 ] );
				// a rest parameter function can forward its own rest array with a spread
				function forward( ...all ) {
					return collect( ...all );
				}
				expect( forward( 1, 2, 3 ) ).toBe( [ 2, 3 ] );
				expect( [ ...collect( 1, 2, 3 ), ...collect( 1, 4 ) ] ).toBe( [ 2, 3, 4 ] );
			});

			it( "a spread file compiles and runs", function() {
				expect( run_( "ok-parse.cfm" ).fileContent.trim() ).toBe( "1,2,3" );
			});

		});
	}

	private function variadic() {
		return arrayLen( arguments );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}
