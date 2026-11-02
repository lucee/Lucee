// LDEV-6148 admin action "updateExtensions": update several extensions in one call (used by the "update all" button in the Admin)
component extends="org.lucee.cfml.test.LuceeTestCase" labels="extension" {

	variables.GROUP = "org.lucee";
	variables.ARTIFACT = "argon2-extension";
	variables.OLD = "1.0.0.10";
	variables.NEW = "1.0.0.11";

	function beforeAll() {
		variables.pw = request.SERVERADMINPASSWORD;
		variables.installedBefore = installedVersion();
	}

	function afterAll() {
		// leave the server like we found it
		if ( len( variables.installedBefore ) ) update( variables.installedBefore );
		else remove();
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6148 admin action updateExtensions", function() {

			it( title="updates all given extensions and reports the result for every one", body=function( currentSpec ) {
				update( OLD );
				expect( installedVersion() ).toBe( OLD );

				var result = updateExtensions( [ entry( NEW ) ] );

				expect( result.recordcount ).toBe( 1 );
				expect( result.success ).toBeTrue();
				expect( result.message ).toBe( "" );
				expect( result.groupId ).toBe( GROUP );
				expect( result.artifactId ).toBe( ARTIFACT );
				expect( result.version ).toBe( NEW );
				expect( result.name ).notToBeEmpty();
				expect( installedVersion() ).toBe( NEW );
			});

			it( title="a failing extension is reported and does not stop the other ones", body=function( currentSpec ) {
				update( OLD );
				expect( installedVersion() ).toBe( OLD );

				var result = updateExtensions( [
					{ groupId: GROUP, artifactId: "ldev6148-does-not-exist-extension", version: "1.0.0.1" }, // fails before the good one
					entry( NEW ),
					entry( "9.9.9.9" ) // fails after the good one, this version does not exist
				] );

				expect( result.recordcount ).toBe( 3 );

				// one result per entry, in the order of the entries
				expect( result.artifactId[ 1 ] ).toBe( "ldev6148-does-not-exist-extension" );
				expect( result.success[ 1 ] ).toBeFalse();
				expect( result.message[ 1 ] ).notToBeEmpty();

				expect( result.artifactId[ 2 ] ).toBe( ARTIFACT );
				expect( result.success[ 2 ] ).toBeTrue();
				expect( result.message[ 2 ] ).toBe( "" );

				expect( result.artifactId[ 3 ] ).toBe( ARTIFACT );
				expect( result.version[ 3 ] ).toBe( "9.9.9.9" );
				expect( result.success[ 3 ] ).toBeFalse();
				expect( result.message[ 3 ] ).notToBeEmpty();

				// the good extension was updated, the failed update did not touch it
				expect( installedVersion() ).toBe( NEW );
			});

			it( title="an entry without a version is reported as failed", body=function( currentSpec ) {
				var result = updateExtensions( [ { groupId: GROUP, artifactId: ARTIFACT } ] );
				expect( result.recordcount ).toBe( 1 );
				expect( result.success ).toBeFalse();
				expect( result.message ).toInclude( "version" );
			});

			it( title="an entry without id and without groupId/artifactId is reported as failed", body=function( currentSpec ) {
				var result = updateExtensions( [ { version: NEW } ] );
				expect( result.recordcount ).toBe( 1 );
				expect( result.success ).toBeFalse();
				expect( result.message ).notToBeEmpty();
			});

			it( title="an empty list returns an empty result", body=function( currentSpec ) {
				var result = updateExtensions( [] );
				expect( result.recordcount ).toBe( 0 );
			});

			it( title="the action is available in the Administrator component", body=function( currentSpec ) {
				update( OLD );
				var adm = new org.lucee.cfml.Administrator( "server", variables.pw );
				var result = adm.updateExtensions( [ entry( NEW ) ] );
				expect( result.recordcount ).toBe( 1 );
				expect( result.success ).toBeTrue();
				expect( installedVersion() ).toBe( NEW );
			});

			it( title="the action requires the admin password", body=function( currentSpec ) {
				expect( function() {
					admin action="updateExtensions" type="server" password="not-the-password-#createUUID()#" extensions=[ entry( NEW ) ] returnVariable="local.r";
				}).toThrow();
			});

		});
	}

	private struct function entry( required string version ) {
		return { groupId: GROUP, artifactId: ARTIFACT, version: arguments.version };
	}

	private query function updateExtensions( required array extensions ) {
		admin action="updateExtensions" type="server" password=variables.pw extensions=arguments.extensions returnVariable="local.result";
		return local.result;
	}

	private void function update( required string version ) {
		admin action="updateExtension" type="server" password=variables.pw groupId=GROUP artifactId=ARTIFACT version=arguments.version;
	}

	private void function remove() {
		admin action="getRHExtensions" type="server" password=variables.pw returnVariable="local.exts";
		loop query=exts {
			if ( exts.groupId == GROUP && exts.artifactId == ARTIFACT ) {
				admin action="removeRHExtension" type="server" password=variables.pw id=exts.id;
			}
		}
	}

	private string function installedVersion() {
		admin action="getRHExtensions" type="server" password=variables.pw returnVariable="local.exts";
		loop query=exts {
			if ( exts.groupId == GROUP && exts.artifactId == ARTIFACT ) return exts.version;
		}
		return "";
	}

}
