component extends="org.lucee.cfml.test.LuceeTestCase" labels="mysql" {

	function isMySqlNotSupported() {
		var mySql = server.getDatasource("mysql");
		return isEmpty( mysql );
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-4670", body=function() {

			it( title='Checking datasource session expiry, timezone UTC',skip=isMySqlNotSupported(),body=function( currentSpec ) {
				var remainingSessions = testSessionTimezone( "UTC" );
				dumpResult( remainingSessions, "UTC" );
				expect( remainingSessions.recordcount ).toBe( 0 );
			});

			it( title='Checking datasource session expiry, timezone PDT -7',skip=isMySqlNotSupported(),body=function( currentSpec ) {
				var remainingSessions = testSessionTimezone( "PDT" );
				dumpResult( remainingSessions, "PDT" );
				expect( remainingSessions.recordcount ).toBe( 0 );
			});

			it( title='Checking datasource session expiry, timezone AEST +10',skip=isMySqlNotSupported(),body=function( currentSpec ) {
				var remainingSessions = testSessionTimezone( "AEST" );
				dumpResult( remainingSessions, "AEST" );

				expect( remainingSessions.recordcount ).toBe( 0 );
			});

		});
	}

	private query function testSessionTimezone( required string timezone ){
		var result = testSession("first cleanout sessions table",{
			action: "purge",
			sessionEnable: false,
			timezone: arguments.timezone
		});

		admin
			action="purgeExpiredSessions"
			type="server"
			password="#request.SERVERADMINPASSWORD#";

		result = testSession("dump sessions table",{
			action: "dump",
			sessionEnable: false,
			timezone: arguments.timezone
		});

		expect( deserializeJson( result.filecontent, false ).recordcount ).toBe( 0 );

		result = testSession("create a session", {
			timezone: arguments.timezone
		});

		result = testSession("dump sessions table, should have 1 session",{
			action: "dump",
			sessionEnable: false,
			timezone: arguments.timezone
		});

		expect( deserializeJson( result.filecontent, false ).recordcount ).toBe( 1 );

		sleep( 2001 ); // session expiry is 1s

		admin
			action="purgeExpiredSessions"
			type="server"
			password="#request.SERVERADMINPASSWORD#";

		result = testSession("dump sessions table, should have 0 sessions",{
			action: "dump",
			sessionEnable: false,
			timezone: arguments.timezone
		});

		var remainingSessionCount = deserializeJson( result.filecontent, false );

		return remainingSessionCount;
	}

	private any function testSession ( required string name, required struct args ){
		var uri = createURI("LDEV4670");
		var result = _InternalRequest(
			template:"#uri#/ldev4670.cfm",
			form: arguments.args
		);
		//systemOutput("----  #arguments.name# / #args.timezone# ----- ", true);
		//systemOutput(result, true);
		//systemOutput(deserializeJson(result.filecontent), true);
		//systemOutput(deserializeJson(result.filecontent, false), true);
		return result;
	}

	private string function epochToDate( epoch ){
		return createObject("java", "java.text.SimpleDateFormat").init("yyyy-MM-dd HH:mm:ss:SSS")
			.format( createObject("java", "java.util.Date").init( arguments.epoch * 1 ) );
	}

	private void function dumpResult( remainingSessions, timezone ){
		systemOutput("", true);
		systemOutput( "#timezone# has #remainingSessions.recordcount# sessions, expires: "
				& epochToDate( remainingSessions.expires )
				& ", now: #dateTimeFormat(now(), " yyyy-mm-dd HH:nn:ss:LLL", "UTC" )#",
			 true );
		var epoch = dateTimeFormat( now(), 'epochms' );
		if ( len( remainingSessions.expires ) )
			systemOutput( "expires: " & remainingSessions.expires & ", now: #epoch#, diff: #epoch-remainingSessions.expires#",  true );

	}

	// Private functions
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}