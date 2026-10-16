component extends="org.lucee.cfml.test.LuceeTestCase" labels="admin" {

	function beforeAll() {
		variables.adminPassword = request.SERVERADMINPASSWORD ?: server.SERVERADMINPASSWORD;
		variables.adm = new Administrator( "server", variables.adminPassword );
		var security = adm.getDefaultSecurityManager();
		variables.originalAccess = {
			access_read: security.access_read,
			access_write: security.access_write
		};
		variables.physical = getTempDirectory() & "ldev6515/";
		if ( !directoryExists( variables.physical ) ) directoryCreate( variables.physical );
		variables.virtuals = [];
	}

	function afterAll() {
		setAccess( variables.originalAccess.access_read, variables.originalAccess.access_write );
		loop array=variables.virtuals item="local.virtual" {
			try {
				admin action="removeMapping" type="server" password=variables.adminPassword virtual=virtual;
			}
			catch ( any e ) {}
		}
		if ( directoryExists( variables.physical ) ) directoryDelete( variables.physical, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6515 general access open/protected/closed applies to cfadmin in the server context", function() {

			afterEach( function() {
				setAccess( variables.originalAccess.access_read, variables.originalAccess.access_write );
			});

			it( title="read access open: cfadmin works without a password", body=function() {
				setAccess( "open", "protected" );
				expect( readWithPassword( "" ) ).toBeQuery();
				expect( readWithoutPassword() ).toBeQuery();
			});

			it( title="write access open: cfadmin works without a password", body=function() {
				setAccess( "protected", "open" );
				var virtual = newVirtual();
				writeWithPassword( virtual, "" );
				expect( hasMapping( virtual ) ).toBeTrue();
			});

			it( title="read access protected: cfadmin fails without a password", body=function() {
				setAccess( "protected", "protected" );
				expect( function() {
					readWithPassword( "" );
				}).toThrow( regex="read access is protected" );
				expect( function() {
					readWithoutPassword();
				}).toThrow( regex="read access is protected" );
				expect( function() {
					readWithPassword( "invalid_" & createUniqueID() );
				}).toThrow( regex="read access is protected" );
				expect( readWithPassword( variables.adminPassword ) ).toBeQuery();
			});

			it( title="write access protected: cfadmin fails without a password", body=function() {
				setAccess( "protected", "protected" );
				var virtual = newVirtual();
				expect( function() {
					writeWithPassword( virtual, "" );
				}).toThrow( regex="write access is protected" );
				expect( function() {
					writeWithPassword( virtual, "invalid_" & createUniqueID() );
				}).toThrow( regex="write access is protected" );
				expect( hasMapping( virtual ) ).toBeFalse();
				writeWithPassword( virtual, variables.adminPassword );
				expect( hasMapping( virtual ) ).toBeTrue();
			});

			it( title="read access closed: cfadmin fails without a password", body=function() {
				setAccess( "close", "protected" );
				expect( function() {
					readWithPassword( "" );
				}).toThrow();
				expect( function() {
					readWithoutPassword();
				}).toThrow();
				// the server context has no parent context that could open it again, so the admin password still works
				expect( readWithPassword( variables.adminPassword ) ).toBeQuery();
			});

			it( title="write access closed: cfadmin fails without a password", body=function() {
				setAccess( "protected", "close" );
				var virtual = newVirtual();
				expect( function() {
					writeWithPassword( virtual, "" );
				}).toThrow();
				expect( hasMapping( virtual ) ).toBeFalse();
				// the server context has no parent context that could open it again, so the admin password still works
				writeWithPassword( virtual, variables.adminPassword );
				expect( hasMapping( virtual ) ).toBeTrue();
			});

		});
	}

	private function setAccess( required string accessRead, required string accessWrite ) {
		adm.updateDefaultSecurityManager( access_read: arguments.accessRead, access_write: arguments.accessWrite );
		var security = adm.getDefaultSecurityManager();
		expect( security.access_read ).toBe( arguments.accessRead );
		expect( security.access_write ).toBe( arguments.accessWrite );
	}

	private string function newVirtual() {
		var virtual = "/ldev6515_" & lCase( replace( createUUID(), "-", "", "all" ) );
		arrayAppend( variables.virtuals, virtual );
		return virtual;
	}

	private function readWithPassword( required string password ) {
		admin action="getMappings" type="server" password=arguments.password returnVariable="local.mappings";
		return mappings;
	}

	private function readWithoutPassword() {
		admin action="getMappings" type="server" returnVariable="local.mappings";
		return mappings;
	}

	private function writeWithPassword( required string virtual, required string password ) {
		admin action="updateMapping" type="server" password=arguments.password
			virtual=arguments.virtual physical=variables.physical archive="" primary="physical" toplevel="true";
	}

	private boolean function hasMapping( required string virtual ) {
		admin action="getMappings" type="server" password=variables.adminPassword returnVariable="local.mappings";
		loop query=mappings {
			if ( mappings.virtual == arguments.virtual ) return true;
		}
		return false;
	}

}
