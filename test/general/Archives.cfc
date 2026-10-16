component extends="org.lucee.cfml.test.LuceeTestCase" labels="archive,mapping" {

	variables.adminPassword = "";
	variables.testDir = "";
	variables.srcBase = "";

	function beforeAll() {
		variables.adminPassword = request.WEBADMINPASSWORD;
		variables.testDir = getTempDirectory() & "archivesTest/";
		variables.srcBase = getTempDirectory() & "archivesSrc/";

		// cleanup from previous runs
		if ( directoryExists( variables.testDir ) ) directoryDelete( variables.testDir, true );
		if ( directoryExists( variables.srcBase ) ) directoryDelete( variables.srcBase, true );
		directoryCreate( variables.testDir, true, true );
		directoryCreate( variables.srcBase, true, true );

		// build all the .lar archives we need
		_buildMappingArchive();
		_buildComponentArchive();
		_buildCustomTagArchive();
		_buildHybridPhysical();
		_buildHybridPhysicalFirst();
		_buildMissingPhysical();
		_buildMissingArchive();
		_buildCorruptArchive();
	}

	function afterAll() {
		// remove admin mappings used during archive creation
		var virtuals = [ "/archivesTest-mapping", "/archivesTest-component", "/archivesTest-customtag", "/archivesTest-hybrid", "/archivesTest-hybridPF" ];
		for ( var v in virtuals ) {
			try { admin action="removeMapping" type="web" password="#variables.adminPassword#" virtual="#v#"; } catch ( any e ) {}
		}
		try { admin action="removeComponentMapping" type="web" password="#variables.adminPassword#" virtual="/archivesTest-component"; } catch ( any e ) {}
		try { admin action="removeCustomTag" type="web" password="#variables.adminPassword#" virtual="/archivesTest-customtag"; } catch ( any e ) {}
	}

	function run( testResults, testBox ) {

		describe( "Application.cfc this.mappings with archive", function() {

			it( "should resolve a template from a LAR via this.mappings", function() {
				var result = _internalRequest(
					template: "#_uri()#/mapping/index.cfm"
				);
				expect( result.fileContent.trim() ).toBe( "hello-from-archive" );
			});

		});

		describe( "Application.cfc this.componentPaths with archive", function() {

			it( "should instantiate a CFC from a LAR via this.componentPaths", function() {
				var result = _internalRequest(
					template: "#_uri()#/componentPaths/index.cfm"
				);
				expect( result.fileContent.trim() ).toBe( "component-from-archive" );
			});

		});

		describe( "Application.cfc this.customTagPaths with archive", function() {

			it( title="should invoke a custom tag from a LAR via this.customTagPaths", body=function() {
				var result = _internalRequest(
					template: "#_uri()#/customTagPaths/index.cfm"
				);
				expect( result.fileContent.trim() ).toBe( "customtag-from-archive" );
			});

		});

		describe( "Hybrid physical + archive with primary=archive", function() {

			it( title="should prefer archive over physical when primary is archive", body=function() {
				var result = _internalRequest(
					template: "#_uri()#/hybrid/index.cfm",
					urls: { scene: "archive" }
				);
				expect( result.fileContent.trim() ).toBe( "from-archive" );
			});

			it( "should fall back to physical for files not in the archive", function() {
				var result = _internalRequest(
					template: "#_uri()#/hybrid/index.cfm",
					urls: { scene: "fallback" }
				);
				expect( result.fileContent.trim() ).toBe( "from-physical" );
			});

		});

		describe( "Hybrid physical + archive with primary=physical (default)", function() {

			it( "should prefer physical over archive when primary is physical", function() {
				var result = _internalRequest(
					template: "#_uri()#/hybridPhysicalFirst/index.cfm",
					urls: { scene: "physical" }
				);
				expect( result.fileContent.trim() ).toBe( "from-physical" );
			});

			it( "should fall back to archive for files not on disk", function() {
				var result = _internalRequest(
					template: "#_uri()#/hybridPhysicalFirst/index.cfm",
					urls: { scene: "fallback" }
				);
				expect( result.fileContent.trim() ).toBe( "from-archive" );
			});

		});

		describe( "Graceful fallback when one side of mapping is missing", function() {

			it( "should fall back to physical when archive path does not exist", function() {
				var result = _internalRequest(
					template: "#_uri()#/missingArchive/index.cfm"
				);
				expect( result.fileContent.trim() ).toBe( "from-physical" );
			});

			it( "should fall back to archive when physical path does not exist", function() {
				var result = _internalRequest(
					template: "#_uri()#/missingPhysical/index.cfm"
				);
				expect( result.fileContent.trim() ).toBe( "hello-from-archive" );
			});

		});

		describe( "Graceful fallback when archive is corrupt", function() {

			it( "should fall back to physical when archive is not a valid LAR", function() {
				var result = _internalRequest(
					template: "#_uri()#/corruptArchive/index.cfm"
				);
				expect( result.fileContent.trim() ).toBe( "from-physical" );
			});

		});

	}

	// ---- archive builders ----

	private function _buildMappingArchive() {
		var srcDir = variables.srcBase & "mapping/";
		var larFile = variables.testDir & "mapping.lar";
		var virtual = "/archivesTest-mapping";

		directoryCreate( srcDir, true, true );
		fileWrite( srcDir & "hello.cfm", "<cfset writeOutput( 'hello-from-archive' )>" );

		admin action="updateMapping" type="web" password="#variables.adminPassword#"
			virtual="#virtual#" physical="#srcDir#" archive="" primary="physical"
			toplevel="true" trusted="no";

		admin action="createArchive" type="web" password="#variables.adminPassword#"
			file="#larFile#" virtual="#virtual#" addCFMLFiles="false" addNonCFMLFiles="false";

		// nuke the source so we know it comes from the archive
		directoryDelete( srcDir, true );
	}

	private function _buildComponentArchive() {
		var srcDir = variables.srcBase & "components/";
		var larFile = variables.testDir & "components.lar";
		var virtual = "/archivesTest-component";

		directoryCreate( srcDir, true, true );
		fileWrite( srcDir & "ArchiveGreeter.cfc", 'component { function greet() { return "component-from-archive"; } }' );

		admin action="updateComponentMapping" type="web" password="#variables.adminPassword#"
			virtual="#virtual#" physical="#srcDir#" archive="" primary="physical";

		admin action="createComponentArchive" type="web" password="#variables.adminPassword#"
			file="#larFile#" virtual="#virtual#" addCFMLFiles="false" addNonCFMLFiles="false";

		directoryDelete( srcDir, true );
	}

	private function _buildCustomTagArchive() {
		var srcDir = variables.srcBase & "customtags/";
		var larFile = variables.testDir & "customtags.lar";
		var virtual = "/archivesTest-customtag";

		directoryCreate( srcDir, true, true );
		fileWrite( srcDir & "greeting.cfm", '<cfif thisTag.executionMode eq "start"><cfset caller.ctResult = "customtag-from-archive"></cfif>' );

		admin action="updateCustomTag" type="web" password="#variables.adminPassword#"
			virtual="#virtual#" physical="#srcDir#" archive="" primary="physical";

		admin action="createCTArchive" type="web" password="#variables.adminPassword#"
			file="#larFile#" virtual="#virtual#" addCFMLFiles="false" addNonCFMLFiles="false";

		directoryDelete( srcDir, true );
	}

	private function _buildHybridPhysical() {
		var hybridSrcDir = variables.srcBase & "hybrid/";
		var hybridLarFile = variables.testDir & "hybrid.lar";
		var virtual = "/archivesTest-hybrid";
		var physDir = variables.testDir & "hybrid-physical/";

		// build archive with hello.cfm that says "from-archive"
		directoryCreate( hybridSrcDir, true, true );
		fileWrite( hybridSrcDir & "hello.cfm", "<cfset writeOutput( 'from-archive' )>" );

		admin action="updateMapping" type="web" password="#variables.adminPassword#"
			virtual="#virtual#" physical="#hybridSrcDir#" archive="" primary="physical"
			toplevel="true" trusted="no";

		admin action="createArchive" type="web" password="#variables.adminPassword#"
			file="#hybridLarFile#" virtual="#virtual#" addCFMLFiles="false" addNonCFMLFiles="false";

		directoryDelete( hybridSrcDir, true );

		// create physical dir with a different hello.cfm (should be ignored) and a fallback.cfm (not in archive)
		directoryCreate( physDir, true, true );
		fileWrite( physDir & "hello.cfm", "<cfset writeOutput( 'from-physical-should-not-see' )>" );
		fileWrite( physDir & "fallback.cfm", "<cfset writeOutput( 'from-physical' )>" );
	}

	private function _buildHybridPhysicalFirst() {
		var srcDir = variables.srcBase & "hybridPF/";
		var larFile = variables.testDir & "hybridPF.lar";
		var virtual = "/archivesTest-hybridPF";
		var physDir = variables.testDir & "hybridPF-physical/";

		// archive has hello.cfm (should lose to physical) and archiveOnly.cfm (fallback target)
		directoryCreate( srcDir, true, true );
		fileWrite( srcDir & "hello.cfm", "<cfset writeOutput( 'from-archive-should-not-see' )>" );
		fileWrite( srcDir & "archiveOnly.cfm", "<cfset writeOutput( 'from-archive' )>" );

		admin action="updateMapping" type="web" password="#variables.adminPassword#"
			virtual="#virtual#" physical="#srcDir#" archive="" primary="physical"
			toplevel="true" trusted="no";

		admin action="createArchive" type="web" password="#variables.adminPassword#"
			file="#larFile#" virtual="#virtual#" addCFMLFiles="false" addNonCFMLFiles="false";

		directoryDelete( srcDir, true );

		// physical has hello.cfm only — no archiveOnly.cfm
		directoryCreate( physDir, true, true );
		fileWrite( physDir & "hello.cfm", "<cfset writeOutput( 'from-physical' )>" );
	}

	private function _buildMissingPhysical() {
		// physical path points to a non-existent directory, archive is valid
		// reuse the mapping.lar which has hello.cfm
		var physDir = variables.testDir & "does-not-exist/";
		// intentionally do NOT create physDir
	}

	private function _buildCorruptArchive() {
		var corruptLar = variables.testDir & "corrupt.lar";
		var physDir = variables.testDir & "corruptArchive-physical/";

		// write garbage bytes — not a valid JAR/ZIP
		fileWrite( corruptLar, repeatString( "NOT A VALID LAR FILE", 100 ) );

		directoryCreate( physDir, true, true );
		fileWrite( physDir & "hello.cfm", "<cfset writeOutput( 'from-physical' )>" );
	}

	private function _buildMissingArchive() {
		// archive path points to a non-existent .lar, physical is valid
		var physDir = variables.testDir & "missingArchive-physical/";
		directoryCreate( physDir, true, true );
		fileWrite( physDir & "hello.cfm", "<cfset writeOutput( 'from-physical' )>" );
	}

	private function _buildCorruptArchive() {
		var corruptLar = variables.testDir & "corrupt.lar";
		var physDir = variables.testDir & "corruptArchive-physical/";

		// write garbage bytes — not a valid JAR/ZIP
		fileWrite( corruptLar, repeatString( "NOT A VALID LAR FILE", 100 ) );

		directoryCreate( physDir, true, true );
		fileWrite( physDir & "hello.cfm", "<cfset writeOutput( 'from-physical' )>" );
	}

	private string function _uri() {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "archives";
	}

}
