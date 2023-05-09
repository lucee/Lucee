component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {
	function beforeAll(){
		variables.testDir = server._getTempDir( "LDEV1388" );
		variables.path = "#getDirectoryFromPath(getCurrenttemplatepath())#LDEV1388\";
	}

	function afterAll(){
		if ( directoryExists( testDir ) ){
			DirectoryDelete( testDir, true );
		}
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1388", function() {
			it(title="checking CMYK image using cfimage tag, with action='resize' ", body = function( currentSpec ) {
				loop from=1 to=4 index="local.indx" {
					var img = getTempFile( variables.testDir, "ldev1388", "jpg" );
					cfimage (action="resize", 
						source="#path#/cmyk_#indx#.jpg", 
						destination="#img#", height="333",  width="500" , overwrite="yes");
					expect( IsImageFile( img ) ).toBe("true");
				}
			});

			it(title="checking CMYK image using cfimage tag, with action='convert' ", body = function( currentSpec ) {
				loop from=1 to=4 index="local.indx" {
					var img = getTempFile( variables.testDir, "ldev1388", "png" );
					cfimage (action="convert", 
						source="#path#/cmyk_#indx#.jpg", 
						destination="#img#", format="png" , overwrite="yes");
					expect( IsImageFile( img ) ).toBe("true");
				}
			});

			it(title="checking CMYK image using cfimage tag, with action='write' ", body = function( currentSpec ) {
				loop from=1 to=4 index="local.indx" {
					var img = getTempFile( variables.testDir, "ldev1388", "jpg" );
					cfimage (action="convert", 
						source="#path#/cmyk_#indx#.jpg", 
						destination="#img#", overwrite="yes");
					expect( IsImageFile( img ) ).toBe("true");
				}
			});

		});
	}
}