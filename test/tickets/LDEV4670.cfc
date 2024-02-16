component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {

	function isMySqlNotSupported() {
		return true; // disable for the moment, because it still fails and blocks the build
		var mySql = server.getDatasource("mysql");
		return isEmpty( mysql );
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-4670", body=function() {
			it( title='Checking datasource session expiry, timezone UTC',skip=isMySqlNotSupported(),body=function( currentSpec ) {
				var remainingSessions = testSessionTimezone( "UTC", "datasource" );
				dumpResult( remainingSessions, "UTC" );
				expect( remainingSessions.recordcount ).toBe( 0 );
			});

			it( title='Checking datasource session expiry, timezone PDT -7',skip=isMySqlNotSupported(),body=function( currentSpec ) {
				var remainingSessions = testSessionTimezone( "PDT", "datasource" );
				dumpResult( remainingSessions, "PDT" );
				expect( remainingSessions.recordcount ).toBe( 0 );
			});

			it( title='Checking datasource session expiry, timezone AEST +10',skip=isMySqlNotSupported(),body=function( currentSpec ) {
				var remainingSessions = testSessionTimezone( "AEST", "datasource" );
				dumpResult( remainingSessions, "AEST" );

				expect( remainingSessions.recordcount ).toBe( 0 );
			});

			it( title='Checking memory session expiry, timezone UTC', body=function( currentSpec ) {
				var remainingSessions = testSessionTimezone( "UTC", "memory" );
				dumpResult( remainingSessions, "UTC" );
				expect( remainingSessions.recordcount ).toBe( 0 );
			});

			it( title='Checking memory session expiry, timezone PDT -7', body=function( currentSpec ) {
				var remainingSessions = testSessionTimezone( "PDT", "memory" );
				dumpResult( remainingSessions, "PDT" );
				expect( remainingSessions.recordcount ).toBe( 0 );
			});

			it( title='Checking memory session expiry, timezone Europe/Berlin +1', body=function( currentSpec ) {
				var remainingSessions = testSessionTimezone( "Europe/Berlin", "memory" );
				dumpResult( remainingSessions, "Europe/Berlin" );

				expect( remainingSessions.recordcount ).toBe( 0 );
			});

			it( title='Checking memory session expiry, timezone AEST +10', body=function( currentSpec ) {
				var remainingSessions = testSessionTimezone( "AEST", "memory" );
				dumpResult( remainingSessions, "AEST" );

				expect( remainingSessions.recordcount ).toBe( 0 );
			});

		});
	}

	private query function testSessionTimezone( required string timezone, required string sessionStorageType ){
		systemOutput( "  ", true );

		systemOutput(">>>>> testSessionTimezone #arguments.timezone# / #arguments.sessionStorageType# ----- ", true);
		if ( arguments.sessionStorageType == "datasource" ) {
			var result = testSession("first cleanout sessions table",{
				action: "purge",
				sessionEnable: false,
				timezone: arguments.timezone,
				sessionStorageType: arguments.sessionStorageType
			});
		}
		systemoutput("purgeExpiredSessions", true);
		admin
			action="purgeExpiredSessions"
			type="server"
			password="#request.SERVERADMINPASSWORD#";

		if ( arguments.sessionStorageType == "datasource" ) {
			result = testSession("dump sessions table",{
				action: "dumpDatabaseSessions",
				sessionEnable: false,
				timezone: arguments.timezone,
				sessionStorageType: arguments.sessionStorageType
			});
			expect( deserializeJson( result.filecontent, false ).recordcount ).toBe( 0 );
		}

		result = testSession("create a session", {
			timezone: arguments.timezone,
			sessionStorageType: arguments.sessionStorageType,
			action: "createSession"
		});

		var currentSession = duplicate( result.session );
		if ( arguments.sessionStorageType == "datasource" ) {
			result = testSession("dump sessions table, should have 1 session", {
				action: "dumpDatabaseSessions",
				sessionEnable: false,
				timezone: arguments.timezone,
				sessionStorageType: arguments.sessionStorageType
			});
			expect( deserializeJson( result.filecontent, false ).recordcount ).toBe( 1 );
		}

		result = testSession("check that the current session is still active", {
				action: "checkSession",
				sessionEnable: true,
				timezone: arguments.timezone,
				sessionStorageType: arguments.sessionStorageType
			},
			currentSession // pass in previous session
		);
		expect( result.session.cfid ).toBe( currentSession.cfid );
		expect( result.session.requestCount ).toBe( 2 );

		// now let the session expire, session expiry is 1s
		sleep( 1001 );

		systemoutput("purgeExpiredSessions", true);
		admin
			action="purgeExpiredSessions"
			type="server"
			password="#request.SERVERADMINPASSWORD#";

		result = testSession("dump sessions from memory, should have 0 sessions", {
			action: "dumpMemorySessions",
			sessionEnable: false,
			timezone: arguments.timezone,
			sessionStorageType: arguments.sessionStorageType
		});

		var remainingSessionCount = deserializeJson( result.filecontent, false );

		expect ( remainingSessionCount.recordcount ).toBe( 0 );

		result = testSession("check that the current session was recreated, requestcount 1", {
				action: "checkSession",
				sessionEnable: true,
				timezone: arguments.timezone,
				sessionStorageType: arguments.sessionStorageType
			},
			currentSession // pass in previous session
		);

		// expect( result.session.cfid ).notToBe( currentSession.cfid ); // doesn't work session gets recreated with the same cfid?
		expect( result.session.requestCount ).toBe( 1 );

		if ( arguments.sessionStorageType == "datasource" ) {
			result = testSession("dump sessions table, should have 0 sessions", {
				action: "dumpDatabaseSessions",
				sessionEnable: false,
				timezone: arguments.timezone,
				sessionStorageType: arguments.sessionStorageType
			});
			remainingSessionCount = deserializeJson( result.filecontent, false );
		}

		return remainingSessionCount;
	}

	private any function testSession ( required string name, required struct args, struct session={} ){
		systemOutput( "----  #arguments.name# ----- ", true );
		var uri = createURI("LDEV4670");
		var cookies = {};
		if ( structCount( arguments.session ) ){
			//systemOutput( arguments.session, true );
			cookies = {
				cfid:    arguments.session.cfid,
				cftoken: arguments.session.cftoken
			};
			//systemOutput( "cookies: #cookies.toJson()#", true );
		}

		var result = _InternalRequest(
			template:"#uri#/ldev4670.cfm",
			form: arguments.args,
			cookies: cookies
		);

		//systemOutput(result, true);
		systemOutput( deserializeJson( result.filecontent ), true );
		//systemOutput(deserializeJson(result.filecontent, false), true);
		return result;
	}

	private string function epochToDate( epoch ){
		return createObject("java", "java.text.SimpleDateFormat").init("yyyy-MM-dd HH:mm:ss:SSS")
			.format( createObject("java", "java.util.Date").init( arguments.epoch * 1 ) );
	}

	private void function dumpResult( remainingSessions, timezone ){
		systemOutput("", true);
		if ( remainingSessions.recordcount eq 0 )
			return;
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