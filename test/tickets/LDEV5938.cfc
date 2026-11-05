component extends="org.lucee.cfml.test.LuceeTestCase" labels="config,mappings" {

	function beforeAll() {
		variables.originalMappings = getApplicationSettings().mappings;
		variables.config = getPageContext().getConfig();
		variables.ConfigUtil = createObject( "java", "lucee.runtime.config.ConfigUtil" );
		variables.ResourceUtil = createObject( "java", "lucee.commons.io.res.util.ResourceUtil" );
		variables.base = getTempDirectory() & "LDEV5938-" & createUUID() & server.separator.file;
		directoryCreate( variables.base, true );
		variables.absoluteDir = variables.base & "absolute-target" & server.separator.file;
		directoryCreate( variables.absoluteDir );
		variables.absoluteFile = variables.absoluteDir & "outside.cfm";
		fileWrite( variables.absoluteFile, '<cfset request.ldev5938include = "from-absolute">' );
		variables.sourceDir = getDirectoryFromPath( getCurrentTemplatePath() );
		variables.source = variables.config.getResource( getCurrentTemplatePath() ).getParentResource();
	}

	function afterAll() {
		application action="update" mappings=variables.originalMappings;
		if ( directoryExists( variables.base ) ) directoryDelete( variables.base, true );
		if ( structKeyExists( variables, "webRelDir" ) && directoryExists( variables.webRelDir ) ) directoryDelete( variables.webRelDir, true );
		if ( structKeyExists( variables, "shadows" ) ) {
			loop array=variables.shadows item="local.shadow" {
				if ( directoryExists( shadow ) ) directoryDelete( shadow, true );
			}
		}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5938 absolute paths are not appended to a base directory", function() {

			it( "getFile uses an absolute file instead of the concatenated shadow", function() {
				var base = variables.config.getResource( variables.base & "file-base" );
				directoryCreate( base.getAbsolutePath() );
				var target = variables.absoluteDir & "log.log";
				fileWrite( target, "absolute-log" );
				var shadow = base.getRealResource( target );
				directoryCreate( shadow.getParentResource().getAbsolutePath(), true );
				fileWrite( shadow.getAbsolutePath(), "shadow-log" );
				trackShadow( shadow.getParentResource() );

				var result = variables.ConfigUtil.getFile( variables.config, base, target, variables.ResourceUtil.TYPE_FILE );
				expect( result ).notToBeNull();
				expect( canon( result.getAbsolutePath() ) ).toBe( canon( target ) );
			} );

			it( "getFile still resolves a relative path against the base directory", function() {
				var base = variables.config.getResource( variables.base & "rel-base" );
				var rel = base.getRealResource( "logs/app.log" );
				directoryCreate( rel.getParentResource().getAbsolutePath(), true );
				fileWrite( rel.getAbsolutePath(), "relative-log" );

				var result = variables.ConfigUtil.getFile( variables.config, base, "logs/app.log", variables.ResourceUtil.TYPE_FILE );
				expect( result ).notToBeNull();
				expect( canon( result.getAbsolutePath() ) ).toBe( canon( rel.getAbsolutePath() ) );
			} );

			it( "getFile keeps a resource-provider path on that provider", function() {
				var base = variables.config.getConfigDir();
				var result = variables.ConfigUtil.getFile( variables.config, base, "ram://ldev5938/note.txt", variables.ResourceUtil.TYPE_FILE );
				if ( !isNull( result ) ) expect( result.getResourceProvider().getScheme() ).toBe( "ram" );
			} );

			it( "getResource prefers an existing absolute directory over the webroot shadow", function() {
				var sc = variables.config.getServletContext();
				var webroot = variables.config.getRootDirectory();
				var shadow = webroot.getRealResource( variables.absoluteDir );
				expect( canon( shadow.getAbsolutePath() ) ).notToBe( canon( variables.absoluteDir ), "shadow and target are the same directory" );
				directoryCreate( shadow.getAbsolutePath(), true );
				trackShadow( webroot.getRealResource( variables.base ) );

				var result = variables.ConfigUtil.getResource( sc, variables.absoluteDir, variables.config.getConfigDir(), variables.ResourceUtil.TYPE_DIR, variables.config, true, true );
				expect( result ).notToBeNull();
				expect( canon( result.getAbsolutePath() ) ).toBe( canon( variables.absoluteDir ) );
			} );

			it( "getResource still resolves a leading-slash path that exists only under the webroot", function() {
				var sc = variables.config.getServletContext();
				var name = "ldev5938-webrel-" & left( createUUID(), 8 );
				variables.webRelDir = variables.config.getRootDirectory().getAbsolutePath() & server.separator.file & name;
				directoryCreate( variables.webRelDir );

				var result = variables.ConfigUtil.getResource( sc, "/" & name, variables.config.getConfigDir(), variables.ResourceUtil.TYPE_DIR, variables.config, true, true );
				expect( result ).notToBeNull();
				expect( canon( result.getAbsolutePath() ) ).toBe( canon( variables.webRelDir ) );
			} );

			it( "the root mapping physical path stays the webroot", function() {
				var mappings = variables.config.getMappings();
				var root = mappings[ arrayLen( mappings ) ];
				expect( root.getVirtual() ).toBe( "/" );
				expect( canon( root.getPhysical().getAbsolutePath() ) ).toBe( canon( variables.config.getRootDirectory().getAbsolutePath() ) );
			} );

			it( "an application mapping keeps an absolute physical path when a shadow exists beside the template", function() {
				var shadow = variables.source.getRealResource( variables.absoluteDir );
				expect( canon( shadow.getAbsolutePath() ) ).notToBe( canon( variables.absoluteDir ) );
				directoryCreate( shadow.getAbsolutePath(), true );
				fileWrite( shadow.getAbsolutePath() & server.separator.file & "shadow.txt", "shadow" );
				trackShadow( variables.source.getRealResource( variables.base ) );

				var mappings = duplicate( variables.originalMappings );
				mappings[ "/ldev5938abs" ] = variables.absoluteDir;
				mappings[ "/ldev5938rel" ] = "ldev5938rel";
				var relDir = variables.sourceDir & "ldev5938rel" & server.separator.file;
				directoryCreate( relDir );
				trackShadow( variables.config.getResource( relDir ) );
				application action="update" mappings=mappings;

				expect( canon( mappingPhysical( "/ldev5938abs" ) ) ).toBe( canon( variables.absoluteDir ) );
				expect( canon( mappingPhysical( "/ldev5938rel" ) ) ).toBe( canon( relDir ) );
			} );

			it( "an application mapping keeps a resource-provider physical path", function() {
				var ramPath = "ram://ldev5938map";
				var shadow = variables.source.getRealResource( ramPath );
				directoryCreate( shadow.getAbsolutePath(), true );
				trackShadow( shadow );

				var mappings = duplicate( variables.originalMappings );
				mappings[ "/ldev5938ram" ] = ramPath;
				application action="update" mappings=mappings;

				var found = mappingByVirtual( "/ldev5938ram" );
				expect( found.getStrPhysical() ).toBe( ramPath );
				expect( found.getPhysical() ).notToBeNull();
				expect( found.getPhysical().getResourceProvider().getScheme() ).toBe( "ram" );
			} );

			it( "getPageSourceExisting still finds a virtual template", function() {
				var virtual = contractPath( getCurrentTemplatePath() );
				expect( left( replace( virtual, "\", "/", "all" ), 1 ) ).toBe( "/" );
				var ps = variables.ConfigUtil.getPageSourceExisting( getPageContext(), variables.config, getPageContext().getApplicationContext().getMappings(), virtual, false, false, true, false );
				expect( ps ).notToBeNull();
				expect( canon( ps.getResource().getAbsolutePath() ) ).toBe( canon( getCurrentTemplatePath() ) );
			} );

			it( "getPageSourceExisting does not turn a resource-provider path into a page under the webroot", function() {
				var ps = variables.ConfigUtil.getPageSourceExisting( getPageContext(), variables.config, getPageContext().getApplicationContext().getMappings(), "ram://ldev5938/nope.cfm", false, false, true, false );
				expect( isNull( local.ps ) ).toBeTrue();
			} );

			it( "an absolute template outside the webroot is executed from that file", function() {
				include variables.absoluteFile;
				expect( request.ldev5938include ).toBe( "from-absolute" );
			} );

		} );
	}

	private string function canon( required string path ) {
		var normalized = replace( arguments.path, "\", "/", "all" );
		while ( len( normalized ) > 1 && right( normalized, 1 ) == "/" ) {
			normalized = left( normalized, len( normalized ) - 1 );
		}
		return getCanonicalPath( normalized );
	}

	private string function mappingPhysical( required string virtual ) {
		return mappingByVirtual( arguments.virtual ).getPhysical().getAbsolutePath();
	}

	private any function mappingByVirtual( required string virtual ) {
		var maps = getPageContext().getApplicationContext().getMappings();
		var i = 0;
		for ( i = 1; i <= arrayLen( maps ); i++ ) {
			if ( maps[ i ].getVirtual() == arguments.virtual ) return maps[ i ];
		}
		throw( message="mapping [#arguments.virtual#] was not registered" );
	}

	private void function trackShadow( required any resource ) {
		if ( !structKeyExists( variables, "shadows" ) ) variables.shadows = [];
		arrayAppend( variables.shadows, resource.getAbsolutePath() );
	}

}
