component extends="org.lucee.cfml.test.LuceeTestCase" labels="schedule" {

	function beforeAll() {
		variables.urlPath = "http://" & cgi.SERVER_NAME & createURI( "LDEV5737/index.cfm" );
		variables.taskNames = [ "LDEV-5737-expired", "LDEV-5737-oneoff" ];
		cleanup();
	}

	function afterAll() {
		cleanup();
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-5737 one off scheduled tasks", body=function() {

			it( title="an expired one off task doesn't start a thread", body=function( currentSpec ) {
				var past = dateAdd( "d", -1, now() );
				cfschedule(
					action="update",
					task="LDEV-5737-expired",
					url=variables.urlPath,
					interval="once",
					startdate=dateFormat( past, "yyyy-mm-dd" ),
					starttime=timeFormat( past, "HH:mm:ss" )
				);
				var task = getTask( "LDEV-5737-expired" );
				sleep( 500 );
				// the controller calls this every minute
				getPageContext().getConfig().getScheduler().startIfNecessary();
				sleep( 500 );
				var thread = getTaskThread( task );
				// either no thread at all, or one that was never started
				expect( isNull( thread ) || thread.getState().name() == "NEW" ).toBeTrue(
					"thread state: " & ( isNull( thread ) ? "null" : thread.getState().name() ) );
			});

			it( title="a one off task isn't restarted after it ran", body=function( currentSpec ) {
				var start = dateAdd( "s", 2, now() );
				cfschedule(
					action="update",
					task="LDEV-5737-oneoff",
					url=variables.urlPath,
					interval="once",
					startdate=dateFormat( start, "yyyy-mm-dd" ),
					starttime=timeFormat( start, "HH:mm:ss" )
				);
				var task = getTask( "LDEV-5737-oneoff" );
				var first = getTaskThread( task );
				expect( isNull( first ) ).toBeFalse( "a thread should be started for a one off task in the future" );
				// wait until the single execution is done and the thread has ended
				var waited = 0;
				while ( first.isAlive() && waited < 15000 ) {
					sleep( 250 );
					waited += 250;
				}
				expect( first.isAlive() ).toBeFalse( "thread should end after the single execution" );

				getPageContext().getConfig().getScheduler().startIfNecessary();
				var second = getTaskThread( task );
				expect( second.equals( first ) ).toBeTrue( "the thread was restarted (state of the new thread: " & second.getState().name() & ")" );
				expect( second.isAlive() ).toBeFalse();
			});

		});
	}

	private function getTask( required string name ) {
		return getPageContext().getConfig().getScheduler().getScheduleTask( arguments.name );
	}

	private function getTaskThread( required task ) {
		var field = arguments.task.getClass().getDeclaredField( "thread" );
		field.setAccessible( true );
		return field.get( arguments.task );
	}

	private function cleanup() {
		cfschedule( action="list", returnvariable="local.tasks" );
		loop query=local.tasks {
			if ( arrayContainsNoCase( variables.taskNames, local.tasks.task ) ) {
				cfschedule( action="delete", task=local.tasks.task );
			}
		}
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}
}
