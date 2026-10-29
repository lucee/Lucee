component extends="org.lucee.cfml.test.LuceeTestCase" labels="struct" {

	function run( testResults, testBox ) {

		describe( title="LDEV-6793 structNew( type=""observable"", onChange=fn )", body=function() {

			it( title="calls onChange for a new key with the key, null as old value, the new value and the struct", body=function( currentSpec ) {
				var log = [];
				var sct = structNew( type="observable", onChange=function( key, oldValue, newValue, struct ) {
					arrayAppend( log, {
						key: arguments.key,
						oldIsNull: isNull( arguments.oldValue ),
						newValue: arguments.newValue,
						sameStruct: arguments.struct.count() == 1 && arguments.struct.keyExists( arguments.key )
					} );
				} );
				sct.a = 1;
				expect( sct.a ).toBe( 1 );
				expect( log ).toHaveLength( 1 );
				expect( log[ 1 ].key ).toBe( "A" );
				expect( log[ 1 ].oldIsNull ).toBeTrue();
				expect( log[ 1 ].newValue ).toBe( 1 );
				expect( log[ 1 ].sameStruct ).toBeTrue( "the struct passed in must already contain the change" );
			});

			it( title="calls onChange for an update with the old and the new value", body=function( currentSpec ) {
				var log = [];
				var sct = structNew( type="observable", onChange=function( key, oldValue, newValue, struct ) {
					arrayAppend( log, [ arguments.key, arguments.oldValue, arguments.newValue ] );
				} );
				sct.a = 1;
				sct.a = 2;
				sct[ "a" ] = 3;
				structInsert( sct, "b", "x" );
				structUpdate( sct, "b", "y" );
				expect( log ).toHaveLength( 5 );
				expect( log[ 2 ][ 2 ] ).toBe( 1 );
				expect( log[ 2 ][ 3 ] ).toBe( 2 );
				expect( log[ 3 ][ 2 ] ).toBe( 2 );
				expect( log[ 3 ][ 3 ] ).toBe( 3 );
				expect( log[ 4 ][ 1 ] ).toBe( "B" );
				expect( log[ 5 ][ 2 ] ).toBe( "x" );
				expect( log[ 5 ][ 3 ] ).toBe( "y" );
			});

			it( title="calls onChange when a key is set to the same value", body=function( currentSpec ) {
				var count = 0;
				var sct = structNew( type="observable", onChange=function() { count++; } );
				sct.a = 1;
				sct.a = 1;
				expect( count ).toBe( 2 );
			});

			it( title="calls onChange for a removed key with null as new value", body=function( currentSpec ) {
				var log = [];
				var sct = structNew( type="observable", onChange=function( key, oldValue, newValue, struct ) {
					arrayAppend( log, {
						key: arguments.key,
						oldValue: arguments.oldValue,
						newIsNull: isNull( arguments.newValue ),
						stillThere: arguments.struct.keyExists( arguments.key )
					} );
				} );
				sct.a = 1;
				sct.b = 2;
				log = [];
				structDelete( sct, "a" );
				sct.delete( "b" );
				expect( log ).toHaveLength( 2 );
				expect( log[ 1 ].key ).toBe( "A" );
				expect( log[ 1 ].oldValue ).toBe( 1 );
				expect( log[ 1 ].newIsNull ).toBeTrue();
				expect( log[ 1 ].stillThere ).toBeFalse( "the change must already be applied" );
				expect( log[ 2 ].key ).toBe( "B" );
				expect( log[ 2 ].oldValue ).toBe( 2 );
			});

			it( title="does not call onChange when removing a key that does not exist", body=function( currentSpec ) {
				var count = 0;
				var sct = structNew( type="observable", onChange=function() { count++; } );
				expect( structDelete( sct, "nope", true ) ).toBeFalse();
				expect( structDelete( sct, "nope" ) ).toBeTrue();
				expect( count ).toBe( 0 );
			});

			it( title="calls onChange once for every removed key on structClear", body=function( currentSpec ) {
				var removed = [];
				var sct = structNew( type="observable", onChange=function( key, oldValue, newValue, struct ) {
					arrayAppend( removed, arguments.key & "=" & arguments.oldValue );
				} );
				sct.a = 1;
				sct.b = 2;
				removed = [];
				structClear( sct );
				expect( sct ).toBeEmpty();
				arraySort( removed, "text" );
				expect( removed ).toBe( [ "A=1", "B=2" ] );
			});

			it( title="calls onChange for structAppend", body=function( currentSpec ) {
				var keys = [];
				var sct = structNew( type="observable", onChange=function( key ) { arrayAppend( keys, arguments.key ); } );
				structAppend( sct, { x: 1, y: 2 } );
				arraySort( keys, "text" );
				expect( keys ).toBe( [ "X", "Y" ] );
			});

			it( title="passes the arguments positionally to a closure with other argument names", body=function( currentSpec ) {
				var log = [];
				var sct = structNew( type="observable", onChange=function( k, o, n, s ) {
					arrayAppend( log, k & ":" & ( isNull( o ) ? "null" : o ) & ":" & n );
				} );
				sct.a = 1;
				sct.a = 2;
				expect( log ).toBe( [ "A:null:1", "A:1:2" ] );
			});

			it( title="reports the old and new value as null with the elvis operator", body=function( currentSpec ) {
				var log = [];
				var sct = structNew( type="observable", onChange=function( key, oldValue, newValue, struct ) {
					log.append( key & ": " & ( oldValue ?: "null" ) & " -> " & ( newValue ?: "null" ) );
				} );
				sct.name = "Lucee";
				sct.name = "Gian";
				sct.delete( "name" );
				expect( log ).toBe( [ "NAME: null -> Lucee", "NAME: Lucee -> Gian", "NAME: Gian -> null" ] );
			});

			it( title="accepts the type in any case", body=function( currentSpec ) {
				var count = 0;
				var sct = structNew( type="Observable", onChange=function() { count++; } );
				sct.a = 1;
				expect( count ).toBe( 1 );
			});

			it( title="behaves like a regular struct otherwise", body=function( currentSpec ) {
				var sct = structNew( type="observable", onChange=function() {} );
				sct.a = 1;
				sct.b = [ 1, 2 ];
				expect( isStruct( sct ) ).toBeTrue();
				expect( sct.count() ).toBe( 2 );
				expect( sct.keyList() ).toInclude( "a" );
				expect( serializeJSON( sct ) ).toInclude( """A"":1" );
			});

			it( title="does not report changes inside nested structs and arrays", body=function( currentSpec ) {
				var count = 0;
				var sct = structNew( type="observable", onChange=function() { count++; } );
				sct.inner = { a: 1 };
				sct.list = [ 1 ];
				expect( count ).toBe( 2 );
				sct.inner.a = 2;
				sct.list.append( 2 );
				expect( count ).toBe( 2 );
			});

			it( title="a duplicate is a regular struct without the listener", body=function( currentSpec ) {
				var count = 0;
				var sct = structNew( type="observable", onChange=function() { count++; } );
				sct.a = 1;
				var copy = duplicate( sct );
				copy.b = 2;
				expect( count ).toBe( 1 );
				expect( copy.count() ).toBe( 2 );
			});

			it( title="does not swallow an exception thrown by onChange", body=function( currentSpec ) {
				var sct = structNew( type="observable", onChange=function() { throw( message="onChange failed", type="MyOnChangeError" ); } );
				expect( function() { sct.a = 1; } ).toThrow( "MyOnChangeError", "onChange failed" );
				expect( function() { structInsert( sct, "b", 1 ); } ).toThrow( "MyOnChangeError", "onChange failed" );
				expect( function() { structDelete( sct, "a" ); } ).toThrow( "MyOnChangeError", "onChange failed" );
				expect( function() { structClear( sct ); } ).toThrow( "MyOnChangeError", "onChange failed" );
			});

			it( title="keeps the change when onChange throws", body=function( currentSpec ) {
				var sct = structNew( type="observable", onChange=function() { throw( message="onChange failed", type="MyOnChangeError" ); } );
				try { sct.a = 1; } catch ( MyOnChangeError e ) {}
				expect( sct.keyExists( "a" ) ).toBeTrue();
			});

			it( title="does not lose changes from concurrent threads", body=function( currentSpec ) {
				var count = createObject( "java", "java.util.concurrent.atomic.AtomicInteger" ).init( 0 );
				var sct = structNew( type="observable", onChange=function() { count.incrementAndGet(); } );
				var names = [];
				loop from=1 to=4 index="local.t" {
					arrayAppend( names, "ldev6793_#t#" );
					thread name="ldev6793_#t#" sct=sct t=t {
						loop from=1 to=100 index="local.i" {
							attributes.sct[ "k#attributes.t#_#i#" ] = i;
						}
					}
				}
				thread action="join" name=names.toList();
				expect( sct.count() ).toBe( 400 );
				expect( count.get() ).toBe( 400 );
			});

			describe( "invalid arguments", function() {

				it( title="type observable without onChange throws", body=function( currentSpec ) {
					expect( function() { structNew( "observable" ); } ).toThrow( "application", "onChange" );
				});

				it( title="onChange with another type throws", body=function( currentSpec ) {
					expect( function() { structNew( type="ordered", onChange=function() {} ); } ).toThrow( "application", "onChange" );
					expect( function() { structNew( onChange=function() {} ); } ).toThrow( "application", "onChange" );
				});

				it( title="onChange in combination with onMissingKey throws", body=function( currentSpec ) {
					expect( function() {
						structNew( type="observable", onMissingKey=function() {}, onChange=function() {} );
					} ).toThrow( "application", "onMissingKey" );
				});

				it( title="onChange must be a function", body=function( currentSpec ) {
					expect( function() { structNew( type="observable", onChange="notAFunction" ); } ).toThrow();
				});

			});

		});
	}

}
