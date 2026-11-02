// LDEV-1585 extensionInstall(): install an extension at runtime from a script
component extends="org.lucee.cfml.test.LuceeTestCase" labels="extension" {

	variables.GAV = "org.lucee:argon2-extension:1.0.0.11";
	variables.GA = "org.lucee:argon2-extension";
	variables.EXT_ID = "7891D723-8F78-45F5-B7E333A22F8467CA";

	function beforeAll() {
		variables.pw = request.SERVERADMINPASSWORD;
		variables.wasInstalled = isInstalled();
		// start from a clean state
		remove();
	}

	function afterAll() {
		// leave the server like we found it
		if ( !variables.wasInstalled ) remove();
	}

	function run( testResults, testBox ) {
		describe( "LDEV-1585 extensionInstall()", function() {

			it( title="installs an extension by maven coordinate and it is usable immediately", body=function( currentSpec ) {
				expect( isInstalled() ).toBeFalse();
				expect( structKeyExists( getFunctionList(), "generateargon2hash" ) ).toBeFalse();

				var res = extensionInstall( GAV, "", variables.pw );

				expect( res.alreadyInstalled ).toBeFalse();
				expect( res.id ).toBe( EXT_ID );
				expect( res.version ).toBe( "1.0.0.11" );
				expect( res.maven ).toBe( GAV );
				expect( res.name ).notToBeEmpty();
				expect( extensionExists( GAV ) ).toBeTrue();
				expect( isInstalled() ).toBeTrue();
				// the function added by the extension can be used in the same request
				expect( len( generateArgon2Hash( "susi" ) ) ).toBeGT( 0 );
			});

			it( title="installing an installed extension is a noop", body=function( currentSpec ) {
				ensureInstalled();
				var res = extensionInstall( GAV, "", variables.pw );
				expect( res.alreadyInstalled ).toBeTrue();
				expect( res.version ).toBe( "1.0.0.11" );
			});

			it( title="without a version any installed version is good enough", body=function( currentSpec ) {
				ensureInstalled();
				var res = extensionInstall( extension=GA, password=variables.pw );
				expect( res.alreadyInstalled ).toBeTrue();
				expect( res.version ).toBe( "1.0.0.11" );
				expect( res.maven ).toBe( GAV );
			});

			it( title="the version can be passed as second argument", body=function( currentSpec ) {
				remove();
				var res = extensionInstall( GA, "1.0.0.11", variables.pw );
				expect( res.alreadyInstalled ).toBeFalse();
				expect( res.maven ).toBe( GAV );
				expect( extensionExists( GAV ) ).toBeTrue();
			});

			it( title="a version argument that equals the version of the coordinate is fine", body=function( currentSpec ) {
				ensureInstalled();
				var res = extensionInstall( GAV, "1.0.0.11", variables.pw );
				expect( res.version ).toBe( "1.0.0.11" );
			});

			it( title="a version argument that conflicts with the version of the coordinate throws", body=function( currentSpec ) {
				expect( function() {
					extensionInstall( GAV, "1.0.0.10", variables.pw );
				}).toThrow( "expression", "conflicts" );
			});

			it( title="the legacy extension id is still supported", body=function( currentSpec ) {
				remove();
				var res = extensionInstall( EXT_ID, "1.0.0.11", variables.pw );
				expect( res.alreadyInstalled ).toBeFalse();
				expect( res.id ).toBe( EXT_ID );
				expect( extensionExists( GAV ) ).toBeTrue();

				// no version, already installed
				res = extensionInstall( extension=EXT_ID, password=variables.pw );
				expect( res.alreadyInstalled ).toBeTrue();
			});

			it( title="throws for an invalid extension argument", body=function( currentSpec ) {
				expect( function() {
					extensionInstall( "", "", variables.pw );
				}).toThrow( "expression" );
				expect( function() {
					extensionInstall( "no-coordinate-and-no-id", "", variables.pw );
				}).toThrow( "expression" );
				expect( function() {
					extensionInstall( "org.lucee:a:b:c:d:e:f:g:h", "", variables.pw );
				}).toThrow( "expression" );
			});

			it( title="throws for an extension that does not exist", body=function( currentSpec ) {
				expect( function() {
					extensionInstall( "org.lucee:this-extension-does-not-exist:1.0.0", "", variables.pw );
				}).toThrow();
			});

			it( title="respects the write access of the server configuration", body=function( currentSpec ) {
				ensureInstalled();
				// the check happens before anything is looked up or installed
				expect( function() {
					extensionInstall( GAV, "", "this is not the password" );
				}).toThrow( "security" );
			});

			it( title="takes the password from the system property lucee.server.admin.password", body=function( currentSpec ) {
				ensureInstalled();
				var sys = createObject( "java", "java.lang.System" );
				var key = "lucee.server.admin.password";
				var old = sys.getProperty( key );
				try {
					sys.setProperty( key, variables.pw );
					var res = extensionInstall( GAV );
					expect( res.alreadyInstalled ).toBeTrue();
				}
				finally {
					if ( isNull( old ) ) sys.clearProperty( key );
					else sys.setProperty( key, old );
				}
			});

		});
	}

	private boolean function isInstalled() {
		var exts = extensionList();
		loop query=exts {
			if ( exts.id == variables.EXT_ID ) return true;
		}
		return false;
	}

	private void function ensureInstalled() {
		if ( !isInstalled() ) extensionInstall( GAV, "", variables.pw );
	}

	private void function remove() {
		if ( isInstalled() ) {
			admin action="removeRHExtension" type="server" password=variables.pw id=EXT_ID;
		}
	}

}
