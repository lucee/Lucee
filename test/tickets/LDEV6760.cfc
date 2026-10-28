component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-6760: the Railo 4 compatibility servlets are removed", function() {

			it( title="railo.loader.servlet.CFMLServlet is not available", body=function( currentSpec ) {
				expect( function() {
					createObject( "java", "railo.loader.servlet.CFMLServlet" );
				} ).toThrow();
			} );

			it( title="railo.loader.servlet.RestServlet is not available", body=function( currentSpec ) {
				expect( function() {
					createObject( "java", "railo.loader.servlet.RestServlet" );
				} ).toThrow();
			} );

			it( title="the Lucee servlets are still available", body=function( currentSpec ) {
				expect( isObject( createObject( "java", "lucee.loader.servlet.CFMLServlet" ) ) ).toBeTrue();
				expect( isObject( createObject( "java", "lucee.loader.servlet.RestServlet" ) ) ).toBeTrue();
			} );

		} );
	}

}
