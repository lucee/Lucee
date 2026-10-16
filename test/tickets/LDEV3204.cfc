/* LDEV-3204: this.mailservers "username"/"password" keys were reported as case sensitive (unquoted keys become upper case).
   Needs a fake SMTP sink on 127.0.0.1:2526 that advertises AUTH and logs the credentials it receives.
   Set MAIL_SINK_TLS_LOG to the sink session log; the spec is skipped when it is not set, so CI stays green. */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {

	variables.authLog = server.system.environment.MAIL_SINK_TLS_LOG ?: "";

	function run( testResults, testBox ) {
		describe( "LDEV-3204 this.mailservers key case", function() {
			it( title="unquoted username/password keys in this.mailservers are used for SMTP AUTH", skip=noSink(), body=function( currentSpec ) {
				var off = len( fileRead( variables.authLog, "utf-8" ) );
				var res = _internalRequest( template=createURI( "LDEV3204" ) & "/index.cfm", url={ id: createUUID() } ).filecontent.trim();
				sleep( 300 );
				var log = mid( fileRead( variables.authLog, "utf-8" ), off + 1, 100000 );
				var r = deserializeJSON( res );
				expect( r.err ).toBe( "" );
				expect( log ).toInclude( "MixedCaseUser" );
				expect( log ).toInclude( "MixedCasePw" );
			});
		});
	}

	private boolean function noSink() {
		return !len( variables.authLog ) || !fileExists( variables.authLog );
	}

	private string function createURI( string calledName ) {
		return getDirectoryFromPath( contractPath( getCurrentTemplatePath() ) ) & calledName;
	}
}
