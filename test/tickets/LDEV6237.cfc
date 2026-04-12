component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {

		describe( "LDEV-6237 error messages when variable names shadow scopes", function() {

			it( "error message mentions scope name when calling method on server scope", function() {
				try {
					var server = createObject( "java", "java.lang.StringBuilder" ).init( "test" );
					server.reverse();
					fail( "should have thrown an error" );
				} catch ( any e ) {
					expect( e.message ).toInclude( "Server" );
					expect( e.message ).toInclude( "scope" );
				}
			} );

			it( "error message mentions scope name when calling method on application scope", function() {
				try {
					var application = createObject( "java", "java.lang.StringBuilder" ).init( "test" );
					application.reverse();
					fail( "should have thrown an error" );
				} catch ( any e ) {
					expect( e.message ).toInclude( "Application" );
					expect( e.message ).toInclude( "scope" );
				}
			} );

			it( "error message mentions scope name when calling method on session scope", function() {
				try {
					var session = createObject( "java", "java.lang.StringBuilder" ).init( "test" );
					session.reverse();
					fail( "should have thrown an error" );
				} catch ( any e ) {
					expect( e.message ).toInclude( "Session" );
					expect( e.message ).toInclude( "scope" );
				}
			} );

			it( "error message does NOT mention scope for a plain struct", function() {
				try {
					var s = { "name": "zac" };
					s.reverse();
					fail( "should have thrown an error" );
				} catch ( any e ) {
					expect( e.message ).toInclude( "Struct" );
					expect( e.message ).notToInclude( "scope" );
				}
			} );

			it( "error message mentions scope name with named arguments", function() {
				try {
					var server = createObject( "java", "java.lang.StringBuilder" ).init( "test" );
					server.reverse( foo="bar" );
					fail( "should have thrown an error" );
				} catch ( any e ) {
					expect( e.message ).toInclude( "Server" );
					expect( e.message ).toInclude( "scope" );
				}
			} );

		} );
	}

}
