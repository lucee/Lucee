component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( title = "LDEV-6349: MethodInstance speculative probes don't allocate NoSuchMethodException", body = function() {

			it( title = "resolves a present get* property via reflection", body = function() {
				var d = createObject( "java", "java.util.Date" ).init( javaCast( "long", 0 ) );
				expect( d.time ).toBe( 0 );
			} );

			it( title = "throws a clean ApplicationException for a missing property (no silent NPE)", body = function() {
				var d = createObject( "java", "java.util.Date" ).init( javaCast( "long", 0 ) );
				try {
					var x = d.nonExistentProp_ldev6349;
					fail( "expected an exception for missing property, got [#x#]" );
				} catch ( any e ) {
					expect( e.type ).toBe( "application" );
					expect( e.message ).toInclude( "nonExistentProp_ldev6349" );
					expect( e.message ).toInclude( "java.util.Date" );
				}
			} );

			it( title = "repeated probes of the same missing property remain stable (no state corruption)", body = function() {
				var d = createObject( "java", "java.util.Date" ).init( javaCast( "long", 0 ) );
				loop times=3 {
					try {
						var x = d.nonExistentProp_ldev6349;
						fail( "expected an exception for missing property" );
					} catch ( any e ) {
						expect( e.type ).toBe( "application" );
						expect( e.message ).toInclude( "nonExistentProp_ldev6349" );
						expect( e.message ).toInclude( "java.util.Date" );
					}
				}
				expect( d.time ).toBe( 0 );
			} );

			it( title = "calling a non-existent method on a Java object fails with a clean message (no silent NPE)", body = function() {
				var d = createObject( "java", "java.util.Date" ).init( javaCast( "long", 0 ) );
				try {
					d.nonExistentMethod_ldev6349();
					fail( "expected an exception for missing method" );
				} catch ( any e ) {
					expect( e.type ).toBe( "expression" );
					expect( e.message ).toInclude( "nonExistentMethod_ldev6349" );
				}
			} );

		} );
	}

}
