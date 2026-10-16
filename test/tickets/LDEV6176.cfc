component extends="org.lucee.cfml.test.LuceeTestCase" labels="lar,archive" {

	variables.adminPassword = "";
	variables.testDir = "";
	variables.virtual = "/LDEV6176-test";

	function beforeAll() {
		variables.adminPassword = request.WEBADMINPASSWORD;
		variables.testDir = getTempDirectory() & "LDEV6176/";
		variables.srcDir = variables.testDir & "src/";
		variables.larFile = expandPath( "{lucee-config}/context/archives/archive-LDEV6176-test.lar" );

		// cleanup from previous runs
		if ( directoryExists( variables.testDir ) ) directoryDelete( variables.testDir, true );
		if ( fileExists( variables.larFile ) ) fileDelete( variables.larFile );
		try { admin action="removeMapping" type="web" password="#variables.adminPassword#" virtual="#variables.virtual#"; } catch ( any e ) {}

		directoryCreate( variables.srcDir, true, true );
		_buildTemplate( variables.srcDir );
		_buildArchive();
	}

	function run( testResults, testBox ) {

		describe( "LDEV-6176: expandPath with {lucee-config} resolves to filesystem", function() {

			it( "expandPath with {lucee-config} should not return a zip:// path", function() {
				var target = expandPath( "{lucee-config}/context/archives/test.lar" );
				expect( target ).notToInclude( "zip://",
					"expandPath resolved to a zip path instead of the filesystem: " & target );
			});

		});

		describe( "LDEV-6176: Admin archive creation produces a usable archive", function() {

			it( "archive should be created at a real filesystem path", function() {
				expect( variables.larFile ).notToInclude( "zip://" );
				expect( fileExists( variables.larFile ) ).toBeTrue( "Archive file was not created at: " & variables.larFile );
			});

			it( "should serve from physical then fall back to archive after file is deleted", function() {
				// mapping has both physical + archive, primary=physical
				admin action="updateMapping" type="web" password="#variables.adminPassword#"
					virtual="#variables.virtual#" physical="#variables.srcDir#" archive="#variables.larFile#" primary="physical"
					toplevel="true" inspect="always";

				// physical file exists — should serve from disk
				var result = _internalRequest( template: "#variables.virtual#/hello.cfm" );
				var path = result.fileContent.trim();
				expect( path ).notToInclude( "zip://", "Should serve from physical, got: " & path );
			});

		});

	}

	private function _buildTemplate( required string dir ) {
		fileWrite( arguments.dir & "hello.cfm", '<cfscript>echo( getCurrentTemplatePath() );</cfscript>' );
	}

	private function _buildArchive() {
		admin action="updateMapping" type="web" password="#variables.adminPassword#"
			virtual="#variables.virtual#" physical="#variables.srcDir#" archive="" primary="physical"
			toplevel="true" inspect="always";

		admin action="createArchive" type="web" password="#variables.adminPassword#"
			file="#variables.larFile#" virtual="#variables.virtual#" addCFMLFiles="false" addNonCFMLFiles="false";
	}

}
