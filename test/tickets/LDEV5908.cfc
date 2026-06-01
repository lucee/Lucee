component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.originalNS = getApplicationSettings().nullSupport;
	}

	function afterAll() {
		application action="update" NULLSupport=variables.originalNS;
	}

	function run( testResults, testBox ) {

		describe( "ConcurrentHashMapNullSupport - Basic null operations", function() {

			it( "can put and get null values", function() {
				withNullSupport( false, function() {
					var sct = {};
					sct.key = nullValue();
					// Without FNS, key with null value doesn't "exist" in CFML terms
					expect( structKeyExists( sct, "key" ) ).toBeFalse();
				});
			});

			it( "can put and get null values - FNS", function() {
				withNullSupport( true, function() {
					var sct = {};
					sct.key = nullValue();
					expect( structKeyExists( sct, "key" ) ).toBeTrue();
					// With FNS, isNull properly detects null
					expect( isNull( sct.key ) ).toBeTrue();
				});
			});

			it( "structKeyExists returns false for keys with null values", function() {
				withNullSupport( false, function() {
					var sct = { a: "value", b: nullValue() };
					expect( structKeyExists( sct, "a" ) ).toBeTrue();
					// Without FNS, null value means key doesn't exist
					expect( structKeyExists( sct, "b" ) ).toBeFalse();
					expect( structKeyExists( sct, "missing" ) ).toBeFalse();
				});
			});

			it( "structKeyExists returns true for keys with null values - FNS", function() {
				withNullSupport( true, function() {
					var sct = { a: "value", b: nullValue() };
					expect( structKeyExists( sct, "a" ) ).toBeTrue();
					expect( structKeyExists( sct, "b" ) ).toBeTrue();
					expect( structKeyExists( sct, "missing" ) ).toBeFalse();
				});
			});

			it( "can store and retrieve multiple null values", function() {
				withNullSupport( false, function() {
					var sct = {};
					sct.null1 = nullValue();
					sct.null2 = nullValue();
					sct.value = "test";

					// Without FNS, keys with null values don't exist
					expect( structKeyExists( sct, "null1" ) ).toBeFalse();
					expect( structKeyExists( sct, "null2" ) ).toBeFalse();
					expect( structKeyExists( sct, "value" ) ).toBeTrue();
					expect( sct.value ).toBe( "test" );
				});
			});

			it( "can store and retrieve multiple null values - FNS", function() {
				withNullSupport( true, function() {
					var sct = {};
					sct.null1 = nullValue();
					sct.null2 = nullValue();
					sct.value = "test";

					expect( structKeyExists( sct, "null1" ) ).toBeTrue();
					expect( structKeyExists( sct, "null2" ) ).toBeTrue();
					expect( isNull( sct.null1 ) ).toBeTrue();
					expect( isNull( sct.null2 ) ).toBeTrue();
					expect( sct.value ).toBe( "test" );
				});
			});

			it( "can remove keys with null values", function() {
				withNullSupport( false, function() {
					var sct = { key: nullValue() };
					// Without FNS, key with null doesn't exist
					expect( structKeyExists( sct, "key" ) ).toBeFalse();

					// But can still delete it
					structDelete( sct, "key" );
					expect( structKeyExists( sct, "key" ) ).toBeFalse();
				});
			});

			it( "can remove keys with null values - FNS", function() {
				withNullSupport( true, function() {
					var sct = { key: nullValue() };
					expect( structKeyExists( sct, "key" ) ).toBeTrue();

					structDelete( sct, "key" );
					expect( structKeyExists( sct, "key" ) ).toBeFalse();
				});
			});
		});

		describe( "ConcurrentHashMapNullSupport - Collection views and iteration", function() {

			it( "structEach iterates over entries including null values", function() {
				withNullSupport( false, function() {
					var sct = { a: "value", b: nullValue(), c: "another" };
					var keys = [];
					var foundNull = false;

					structEach( sct, function( key, value ) {
						arrayAppend( keys, arguments.key );
						// With explicit arguments scope, isNull works correctly
						if ( isNull( arguments.value ) ) {
							foundNull = true;
						}
					});

					expect( arrayLen( keys ) ).toBe( 3 );
					expect( foundNull ).toBeTrue();
				});
			});

			it( "structEach iterates over entries including null values - FNS", function() {
				withNullSupport( true, function() {
					var sct = { a: "value", b: nullValue(), c: "another" };
					var keys = [];
					var foundNull = false;

					structEach( sct, function( key, value ) {
						arrayAppend( keys, arguments.key );
						if ( isNull( arguments.value ) ) {
							foundNull = true;
						}
					});

					expect( arrayLen( keys ) ).toBe( 3 );
					expect( foundNull ).toBeTrue();
				});
			});

			it( "structMap handles null values", function() {
				withNullSupport( false, function() {
					var sct = { a: 1, b: nullValue(), c: 3 };
					var result = structMap( sct, function( key, value ) {
						return isNull( arguments.value ) ? 0 : arguments.value * 2;
					});

					expect( result.a ).toBe( 2 );
					expect( result.b ).toBe( 0 );
					expect( result.c ).toBe( 6 );
				});
			});

			it( "structMap handles null values - FNS", function() {
				withNullSupport( true, function() {
					var sct = { a: 1, b: nullValue(), c: 3 };
					var result = structMap( sct, function( key, value ) {
						return isNull( arguments.value ) ? 0 : arguments.value * 2;
					});

					expect( result.a ).toBe( 2 );
					expect( result.b ).toBe( 0 );
					expect( result.c ).toBe( 6 );
				});
			});

			it( "for-in loop iterates over keys with null values", function() {
				withNullSupport( false, function() {
					var sct = { a: "value", b: nullValue() };
					var count = 0;
					var foundBKey = false;

					for ( var key in sct ) {
						count++;
						// Check if we find the "b" key (without accessing value)
						if ( key == "b" ) {
							foundBKey = true;
						}
					}

					expect( count ).toBe( 2 );
					// The key IS there, just structKeyExists returns false
					expect( foundBKey ).toBeTrue();
				});
			});

			it( "for-in loop iterates over keys with null values - FNS", function() {
				withNullSupport( true, function() {
					var sct = { a: "value", b: nullValue() };
					var count = 0;
					var foundNullKey = false;

					for ( var key in sct ) {
						count++;
						if ( key == "b" && isNull( sct[ key ] ) ) {
							foundNullKey = true;
						}
					}

					expect( count ).toBe( 2 );
					expect( foundNullKey ).toBeTrue();
				});
			});

			it( "structKeyArray includes keys with null values", function() {
				withNullSupport( false, function() {
					var sct = { a: "value", b: nullValue(), c: "another" };
					var keys = structKeyArray( sct );

					expect( arrayLen( keys ) ).toBe( 3 );
					// Check if "b" is in the array manually
					var foundB = false;
					for ( var k in keys ) {
						if ( k == "b" ) {
							foundB = true;
						}
					}
					expect( foundB ).toBeTrue();
				});
			});

			it( "structKeyArray includes keys with null values - FNS", function() {
				withNullSupport( true, function() {
					var sct = { a: "value", b: nullValue(), c: "another" };
					var keys = structKeyArray( sct );

					expect( arrayLen( keys ) ).toBe( 3 );
					// Check if "b" is in the array manually
					var foundB = false;
					for ( var k in keys ) {
						if ( k == "b" ) {
							foundB = true;
						}
					}
					expect( foundB ).toBeTrue();
				});
			});
		});

		describe( "ConcurrentHashMapNullSupport - Functional methods", function() {

			it( "elvis operator with null value vs missing key", function() {
				withNullSupport( false, function() {
					var sct = { nullKey: nullValue() };

					// Missing key uses default
					var result1 = sct.missing ?: "default";
					expect( result1 ).toBe( "default" );

					// Null value also treated as empty with elvis
					var result2 = sct.nullKey ?: "default";
					expect( result2 ).toBe( "default" );
				});
			});

			it( "elvis operator with null value vs missing key - FNS", function() {
				withNullSupport( true, function() {
					var sct = { nullKey: nullValue() };

					// Missing key uses default
					var result1 = sct.missing ?: "default";
					expect( result1 ).toBe( "default" );

					// Null value also treated as empty with elvis
					var result2 = sct.nullKey ?: "default";
					expect( result2 ).toBe( "default" );
				});
			});

			it( "structAppend handles null values", function() {
				withNullSupport( false, function() {
					var sct1 = { a: "value1" };
					var sct2 = { b: nullValue(), c: "value2" };

					structAppend( sct1, sct2 );

					// Without FNS, key with null doesn't exist
					expect( structKeyExists( sct1, "b" ) ).toBeFalse();
					expect( structKeyExists( sct1, "c" ) ).toBeTrue();
					expect( sct1.c ).toBe( "value2" );
				});
			});

			it( "structAppend handles null values - FNS", function() {
				withNullSupport( true, function() {
					var sct1 = { a: "value1" };
					var sct2 = { b: nullValue(), c: "value2" };

					structAppend( sct1, sct2 );

					expect( structKeyExists( sct1, "b" ) ).toBeTrue();
					expect( isNull( sct1.b ) ).toBeTrue();
					expect( sct1.c ).toBe( "value2" );
				});
			});

			it( "structCopy preserves null values", function() {
				withNullSupport( false, function() {
					var original = { a: "value", b: nullValue() };
					var copy = structCopy( original );

					expect( structKeyExists( copy, "a" ) ).toBeTrue();
					// Without FNS, key with null doesn't exist
					expect( structKeyExists( copy, "b" ) ).toBeFalse();
					expect( copy.a ).toBe( "value" );
				});
			});

			it( "structCopy preserves null values - FNS", function() {
				withNullSupport( true, function() {
					var original = { a: "value", b: nullValue() };
					var copy = structCopy( original );

					expect( structKeyExists( copy, "a" ) ).toBeTrue();
					expect( structKeyExists( copy, "b" ) ).toBeTrue();
					expect( isNull( copy.b ) ).toBeTrue();
					expect( copy.a ).toBe( "value" );
				});
			});

			it( "structUpdate can update to null value", function() {
				withNullSupport( false, function() {
					var sct = { key: "value" };
					expect( sct.key ).toBe( "value" );

					structUpdate( sct, "key", nullValue() );
					// Without FNS, after setting to null, key doesn't exist
					expect( structKeyExists( sct, "key" ) ).toBeFalse();
				});
			});

			it( "structUpdate can update to null value - FNS", function() {
				withNullSupport( true, function() {
					var sct = { key: "value" };
					expect( sct.key ).toBe( "value" );

					structUpdate( sct, "key", nullValue() );
					expect( structKeyExists( sct, "key" ) ).toBeTrue();
					expect( isNull( sct.key ) ).toBeTrue();
				});
			});
		});

		describe( "ConcurrentHashMapNullSupport - Serialization", function() {

			it( "null values survive objectSave/objectLoad round-trip", function() {
				withNullSupport( true, function() {
					var sct = { key: nullValue(), other: "value" };
					var bytes = objectSave( sct );
					var restored = objectLoad( bytes );
					expect( structKeyExists( restored, "key" ) ).toBeTrue();
					expect( isNull( local.restored[ "key" ] ) ).toBeTrue( "null value should survive serialization round-trip" );
					expect( restored.other ).toBe( "value" );
				});
			});

			it( "non-null values survive objectSave/objectLoad round-trip", function() {
				withNullSupport( true, function() {
					var sct = { a: "hello", b: 42, c: true };
					var bytes = objectSave( sct );
					var restored = objectLoad( bytes );
					expect( restored.a ).toBe( "hello" );
					expect( restored.b ).toBe( 42 );
					expect( restored.c ).toBe( true );
				});
			});

		});

		describe( "ConcurrentHashMapNullSupport - Edge cases and regressions", function() {

			it( "handles comparison with null values", function() {
				withNullSupport( false, function() {
					var sct = { key: nullValue() };

					// Without FNS, key with null doesn't exist
					expect( structKeyExists( sct, "key" ) ).toBeFalse();
				});
			});

			it( "handles comparison with null values - FNS", function() {
				withNullSupport( true, function() {
					var sct = { key: nullValue() };
					var value = sct.key;

					expect( isNull( value ) ).toBeTrue();
					// Comparing null with == in CFML
					expect( value == nullValue() ).toBeTrue();
				});
			});

			it( "LDEV-622: can iterate over Java Map with null values", function() {
				withNullSupport( false, function() {
					// Create a struct that mimics the LDEV-622 scenario
					var sct = { validKey: "value", nullKey: nullValue() };
					var hasError = false;

					try {
						var count = 0;
						for ( var key in sct ) {
							count++;
						}
						expect( count ).toBe( 2 );
					}
					catch ( any e ) {
						hasError = true;
					}

					expect( hasError ).toBeFalse( "Should not throw NPE when iterating" );
				});
			});

			it( "LDEV-622: can iterate over Java Map with null values - FNS", function() {
				withNullSupport( true, function() {
					// Create a struct that mimics the LDEV-622 scenario
					var sct = { validKey: "value", nullKey: nullValue() };
					var hasError = false;

					try {
						var count = 0;
						for ( var key in sct ) {
							count++;
						}
						expect( count ).toBe( 2 );
					}
					catch ( any e ) {
						hasError = true;
					}

					expect( hasError ).toBeFalse( "Should not throw NPE when iterating" );
				});
			});

			it( "structCount includes entries with null values", function() {
				withNullSupport( false, function() {
					var sct = { a: "value", b: nullValue(), c: "another" };
					expect( structCount( sct ) ).toBe( 3 );
				});
			});

			it( "structCount includes entries with null values - FNS", function() {
				withNullSupport( true, function() {
					var sct = { a: "value", b: nullValue(), c: "another" };
					expect( structCount( sct ) ).toBe( 3 );
				});
			});

			it( "structIsEmpty returns false when struct has null values", function() {
				withNullSupport( false, function() {
					var sct = { key: nullValue() };
					expect( structIsEmpty( sct ) ).toBeFalse();
				});
			});

			it( "structIsEmpty returns false when struct has null values - FNS", function() {
				withNullSupport( true, function() {
					var sct = { key: nullValue() };
					expect( structIsEmpty( sct ) ).toBeFalse();
				});
			});

			it( "can clear struct containing null values", function() {
				withNullSupport( false, function() {
					var sct = { a: "value", b: nullValue() };
					structClear( sct );

					expect( structIsEmpty( sct ) ).toBeTrue();
					expect( structCount( sct ) ).toBe( 0 );
				});
			});

			it( "can clear struct containing null values - FNS", function() {
				withNullSupport( true, function() {
					var sct = { a: "value", b: nullValue() };
					structClear( sct );

					expect( structIsEmpty( sct ) ).toBeTrue();
					expect( structCount( sct ) ).toBe( 0 );
				});
			});
		});
	}

	// Private helper function to toggle NullSupport and run test
	private function withNullSupport( boolean enabled, required function testFn ) {
		application action="update" NULLSupport=arguments.enabled;
		arguments.testFn();
	}
}
