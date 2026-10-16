component extends="org.lucee.cfml.test.LuceeTestCase" labels="java" {
	import lucee.runtime.type.KeyImpl;

	function run( testResults, testBox ) {
		describe( title = "LDEV-6573: Class::init() calls a Java static method named init", body = function() {

			it( title = "static init via full name", body = function() {
				expect( lucee.runtime.type.KeyImpl::init( "lucee" ).getString() ).toBe( "lucee" );
			} );

			it( title = "static init via import", body = function() {
				expect( KeyImpl::init( "lucee" ).getString() ).toBe( "lucee" );
			} );

			it( title = "static init via java prefix", body = function() {
				expect( java:lucee.runtime.type.KeyImpl::init( "lucee" ).getString() ).toBe( "lucee" );
			} );

			it( title = "static init on an abstract class returns the static result", body = function() {
				var paths = lucee.runtime.rest.path.Path::init( "/a/b" );
				expect( arrayLen( paths ) ).toBe( 2 );
			} );

			it( title = "createObject().init() still calls the constructor", body = function() {
				expect( function() {
					createObject( "java", "lucee.runtime.rest.path.Path" ).init( "/a/b" );
				} ).toThrow( regex = "No matching Constructor" );
			} );

			it( title = "a class with only an instance init still throws", body = function() {
				expect( function() {
					javax.crypto.Cipher::init( 1, javaCast( "null", "" ) );
				} ).toThrow( regex = "No static method named \[init\]" );
			} );

			it( title = "static init with wrong arguments reports no matching method", body = function() {
				try {
					lucee.runtime.type.KeyImpl::init( 1, 2, 3 );
					fail( "expected an exception for wrong arguments" );
				}
				catch ( any e ) {
					expect( e.message ).notToInclude( "There may be an instance method" );
					expect( e.message ).toInclude( "init" );
				}
			} );

		} );
	}

}
