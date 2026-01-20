component extends="org.lucee.cfml.test.LuceeTestCase" skip=false {

	function run( testResults, testBox ){
		describe( "LDEV-5934: Include template with ../ from mapped directory", function(){
			it( "should resolve ../ relative to physical directory, not virtual mapping", function(){
				local.result = _InternalRequest(
					template: "#testMappingVirtual#/sub/test.cfm"
				);
				expect( result.filecontent.trim() ).toBe( "success" );
			});
		});
	}

	// Before all tests, set up the mapping and files
	function beforeAll(){
		variables.testMappingVirtual = "/ldev5934test";
		variables.testDir = getTempDirectory() & "LDEV5934/";
		variables.subDir = variables.testDir & "sub/";

		// Create directory structure
		directoryCreate( variables.testDir, true, true );
		directoryCreate( variables.subDir, true, true );

		// Create the target file at the root level (one level up from sub/)
		fileWrite( variables.testDir & "target.cfm", "success" );

		// Create the test file in the sub directory that includes ../target.cfm
		fileWrite( variables.subDir & "test.cfm", '<cfinclude template="../target.cfm">' );

		// Add mapping
		application action="update" mappings={
			"#variables.testMappingVirtual#": variables.testDir
		};
	}

	// After all tests, cleanup
	function afterAll(){
		// Remove mapping
		application action="update" mappings={
			"#variables.testMappingVirtual#": nullValue()
		};

		// Note: Per test guidelines, leave artifacts for inspection
		// Only cleanup would be done before the test, not after
	}

}
