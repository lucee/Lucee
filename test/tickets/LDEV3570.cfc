component extends="org.lucee.cfml.test.LuceeTestCase" labels="logs" {

	function beforeAll() {
		variables.logFile = expandPath( "{lucee-config}" ) & "/logs/application.log";
	}

	function run( testResults, testBox ) {
		describe( "LDEV-3570: exception handled by Application.cfc onError", function() {

			it( title="is not logged as ERROR to application.log", skip=areLogsSentToConsole(), body=function( currentSpec ) {
				var marker = "LDEV3570-handled-" & createUUID();
				var res = callRequest( marker );
				expect( res.filecontent ).toBe( "handled" );
				waitForLogFlush();
				expect( readLog() ).notToInclude( marker );
			});

			it( title="the return value of onError has no meaning", skip=areLogsSentToConsole(), body=function( currentSpec ) {
				var marker = "LDEV3570-returnfalse-" & createUUID();
				var res = callRequest( marker, "returnFalse=true" );
				expect( res.filecontent ).toBe( "handled" );
				waitForLogFlush();
				expect( readLog() ).notToInclude( marker );
			});

			it( title="is still logged to application.log if onError itself throws", skip=areLogsSentToConsole(), body=function( currentSpec ) {
				var marker = "LDEV3570-throws-" & createUUID();
				callRequest( marker, "onErrorThrows=true" );
				var found = false;
				loop times=50 {
					if ( readLog().contains( marker ) ) {
						found = true;
						break;
					}
					sleep( 100 );
				}
				expect( found ).toBeTrue( "the exception must be logged when onError throws" );
			});

		});
	}

	// the request has to go through the engine's servlet handling (CFMLEngine.serviceCFML), where the exception is logged
	// an _InternalRequest does not
	private struct function callRequest( required string marker, string extraQuery="" ) {
		var engine = createObject( "java", "lucee.loader.engine.CFMLEngineFactory" ).getInstance();
		var creation = engine.getCreationUtil();
		var servlet = getPageContext().getServlet();
		var root = createObject( "java", "java.io.File" ).init( expandPath( "/" ) );
		var query = "marker=#arguments.marker#";
		if ( len( arguments.extraQuery ) ) query &= "&" & arguments.extraQuery;
		var out = createObject( "java", "java.io.ByteArrayOutputStream" ).init();
		var req = creation.createHttpServletRequest( root, "localhost", "/test/tickets/LDEV3570/index.cfm", query, [], {}, {}, {}, nullValue() );
		var rsp = creation.createHttpServletResponse( out );
		// own thread, the engine releases the thread's PageContext when the request ends
		var thread_name = "LDEV3570-" & createUUID();
		thread name=thread_name servlet=servlet req=req rsp=rsp engine=engine {
			attributes.engine.serviceCFML( attributes.servlet, attributes.req, attributes.rsp );
		}
		thread action="join" name=thread_name timeout=30000;
		if ( !isNull( cfthread[ thread_name ].error ) ) throw( object=cfthread[ thread_name ].error );
		return { "filecontent": out.toString( "UTF-8" ) };
	}

	private string function readLog() {
		return fileExists( variables.logFile ) ? fileRead( variables.logFile ) : "";
	}

	// make sure an entry written after the request would be visible by writing and waiting for a canary entry
	private void function waitForLogFlush() {
		var canary = "LDEV3570-canary-" & createUUID();
		cflog( text=canary, type="error", application=true );
		loop times=50 {
			if ( readLog().contains( canary ) ) return;
			sleep( 100 );
		}
		fail( "log canary did not show up in application.log" );
	}

	// if logs are sent to the console, no log files are created and these tests can't run
	private function areLogsSentToConsole() {
		if ( getSystemPropOrEnvVar( "lucee_logging_force_appender" ) eq "console" ) return true;
		// the test runner (ant/run-testcases.xml) can also configure the application log with a console appender
		admin action="getLogSettings" type="server" password="#request.SERVERADMINPASSWORD#" returnVariable="local.logSettings";
		loop query=logSettings {
			if ( logSettings.name eq "application" ) return logSettings.appenderClass contains "ConsoleAppender";
		}
		return false;
	}

}
