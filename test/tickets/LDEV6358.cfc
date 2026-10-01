component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-6358 inspect templates", function() {

			it( title="always recompiles an external change on the next request", body=function( currentSpec ) {
				assertInspect( "always", "v2" );
			});

			it( title="once recompiles an external change on the next request", body=function( currentSpec ) {
				assertInspect( "once", "v2" );
			});

			it( title="never keeps the compiled template after an external change", body=function( currentSpec ) {
				assertInspect( "never", "v1" );
			});

			it( title="auto ignores an external change until the inspect ticker runs", body=function( currentSpec ) {
				assertInspect( "auto", "v1", "v2" );
			});

		});
	}

	/**
	 * Once checks again on the next request. Never keeps the first compile.
	 * Auto keeps it until the server inspect ticker resets the page.
	 */
	private void function assertInspect( required string inspect, required string immediate, string afterInterval ) {
		var virtual = "/test-inspect-template-" & arguments.inspect;
		var dir = getTempDirectory() & "test-inspect-template-" & arguments.inspect & "-" & createUniqueId() & server.separator.file;
		directoryCreate( dir );
		writeRaw( dir & "Application.cfc", "component { this.name = ""inspect-" & arguments.inspect & "-" & createUniqueId() & """; }" );
		var path = dir & "index.cfm";
		writeRaw( path, "<cfoutput>v1</cfoutput>" );
		var adminWeb = new org.lucee.cfml.Administrator( "server", request.ServerAdminPassword );
		try {
			adminWeb.updateMapping( virtual=virtual, physical=dir, archive="", primary="physical", inspect=arguments.inspect );
			expect( requestTemplate( virtual ) ).toBe( "v1" );
			writeRaw( path, "<cfoutput>v2</cfoutput>", 2000 );
			expect( requestTemplate( virtual ) ).toBe( arguments.immediate );
			if ( structKeyExists( arguments, "afterInterval" ) ) {
				var serverAdmin = new org.lucee.cfml.Administrator( "server", request.ServerAdminPassword );
				var slow = serverAdmin.getPerformanceSettings().inspectTemplateIntervalSlow;
				if ( !isNumeric( slow ) || slow < 1 ) slow = 3000;
				sleep( slow + 1500 );
				expect( requestTemplate( virtual ) ).toBe( arguments.afterInterval );
			}
		}
		finally {
			try {
				adminWeb.removeMapping( virtual );
			}
			catch ( any e ) {}
			if ( directoryExists( dir ) ) directoryDelete( dir, true );
		}
	}

	private string function requestTemplate( required string virtual ) {
		return trim( _internalRequest( template="#arguments.virtual#/index.cfm" ).fileContent );
	}

	// fileWrite and cffile call PageSourcePool.flush for .cfm/.cfc, which drops the compiled page.
	// The next request then recompiles under every inspect mode, so Never and Auto would look like Always.
	// FileWriter leaves the pool alone, the same as an editor save or a deploy.
	private void function writeRaw( required string path, required string content, numeric offsetMillis=0 ) {
		var writer = createObject( "java", "java.io.FileWriter" ).init( arguments.path );
		try {
			writer.write( arguments.content );
		}
		finally {
			writer.close();
		}
		createObject( "java", "java.io.File" ).init( arguments.path ).setLastModified( createObject( "java", "java.lang.System" ).currentTimeMillis() + arguments.offsetMillis );
	}

}
