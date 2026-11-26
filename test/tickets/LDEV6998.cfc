component extends="org.lucee.cfml.test.LuceeTestCase" labels="schedule" {

	function beforeAll() {
		variables.taskName = "test-sched-proxy-" & createUUID();
	}

	function afterAll() {
		try {
			cfschedule( action="delete", task=variables.taskName );
		}
		catch ( any e ) {}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6998 cfschedule proxy credentials", function() {

			it( "action=list returns the saved proxy username and password", function() {
				cfschedule(
					action="update",
					task=variables.taskName,
					url="http://localhost/",
					interval="daily",
					startdate=dateFormat( now() ),
					starttime=timeFormat( now() ),
					proxyserver="proxy.example.com",
					proxyport=8080,
					proxyuser="proxyUserTest",
					proxypassword="proxyPassTest",
					paused=true
				);

				var task = getTask();
				expect( task.recordCount ).toBe( 1 );
				expect( task.proxyserver ).toBe( "proxy.example.com" );
				expect( task.proxyport ).toBe( "8080" );
				expect( task.proxyuser ).toBe( "proxyUserTest" );
				expect( task.proxypassword ).toBe( "proxyPassTest" );
			});

			it( "action=list returns empty proxy credentials when none are set", function() {
				cfschedule(
					action="update",
					task=variables.taskName,
					url="http://localhost/",
					interval="daily",
					startdate=dateFormat( now() ),
					starttime=timeFormat( now() ),
					proxyserver="proxy.example.com",
					proxyport=8080,
					paused=true
				);

				var task = getTask();
				expect( task.recordCount ).toBe( 1 );
				expect( task.proxyserver ).toBe( "proxy.example.com" );
				expect( task.proxyuser ).toBe( "" );
				expect( task.proxypassword ).toBe( "" );
			});

		});
	}

	private query function getTask() {
		cfschedule( action="list", result="local.tasks" );
		return queryExecute(
			"select * from tasks where lower(task) = :task",
			{ task: lCase( variables.taskName ) },
			{ dbtype: "query" }
		);
	}

}
