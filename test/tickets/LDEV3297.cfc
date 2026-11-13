component extends="org.lucee.cfml.test.LuceeTestCase" labels="cfimport,customtag,inspect" {

	variables.virtual = "/ldev3297";

	function beforeAll() {
		variables.adminPassword = request.WEBADMINPASSWORD ?: "webweb";
		variables.appDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV3297/";
		variables.mappingDir = variables.appDir & "mapping/";
		variables.dynamicFile = variables.mappingDir & "Dynamic.cfm";
		variables.base = "/test/tickets/LDEV3297/";
		_register( "auto" );
	}

	function afterAll() {
		try { admin action="removeMapping" type="web" password="#variables.adminPassword#" virtual="#variables.virtual#"; } catch ( any e ) {}
		if ( fileExists( variables.dynamicFile ) ) fileDelete( variables.dynamicFile );
	}

	function run( testResults, testBox ) {

		describe( "LDEV-3297 cfimport custom tag resolution", function() {

			it( "renders a tag found via a relative taglib, repeatedly", function() {
				loop times=3 {
					expect( _request( "relative.cfm" ) ).toBe( "(a)(b)" );
				}
			});

			it( "renders nested tags", function() {
				loop times=3 {
					expect( _request( "nested.cfm" ) ).toBe( "((x)(y))" );
				}
			});

			it( "resolves the same relative taglib per calling directory", function() {
				loop times=2 {
					expect( _request( "relative.cfm" ) ).toBe( "(a)(b)" );
					expect( _request( "sub/relative.cfm" ) ).toBe( "[a][b]" );
				}
			});

			it( "renders a tag found via a mapping taglib, repeatedly", function() {
				loop times=3 {
					expect( _request( "mapping.cfm" ) ).toBe( "{a}{{b}}" );
				}
			});

			it( "reports a missing tag", function() {
				_removeDynamic();
				inspectTemplates();
				expect( _request( "dynamic.cfm", { via="import" } ) ).toBe( "MISSING" );
			});

			loop array=[ "never", "once", "always", "auto" ] item="local.mode" {
				var inspect = local.mode;
				describe( "template inspection #inspect#", function() {

					it( "picks up a tag file added, changed and removed at runtime like cfmodule", function() {
						_register( inspect );
						_removeDynamic();
						inspectTemplates();

						// not there yet
						_expectBoth( "MISSING", "missing" );

						// added
						_writeDynamic( "v1" );
						_expectBoth( "v1", "added" );
						// again, to be served from the cache
						_expectBoth( "v1", "added, repeated" );

						// changed, without inspectTemplates() only the modes that inspect on every use or once per request
						// are guaranteed to see it
						_writeDynamic( "v2" );
						if ( inspect == "once" || inspect == "always" ) _expectBoth( "v2", "changed" );
						inspectTemplates();
						_expectBoth( "v2", "changed, after inspectTemplates()" );

						// removed
						_removeDynamic();
						if ( inspect == "once" || inspect == "always" ) _expectBoth( "MISSING", "removed" );
						inspectTemplates();
						_expectBoth( "MISSING", "removed, after inspectTemplates()" );

						// added again
						_writeDynamic( "v3" );
						_expectBoth( "v3", "added again" );
					});

				});
			}

		});
	}

	private function _expectBoth( required string expected, required string step ) {
		var viaModule = _request( "dynamic.cfm", { via="module" } );
		var viaImport = _request( "dynamic.cfm", { via="import" } );
		expect( viaModule ).toBe( arguments.expected, "cfmodule, #arguments.step#" );
		expect( viaImport ).toBe( arguments.expected, "cfimport, #arguments.step#" );
	}

	private string function _request( required string template, struct urls={} ) {
		return trim( _internalRequest( template=variables.base & arguments.template, urls=arguments.urls ).fileContent );
	}

	private function _register( required string inspect ) {
		admin action="updateMapping" type="web" password="#variables.adminPassword#"
			virtual="#variables.virtual#" physical="#variables.mappingDir#" archive="" primary="physical"
			toplevel="true" inspect="#arguments.inspect#";
	}

	private function _writeDynamic( required string content ) {
		fileWrite( variables.dynamicFile, '<cfif thistag.executionMode eq "start">#arguments.content#</cfif>' );
	}

	private function _removeDynamic() {
		if ( fileExists( variables.dynamicFile ) ) fileDelete( variables.dynamicFile );
	}
}
