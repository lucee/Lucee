component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.name = "LDEV3159" & getTickCount();
		variables.dir = getTempDirectory() & variables.name & "/";
		directoryCreate( variables.dir );

		fileWrite( variables.dir & "restPathOnly.cfc", 'component restPath="/ldev3159/restPathOnly" {}' );
		fileWrite( variables.dir & "restPathRestFalse.cfc", 'component restPath="/ldev3159/restPathRestFalse" rest=false {}' );
		fileWrite( variables.dir & "restTrueRestPath.cfc", 'component restPath="/ldev3159/restTrueRestPath" rest=true {}' );
		fileWrite( variables.dir & "noRestAttributes.cfc", 'component {}' );
		fileWrite( variables.dir & "emptyRestPath.cfc", 'component restPath="" {}' );

		RestInitApplication( variables.dir, "/" & variables.name, false, request.webadminpassword );
		variables.paths = listRestPaths();
	}

	function afterAll() {
		try {
			RestDeleteApplication( variables.dir, request.webadminpassword );
		}
		finally {
			if ( directoryExists( variables.dir ) ) directoryDelete( variables.dir, true );
		}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-3159 restPath implies rest=true when rest is not set", function() {

			it( "restPath only => exposed", function() {
				expect( variables.paths ).toInclude( "/ldev3159/restPathOnly" );
			} );

			it( "restPath + rest=false => not exposed", function() {
				expect( variables.paths ).notToInclude( "/ldev3159/restPathRestFalse" );
			} );

			it( "restPath + rest=true => exposed", function() {
				expect( variables.paths ).toInclude( "/ldev3159/restTrueRestPath" );
			} );

			it( "no rest and no restPath => not exposed", function() {
				expect( arrayLen( variables.paths ) ).toBe( 2, "only restPathOnly and restTrueRestPath are exposed" );
			} );

			it( "empty restPath and no rest => not exposed", function() {
				expect( variables.paths ).notToInclude( "" );
			} );

		} );
	}

	private array function listRestPaths() {
		var pc = getPageContext();
		var mappings = pc.getConfig().getRestMappings();
		loop array=mappings item="local.m" {
			if ( m.getVirtual() == "/" & variables.name ) {
				var result = [];
				loop array=m.listSources( pc ) item="local.p" {
					arrayAppend( result, p );
				}
				return result;
			}
		}
		throw "rest mapping [/#variables.name#] not found";
	}

}
