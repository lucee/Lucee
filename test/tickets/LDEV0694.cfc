component extends="org.lucee.cfml.test.LuceeTestCase" labels="vdirs" {

	function run( testResults, testBox ) {
		describe( "LDEV-694: Virtual directory mappings from x-vdirs header", function() {

			it( "should resolve virtual directory path via expandPath", function() {
				var testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV0694/";
				var physicalPath = testDir & "virtual";

				// Use internalRequest to simulate mod_cfml sending x-vdirs header
				var result = internalRequest(
					template = createURI( "LDEV0694/test.cfm" ),
					headers = {
						"x-vdirs" = "/vdir,#physicalPath#",
						"x-vdirs-sharedkey" = "testsecret"
					}
				);

				expect( result.status ).toBe( 200 );
				expect( result.filecontent ).toInclude( 4 );
				expect( result.filecontent ).toInclude( "SUCCESS" );
			});

			it( "should reject invalid shared key", function() {
				var testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV0694/";
				var physicalPath = testDir & "virtual";

				// Use internalRequest with wrong shared key
				var result = internalRequest(
					template = createURI( "LDEV0694/test-reject.cfm" ),
					headers = {
						"x-vdirs" = "/vdir,#physicalPath#",
						"x-vdirs-sharedkey" = "wrongkey"
					}
				);

				expect( result.filecontent ).toInclude( "FAIL", "Invalid shared key should be rejected" );
			});

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}

}
