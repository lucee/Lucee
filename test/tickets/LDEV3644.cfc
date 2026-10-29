component extends="org.lucee.cfml.test.LuceeTestCase" labels="gateway,admin" {

	function beforeAll() {
		variables.password = request.WEBADMINPASSWORD;
		variables.virtual = "/ldev3644-gateway";
		variables.gatewayId = "ldev3644-failing";

		cleanup();

		admin
			action="updateMapping"
			type="web"
			password="#variables.password#"
			virtual="#variables.virtual#"
			physical="#getDirectoryFromPath( getCurrentTemplatePath() )#LDEV3644/"
			toplevel="true"
			archive=""
			primary="physical"
			trusted="no";

		// manual startup, so the gateway only starts when the test starts it
		admin
			action="updateGatewayEntry"
			type="web"
			password="#variables.password#"
			id="#variables.gatewayId#"
			class=""
			cfcPath="ldev3644-gateway.FailingGateway"
			listenerCfcPath=""
			startupMode="manual"
			custom="#{}#";
	}

	function afterAll() {
		cleanup();
		structDelete( server, "ldev3644FailStart" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-3644: event gateway keeps its last exception", function() {

			it( title="a gateway that fails to start exposes the exception in getGatewayEntry", body=function( currentSpec ) {
				server.ldev3644FailStart = true;
				startGateway();
				var entry = waitFor( function( entry ) {
					return entry.state == "failed" && len( entry.lastError );
				} );

				expect( entry.state ).toBe( "failed" );
				expect( entry.lastError ).toInclude( "LDEV-3644 gateway start failure" );
				expect( entry.lastError ).toInclude( "LDEV-3644 failure detail" );
			});

			it( title="the last error is also part of getGatewayEntries", body=function( currentSpec ) {
				server.ldev3644FailStart = true;
				startGateway();
				waitFor( function( entry ) {
					return entry.state == "failed" && len( entry.lastError );
				} );

				admin action="getGatewayEntries" type="web" password="#variables.password#" returnVariable="local.entries";
				expect( listFindNoCase( entries.columnList, "lastError" ) ).toBeGT( 0 );
				var found = false;
				loop query="entries" {
					if ( entries.id == variables.gatewayId ) {
						found = true;
						expect( entries.state ).toBe( "failed" );
						expect( entries.lastError ).toInclude( "LDEV-3644 gateway start failure" );
					}
				}
				expect( found ).toBeTrue();
			});

			it( title="a successful start clears the last error", body=function( currentSpec ) {
				server.ldev3644FailStart = true;
				startGateway();
				waitFor( function( entry ) {
					return entry.state == "failed" && len( entry.lastError );
				} );

				server.ldev3644FailStart = false;
				startGateway();
				var entry = waitFor( function( entry ) {
					return entry.state == "running";
				} );

				expect( entry.state ).toBe( "running" );
				expect( entry.lastError ).toBe( "" );
			});

			it( title="a gateway that never failed has no last error", body=function( currentSpec ) {
				server.ldev3644FailStart = false;
				startGateway();
				var entry = waitFor( function( entry ) {
					return entry.state == "running";
				} );

				expect( entry.lastError ).toBe( "" );
			});

		});
	}

	private function startGateway() {
		admin
			action="gateway"
			type="web"
			password="#variables.password#"
			id="#variables.gatewayId#"
			gatewayAction="start";
	}

	private struct function getEntry() {
		admin
			action="getGatewayEntry"
			type="web"
			password="#variables.password#"
			id="#variables.gatewayId#"
			returnVariable="local.entry";
		return entry;
	}

	/**
	 * the gateway starts in its own thread, so poll until the condition is met (or fail after ~15 seconds)
	 */
	private struct function waitFor( required function condition ) {
		var entry = getEntry();
		var tries = 0;
		while ( !arguments.condition( entry ) && tries++ < 60 ) {
			sleep( 250 );
			entry = getEntry();
		}
		return entry;
	}

	private void function cleanup() {
		try {
			admin action="gateway" type="web" password="#variables.password#" id="#variables.gatewayId#" gatewayAction="stop";
		}
		catch ( any e ) {}
		try {
			admin action="removeGatewayEntry" type="web" password="#variables.password#" id="#variables.gatewayId#";
		}
		catch ( any e ) {}
		try {
			admin action="removeMapping" type="web" password="#variables.password#" virtual="#variables.virtual#";
		}
		catch ( any e ) {}
	}

}
