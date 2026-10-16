component extends="org.lucee.cfml.test.LuceeTestCase" labels="mapping,archive" {

	variables.testVirtualPrefix = "/test-"&createUniqueID();
	variables.adminPassword = "";
	variables.virtuals=[];

	function beforeAll() {
		variables.adminPassword = request.WEBADMINPASSWORD;
		variables.oldMappings = GetApplicationSettings().mappings;
	}

	function afterAll() {
		application action="update" mappings=variables.oldMappings;
		try {
			loop array=variables.virtuals item="local.v" {
				admin action="removeMapping" type="web" password="#variables.adminPassword#" virtual="#v#";
			}
		} 
		catch ( any e ) {}
	}

	private function createLarArchive( required string virtual, required string srcDir, required string larFile, boolean addNonCFMLFiles=false ) {
		// create mapping pointing to physical source
		admin
			action="updateMapping"
			type="web"
			password="#variables.adminPassword#"
			virtual="#arguments.virtual#"
			physical="#arguments.srcDir#"
			toplevel="true"
			archive=""
			primary="physical"
			trusted="no";

		// create an archive (compiles CFML to .class files)
		admin
			action="createArchive"
			type="web"
			password="#variables.adminPassword#"
			file="#arguments.larFile#"
			virtual="#arguments.virtual#"
			addCFMLFiles="false"
			addNonCFMLFiles="#arguments.addNonCFMLFiles#";

		expect( fileExists( arguments.larFile ) ).toBeTrue( "LAR file should exist" );

		// switch mapping to archive-only, no physical
		admin
			action="updateMapping"
			type="web"
			password="#variables.adminPassword#"
			virtual="#arguments.virtual#"
			physical=""
			toplevel="true"
			archive="#arguments.larFile#"
			primary="archive"
			trusted="no";

		// remove the physical source so we know it's coming from the archive
		directoryDelete( arguments.srcDir, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6165: LAR archive mapping with only compiled CFML", function() {

			it( "should resolve pages from a CFML-only LAR archive", function() {
				var testDir = getTempDirectory() & "LDEV6165/cfml-only/";
				var srcDir = testDir & "src/";
				var larFile = testDir & "test.lar";
				var virtual=variables.testVirtualPrefix&"CFMLOnly";

				// cleanup from previous runs
				if ( directoryExists( testDir ) ) directoryDelete( testDir, true );
				directoryCreate( srcDir, true, true );

				// create a simple CFML file — no non-CFML resources
				fileWrite( srcDir & "hello.cfm", "<cfset greeting = 'cfml-only'>" );

				createLarArchive(virtual, srcDir, larFile, false );

				// this is where the bug manifests — include from the archive
				include "#virtual#/hello.cfm";
				expect( greeting ).toBe( "cfml-only" );
			});

			it( title="should resolve pages from a LAR archive with mixed content", body=function() {
				var testDir = getTempDirectory() & "LDEV6165/mixed/";
				var srcDir = testDir & "src/";
				var larFile = testDir & "test.lar";
				var virtual=variables.testVirtualPrefix&"Mixed";
				
				// cleanup from previous runs
				if ( directoryExists( testDir ) ) directoryDelete( testDir, true );
				directoryCreate( srcDir, true, true );

				// CFML file plus a static resource
				fileWrite( srcDir & "hello.cfm", "<cfset greeting = 'mixed-content'>" );
				fileWrite( srcDir & "style.css", "body { color: red; }" );

				createLarArchive(virtual, srcDir, larFile, true );

				include "#virtual#/hello.cfm";
				expect( greeting ).toBe( "mixed-content" );
			});

		});
	}

}
