component extends="org.lucee.cfml.test.LuceeTestCase" labels="mapping" {

	function beforeAll() {
		variables.originalMappings = getApplicationSettings().mappings;
		variables.baseDir = getTempDirectory() & "LDEV6449-" & createUUID() & "/";
		variables.existingDir = variables.baseDir & "existing/";
		directoryCreate( variables.existingDir, true, true );
	}

	function afterAll() {
		application action="update" mappings=variables.originalMappings;
		if ( directoryExists( variables.baseDir ) ) directoryDelete( variables.baseDir, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6449 getDisplayPath() can return null", function() {

			it( title="getDisplayPath() is not null for a mapping with an empty physical path", body=function( currentSpec ) {
				var ps = getMappedPageSource( "/ldev6449empty", { physical: "" } );
				expect( isNull( ps.getDisplayPath() ) ).toBeFalse();
				expect( ps.getDisplayPath() ).toBe( "/ldev6449empty/test.cfm" );
			});

			it( title="getDisplayPath() is not null for an archive only mapping whose archive does not exist", body=function( currentSpec ) {
				var ps = getMappedPageSource( "/ldev6449archive", { archive: variables.baseDir & "missing.lar" } );
				expect( isNull( ps.getDisplayPath() ) ).toBeFalse();
				expect( ps.getDisplayPath() ).toBe( "/ldev6449archive/test.cfm" );
			});

			it( title="the display path of an unresolved mapping can be used to look the page up again", body=function( currentSpec ) {
				var ps = getMappedPageSource( "/ldev6449lookup", { physical: "" } );
				expect( function() {
					getPageContext().getPageSources( ps.getDisplayPath() );
				}).notToThrow();
			});

			it( title="including a template from an unresolved mapping still throws missinginclude", body=function( currentSpec ) {
				getMappedPageSource( "/ldev6449include", { physical: "" } );
				expect( function() {
					include "/ldev6449include/test.cfm";
				}).toThrow( "missinginclude" );
			});

			it( title="getDisplayPath() still returns the physical file for a mapping with an existing folder", body=function( currentSpec ) {
				var ps = getMappedPageSource( "/ldev6449existing", { physical: variables.existingDir } );
				var displayPath = replace( ps.getDisplayPath(), "\", "/", "all" );
				expect( displayPath ).toInclude( "/existing/test.cfm" );
				expect( displayPath ).notToBe( "/ldev6449existing/test.cfm" );
			});

		});
	}

	private function getMappedPageSource( required string virtual, required struct mapping ) {
		var mappings = duplicate( variables.originalMappings );
		mappings[ arguments.virtual ] = arguments.mapping;
		application action="update" mappings=mappings;
		var sources = getPageContext().getPageSources( arguments.virtual & "/test.cfm" );
		expect( arrayLen( sources ) ).toBeGT( 0 );
		return sources[ 1 ];
	}

}
