component extends="org.lucee.cfml.test.LuceeTestCase" labels="gateway,log" {

	function beforeAll() {
		if ( !structKeyExists( request, "WebAdminPassword" ) ) request.WebAdminPassword = "password";
		variables.pw = request.WebAdminPassword;
		variables.ids = [];
	}

	function afterAll() {
		for ( var id in variables.ids ) {
			try {
				admin action="gateway" type="web" password="#variables.pw#" id="#id#" gatewayAction="stop";
			}
			catch ( any e ) {
				// not running
			}
			admin action="removeGatewayEntry" type="web" password="#variables.pw#" id="#id#";
		}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-3645 gateway log entries for a failing gateway CFC name the template and line", function() {

			it( title="syntax error in the gateway CFC", body=function( currentSpec ) {
				var id = addGateway( "Syntax" );
				// reading the state of the gateway loads the CFC
				admin action="getGatewayEntries" type="web" password="#variables.pw#" returnVariable="local.entries";
				checkLog( id, "Syntax", "syntax" );
			});

			it( title="error in a function of the gateway CFC while reading the state", body=function( currentSpec ) {
				var id = addGateway( "Runtime" );
				admin action="getGatewayEntries" type="web" password="#variables.pw#" returnVariable="local.entries";
				checkLog( id, "Runtime", "state" );
			});

			it( title="error in a function of the gateway CFC while starting the gateway", body=function( currentSpec ) {
				var id = addGateway( "Runtime" );
				try {
					admin action="gateway" type="web" password="#variables.pw#" id="#id#" gatewayAction="start";
				}
				catch ( any e ) {
					// the failure is logged by the gateway, nothing to do here
				}
				checkLog( id, "Runtime", "start" );
			});

		});
	}

	private string function addGateway( required string cfc ) {
		var id = "ldev3645_" & replace( lCase( createUUID() ), "-", "", "all" );
		var cfcPath = "test.tickets.LDEV3645." & arguments.cfc;
		admin action="updateGatewayEntry" type="web" password="#variables.pw#" id="#id#"
			cfcPath="#cfcPath#" listenerCfcPath="#cfcPath#" startupMode="manual" custom={};
		arrayAppend( variables.ids, id );
		return id;
	}

	// the entry for the gateway in the gateway log has to name the CFC file and the line of the error
	private void function checkLog( required string id, required string cfc, required string marker ) {
		var expected = arguments.cfc & ".cfc:" & findMarkerLine( arguments.cfc, arguments.marker );
		var logFile = getGatewayLogFile();
		var found = "";
		var content = "";
		// the log is written asynchronously, so give it some time
		loop times=50 {
			if ( fileExists( logFile ) ) {
				content = fileRead( logFile );
				for ( var line in listToArray( content, chr( 10 ) ) ) {
					if ( find( "Gateway:" & arguments.id, line ) && find( expected, line ) ) {
						found = line;
						break;
					}
				}
			}
			if ( len( found ) ) break;
			sleep( 200 );
		}
		expect( len( found ) ).toBeGT( 0, "no entry for gateway [#arguments.id#] with [#expected#] in [#logFile#]" );
	}

	private numeric function findMarkerLine( required string cfc, required string marker ) {
		var lines = listToArray( fileRead( getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV3645/" & arguments.cfc & ".cfc" ), chr( 10 ), true );
		loop array=lines index="local.i" item="local.line" {
			if ( find( "ERROR:" & arguments.marker, line ) ) return i;
		}
		throw( message="marker [#arguments.marker#] not found in [#arguments.cfc#.cfc]" );
	}

	private string function getGatewayLogFile() {
		admin action="getLogSettings" type="server" password="#request.ServerAdminPassword#" returnVariable="local.logs";
		loop query=logs {
			if ( logs.name == "gateway" ) {
				// the log settings are server wide, so the file is in the server config directory
				return expandPath( replace( logs.appenderArgs.path, "{lucee-config}", "{lucee-server}" ) );
			}
		}
		throw( message="no log settings for the gateway log" );
	}

}
