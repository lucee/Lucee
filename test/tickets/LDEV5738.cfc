component extends="org.lucee.cfml.test.LuceeTestCase" labels="schedule" {

	function beforeAll() {
		variables.taskName = "LDEV5738-" & createUUID();
	}

	function afterAll() {
		try {
			schedule action="delete" task=variables.taskName;
		}
		catch ( any e ) {}
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-5738 autoDelete via cfadmin action=schedule", body=function() {

			it( title="autoDelete=true is stored", body=function( currentSpec ) {
				updateTask( autoDelete=true );
				expect( getTask().autoDelete ).toBeTrue();
			});

			it( title="autoDelete=false is stored", body=function( currentSpec ) {
				updateTask( autoDelete=false );
				expect( getTask().autoDelete ).toBeFalse();
			});

			it( title="autoDelete defaults to false", body=function( currentSpec ) {
				updateTask( autoDelete=true );
				updateTask();
				expect( getTask().autoDelete ).toBeFalse();
			});
		});
	}

	private function updateTask( autoDelete ) {
		var attrs = {
			action: "schedule",
			type: "server",
			password: server.SERVERADMINPASSWORD,
			scheduleAction: "update",
			task: variables.taskName,
			url: "http://#cgi.server_name#/ldev5738.cfm",
			interval: "daily",
			startDate: dateFormat( now() ),
			startTime: timeFormat( now() )
		};
		if ( !isNull( arguments.autoDelete ) ) attrs.autoDelete = arguments.autoDelete;
		admin attributeCollection=attrs;
	}

	private struct function getTask() {
		schedule action="list" returnvariable="local.tasks";
		var qry = queryFilter( tasks, function( row ) {
			return row.task == variables.taskName;
		});
		expect( qry.recordCount ).toBe( 1 );
		return queryGetRow( qry, 1 );
	}

}
