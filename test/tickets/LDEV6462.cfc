component extends="org.lucee.cfml.test.LuceeTestCase" labels="extension,mappings" {

	variables.SCHEDULER_CLASSIC = "97EB5427-F051-4684-91EBA6DBB5C5203F";

	function beforeAll() {
		variables.adminPw = request.SERVERADMINPASSWORD;
		variables.cfConfigFile = expandPath( "{lucee-config}/.CFConfig.json" );
		// keep a copy of the extension so afterAll can put it back
		var lex = directoryList( expandPath( "{lucee-config}/extensions/available" ), false, "path", "*scheduler-classic*.lex" );
		if ( arrayLen( lex ) ) {
			variables.lexBackup = getTempDirectory() & "LDEV6462-" & listLast( lex[ 1 ], "/\" );
			fileCopy( lex[ 1 ], variables.lexBackup );
		}
	}

	function afterAll() {
		if ( !isInstalled() && !isNull( variables.lexBackup ) && fileExists( variables.lexBackup ) ) {
			admin action="updateRHExtension" type="server" password=variables.adminPw source=variables.lexBackup;
		}
		// restore the admin mapping in case the uninstall removed it
		if ( !hasPersistedMapping( "/lucee/admin" ) ) {
			admin action="updateMapping" type="server" password=variables.adminPw
				virtual="/lucee/admin" physical="{lucee-config}/context/admin" archive="{lucee-config}/context/lucee-admin.lar"
				primary="physical" inspect="once" toplevel=true;
		}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6462 uninstalling the Classic Scheduler extension", function() {

			it( title="does not remove the /lucee/admin mapping from .CFConfig.json", skip=!isInstalled(), body=function( currentSpec ) {
				expect( hasPersistedMapping( "/lucee/admin" ) ).toBeTrue( "/lucee/admin mapping missing before the uninstall" );

				admin action="removeRHExtension" type="server" password=variables.adminPw id=SCHEDULER_CLASSIC;

				expect( isInstalled() ).toBeFalse();
				// the running config keeps the mapping until the next restart, the persisted config is what breaks the Admin
				expect( hasPersistedMapping( "/lucee/admin" ) ).toBeTrue( "/lucee/admin mapping was removed from .CFConfig.json together with the Classic Scheduler extension" );
			});

		});
	}

	public boolean function isInstalled() {
		admin action="getRHExtensions" type="server" password=request.SERVERADMINPASSWORD returnVariable="local.exts";
		return queryFilter( local.exts, function( row ) { return row.id == SCHEDULER_CLASSIC; } ).recordCount > 0;
	}

	private boolean function hasPersistedMapping( required string virtual ) {
		var cfg = deserializeJSON( fileRead( variables.cfConfigFile ) );
		var mappings = cfg.mappings ?: {};
		return structKeyExists( mappings, arguments.virtual ) || structKeyExists( mappings, arguments.virtual & "/" );
	}
}
