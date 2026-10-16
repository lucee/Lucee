component extends="org.lucee.cfml.test.LuceeTestCase" labels="inspect,performance" {

	variables.virtual = "/ldev6375";

	function beforeAll() {
		variables.adminPassword = request.WEBADMINPASSWORD ?: "webweb";
		variables.appDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6375/";
		variables.mappingDir = variables.appDir & "mapping/";
		variables.template = "/test/tickets/LDEV6375/test.cfm";

		if ( !directoryExists( variables.mappingDir ) ) directoryCreate( variables.mappingDir, true, true );

		admin action="updateMapping" type="web" password="#variables.adminPassword#"
			virtual="#variables.virtual#" physical="#variables.mappingDir#" archive="" primary="physical"
			toplevel="true" inspect="never";
	}

	function afterAll() {
		try { admin action="removeMapping" type="web" password="#variables.adminPassword#" virtual="#variables.virtual#"; } catch ( any e ) {}
		for ( var f in [ "ghost.cfm", "ghost.cfc" ] ) {
			if ( fileExists( variables.mappingDir & f ) ) fileDelete( variables.mappingDir & f );
		}
	}

	function run( testResults, testBox ) {

		describe( "LDEV-6375 inspect=never caches negative file lookups", function() {

			it( "caches a missing .cfm under inspect=never (cfinclude path)", function() {
				_runScenario( target="cfm", file="ghost.cfm", body="" );
			});

			it( "caches a missing .cfc under inspect=never (createObject path)", function() {
				_runScenario( target="cfc", file="ghost.cfc", body='component { function hello() { return "hi"; } }' );
			});

		});
	}

	private function _runScenario( required string target, required string file, required string body ) {
		var path = variables.mappingDir & arguments.file;
		if ( fileExists( path ) ) fileDelete( path );
		// the mapping was registered with inspect=never; if the cache from a prior it() block lingers,
		// clear it so step 1 starts from a known state
		inspectTemplates();

		var r1 = _internalRequest( template=variables.template, urls={ target=arguments.target } );
		expect( r1.fileContent.trim() ).toBe( "MISSING", "step 1 baseline: #arguments.file# truly missing" );

		fileWrite( path, arguments.body );

		var r2 = _internalRequest( template=variables.template, urls={ target=arguments.target } );
		expect( r2.fileContent.trim() ).toBe( "MISSING", "step 3 negative cache should hold #arguments.file# under NEVER" );

		inspectTemplates();
		var r3 = _internalRequest( template=variables.template, urls={ target=arguments.target } );
		expect( r3.fileContent.trim() ).toBe( "FOUND", "step 4 inspectTemplates() should clear #arguments.file# cache" );
	}
}
