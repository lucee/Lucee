component extends="org.lucee.cfml.test.LuceeTestCase" labels="lar,archive" {

	variables.adminPassword = "";
	variables.mappings = [];

	function beforeAll() {
		variables.adminPassword = request.WEBADMINPASSWORD ?:"webweb";
	}

	function afterAll() {
		// cleanup all mappings created during tests
		for ( var v in variables.mappings ) {
			try { admin action="removeMapping" type="web" password="#variables.adminPassword#" virtual="#v#"; } catch ( any e ) {}
		}
	}

	function run( testResults, testBox ) {

		describe( "LDEV-6178: primary=physical — inspect modes should detect changes and fall back to archive", function() {

			it( "inspect=always, primary=physical: should detect changes and fall back to archive", function() {
				_testInspectFallback( inspect: "always", primary: "physical" );
			});

			it( "inspect=once, primary=physical: should detect changes and fall back to archive", function() {
				_testInspectFallback( inspect: "once", primary: "physical" );
			});

			it( "inspect=never, primary=physical: should NOT detect changes (expected to serve stale)", function() {
				_testInspectNoFallback( inspect: "never", primary: "physical" );
			});

		});

		describe( "LDEV-6178: getCurrentTemplatePath() should reflect the actual source", function() {

			it( "should return physical path when served from physical", function() {
				_testPathConsistency_physical();
			});

			it( "should return archive path when physical is deleted and falls back to archive", function() {
				_testPathConsistency_archiveFallback();
			});

			it( "should return archive path when primary=archive", function() {
				_testPathConsistency_archivePrimary();
			});

		});

		describe( "LDEV-6178: primary=archive — should prefer archive over physical", function() {

			it( "inspect=always, primary=archive: should serve from archive even when physical exists", function() {
				_testArchivePrimary( "always" );
			});

			it( "inspect=once, primary=archive: should serve from archive even when physical exists", function() {
				_testArchivePrimary( "once" );
			});

			it( "inspect=never, primary=archive: should serve from archive even when physical exists", function() {
				_testArchivePrimary( "never" );
			});

		});

	}

	private string function _requestPath( required struct ctx ) {
		if ( _isJsr223() ) {
			var result = _internalRequest( template: "#arguments.ctx.virtual#/hello.cfm" );
			return result.fileContent.trim();
		}
		var hostIdx = find( cgi.script_name, cgi.request_url );
		if ( hostIdx lte 0 ) throw "failed to extract host from cgi [#cgi.script_name#], [#cgi.request_url#]";
		var host = left( cgi.request_url, hostIdx - 1 );
		var webUrl = host & "#arguments.ctx.virtual#/hello.cfm";
		http method="get" url="#webUrl#" result="local.httpResult";
		return httpResult.fileContent.trim();
	}

	private boolean function _isJsr223() {
		return ( cgi.request_url eq "http://localhost/index.cfm" );
	}

	private struct function _setupTest( required string label ) {
		var ctx = {};
		ctx.testDir = getTempDirectory( unique: "LDEV6178-#arguments.label#" );
		ctx.srcDir = ctx.testDir & "src/";
		ctx.larFile = ctx.testDir & "archive-#arguments.label#.lar";
		ctx.virtual = "/LDEV6178-#arguments.label#";

		// cleanup from previous runs
		if ( directoryExists( ctx.testDir ) ) directoryDelete( ctx.testDir, true );
		directoryCreate( ctx.srcDir, true, true );

		variables.mappings.append( ctx.virtual );

		// build template and archive
		_buildTemplate( ctx.srcDir, "initial" );

		admin action="updateMapping" type="web" password="#variables.adminPassword#"
			virtual="#ctx.virtual#" physical="#ctx.srcDir#" archive="" primary="physical"
			toplevel="true" inspect="always";

		admin action="createArchive" type="web" password="#variables.adminPassword#"
			file="#ctx.larFile#" virtual="#ctx.virtual#" addCFMLFiles="false" addNonCFMLFiles="false";

		return ctx;
	}

	private function _testInspectFallback( required string inspect, required string primary ) {
		var ctx = _setupTest( "#arguments.inspect#-#arguments.primary#" );

		// overwrite with "physical" flag so we can distinguish from archived "initial"
		_buildTemplate( ctx.srcDir, "physical" );

		admin action="updateMapping" type="web" password="#variables.adminPassword#"
			virtual="#ctx.virtual#" physical="#ctx.srcDir#" archive="#ctx.larFile#" primary="#arguments.primary#"
			toplevel="true" inspect="#arguments.inspect#";

		// physical file exists — should serve from disk
		var resp = _requestPath( ctx );
		expect( resp ).toInclude( "flag=physical", "Should contain 'physical' flag, got: " & resp );

		// update physical file — should pick up the change
		_buildTemplate( ctx.srcDir, "touched" );
		resp = _requestPath( ctx );
		expect( resp ).toInclude( "flag=touched", "Should detect file change with inspect=#arguments.inspect#, got: " & resp );

		// delete the physical file — should fall back to archive
		fileDelete( ctx.srcDir & "hello.cfm" );

		resp = _requestPath( ctx );
		expect( resp ).toInclude( "flag=initial", "Should serve archived content with inspect=#arguments.inspect#, got: " & resp );

		// recreate physical file — should switch back to physical
		_buildTemplate( ctx.srcDir, "restored" );
		resp = _requestPath( ctx );
		expect( resp ).toInclude( "flag=restored", "Should switch back to physical after restore with inspect=#arguments.inspect#, got: " & resp );

		// delete again — should fall back to archive again
		fileDelete( ctx.srcDir & "hello.cfm" );
		resp = _requestPath( ctx );
		expect( resp ).toInclude( "flag=initial", "Should fall back to archive again with inspect=#arguments.inspect#, got: " & resp );

		// recreate with different content — should pick up the new content
		_buildTemplate( ctx.srcDir, "round2" );
		resp = _requestPath( ctx );
		expect( resp ).toInclude( "flag=round2", "Should serve new physical content with inspect=#arguments.inspect#, got: " & resp );
	}

	private function _testInspectNoFallback( required string inspect, required string primary ) {
		var ctx = _setupTest( "#arguments.inspect#-#arguments.primary#" );

		// overwrite with "physical" flag
		_buildTemplate( ctx.srcDir, "physical" );

		admin action="updateMapping" type="web" password="#variables.adminPassword#"
			virtual="#ctx.virtual#" physical="#ctx.srcDir#" archive="#ctx.larFile#" primary="#arguments.primary#"
			toplevel="true" inspect="#arguments.inspect#";

		// physical file exists — should serve from disk
		var resp = _requestPath( ctx );
		expect( resp ).toInclude( "flag=physical", "Should contain 'physical' flag, got: " & resp );

		// delete the physical file — with inspect=never, should still serve cached/stale
		fileDelete( ctx.srcDir & "hello.cfm" );

		resp = _requestPath( ctx );
		// inspect=never means it won't re-check, so it'll serve the cached compiled class
		expect( resp ).toInclude( "flag=physical", "inspect=never should still serve stale content, got: " & resp );
	}

	private function _testArchivePrimary( required string inspect ) {
		var ctx = _setupTest( "#arguments.inspect#-archive" );

		// overwrite physical with different flag — archive has "initial"
		_buildTemplate( ctx.srcDir, "physical" );

		admin action="updateMapping" type="web" password="#variables.adminPassword#"
			virtual="#ctx.virtual#" physical="#ctx.srcDir#" archive="#ctx.larFile#" primary="archive"
			toplevel="true" inspect="#arguments.inspect#";

		// primary=archive — should serve from archive, not physical
		var resp = _requestPath( ctx );
		expect( resp ).toInclude( "flag=initial", "Should contain 'initial' flag from archive, got: " & resp );
	}

	private function _testPathConsistency_physical() {
		var ctx = _setupTest( "path-physical" );
		_buildTemplate( ctx.srcDir, "physical" );

		admin action="updateMapping" type="web" password="#variables.adminPassword#"
			virtual="#ctx.virtual#" physical="#ctx.srcDir#" archive="#ctx.larFile#" primary="physical"
			toplevel="true" inspect="always";

		var resp = _requestPath( ctx );
		expect( resp ).toInclude( "flag=physical", "Should serve from physical, got: " & resp );
		// path should be a real filesystem path, not zip://
		expect( resp ).notToInclude( "path=zip://", "Physical path should not be a zip path, got: " & resp );
		expect( resp ).toInclude( "hello.cfm", "Path should contain hello.cfm, got: " & resp );
	}

	private function _testPathConsistency_archiveFallback() {
		var ctx = _setupTest( "path-fallback" );
		_buildTemplate( ctx.srcDir, "physical" );

		admin action="updateMapping" type="web" password="#variables.adminPassword#"
			virtual="#ctx.virtual#" physical="#ctx.srcDir#" archive="#ctx.larFile#" primary="physical"
			toplevel="true" inspect="always";

		// delete the physical file — should fall back to archive
		fileDelete( ctx.srcDir & "hello.cfm" );

		var resp = _requestPath( ctx );
		expect( resp ).toInclude( "flag=initial", "Should serve archived content, got: " & resp );
		// getCurrentTemplatePath() should reflect the archive source
		expect( resp ).toInclude( "path=zip://", "Archive-served path should be a zip:// path, got: " & resp );
	}

	private function _testPathConsistency_archivePrimary() {
		var ctx = _setupTest( "path-primary" );
		_buildTemplate( ctx.srcDir, "physical" );

		admin action="updateMapping" type="web" password="#variables.adminPassword#"
			virtual="#ctx.virtual#" physical="#ctx.srcDir#" archive="#ctx.larFile#" primary="archive"
			toplevel="true" inspect="always";

		var resp = _requestPath( ctx );
		expect( resp ).toInclude( "flag=initial", "Should serve archived content, got: " & resp );
		// getCurrentTemplatePath() should reflect the archive source
		expect( resp ).toInclude( "path=zip://", "Archive-served path should be a zip:// path, got: " & resp );
	}

	private function _buildTemplate( required string dir, required string flag ) {
		fileWrite( arguments.dir & "hello.cfm", '<cfset m = getPageContext().getCurrentPageSource().getMapping()><cfset writeOutput( "flag=' & arguments.flag & ' | inspect=" & m.getInspectTemplate() & " | physicalFirst=" & m.isPhysicalFirst() & " | trusted=" & m.isTrusted() & " | virtual=" & m.getVirtual() & " | path=" & getCurrentTemplatePath() )>' );
	}

}
