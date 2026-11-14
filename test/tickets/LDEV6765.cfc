component extends="org.lucee.cfml.test.LuceeTestCase" labels="mappings" {

	variables.adminPassword = "";
	variables.prefix = "/ldev6765-";
	variables.iterations = 100;

	function beforeAll() {
		variables.adminPassword = request.WEBADMINPASSWORD ?: "webweb";
		variables.testDir = getTempDirectory( unique: "LDEV6765" );
		directoryCreate( variables.testDir & "src/", true, true );
		fileWrite( variables.testDir & "src/hello.cfm", "hello" );
	}

	function afterAll() {
		admin action="getMappings" type="web" password="#variables.adminPassword#" returnVariable="local.mappings";
		loop query="#mappings#" {
			if ( left( mappings.virtual, len( variables.prefix ) ) == variables.prefix ) {
				admin action="removeMapping" type="web" password="#variables.adminPassword#" virtual="#mappings.virtual#";
			}
		}
		if ( directoryExists( variables.testDir ) ) directoryDelete( variables.testDir, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6765: a mapping added with admin updateMapping", function() {

			it( "is visible to the next admin action right away", function() {
				for ( var i = 1; i <= variables.iterations; i++ ) {
					var virtual = variables.prefix & i;
					admin action="updateMapping" type="web" password="#variables.adminPassword#"
						virtual="#virtual#" physical="#variables.testDir#src/" archive="" primary="physical"
						toplevel="true" inspect="always";

					expect( _hasMapping( virtual ) ).toBeTrue( "mapping [#virtual#] should be visible right after updateMapping (iteration #i#)" );

					admin action="removeMapping" type="web" password="#variables.adminPassword#" virtual="#virtual#";

					expect( _hasMapping( virtual ) ).toBeFalse( "mapping [#virtual#] should be gone right after removeMapping (iteration #i#)" );
				}
			});

			it( "can be used by createArchive right away", function() {
				for ( var i = 1; i <= 10; i++ ) {
					var virtual = variables.prefix & "archive-" & i;
					var larFile = variables.testDir & "archive-#i#.lar";
					admin action="updateMapping" type="web" password="#variables.adminPassword#"
						virtual="#virtual#" physical="#variables.testDir#src/" archive="" primary="physical"
						toplevel="true" inspect="always";

					admin action="createArchive" type="web" password="#variables.adminPassword#"
						file="#larFile#" virtual="#virtual#" addCFMLFiles="false" addNonCFMLFiles="false";

					expect( fileExists( larFile ) ).toBeTrue( "archive for mapping [#virtual#] should have been created (iteration #i#)" );

					admin action="removeMapping" type="web" password="#variables.adminPassword#" virtual="#virtual#";
				}
			});

		});
	}

	private boolean function _hasMapping( required string virtual ) {
		admin action="getMappings" type="web" password="#variables.adminPassword#" returnVariable="local.mappings";
		loop query="#mappings#" {
			if ( mappings.virtual == arguments.virtual ) return true;
		}
		return false;
	}

}
