component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "Test suite for LDEV-6455", function() {

			it( title="a spooled task contributed by an extension can be read back", body = function( currentSpec ) {
				var subject = "LDEV6455-" & createUUID();

				// sendTime is a day out, so the task is stored but never sent and no mail server is needed
				mail to="receiver@lucee.org" from="sender@lucee.org" subject=subject
						server="localhost" port=25 spoolEnable=true sendTime=dateAdd( "d", 1, now() ) {
					echo( "LDEV-6455" );
				}

				// listing reads every task back from disk
				admin action="getSpoolerTasks" type="web" password=server.WEBADMINPASSWORD
						startrow="1" maxrow="1000" returnVariable="local.tasks";

				var mine = queryFilter( local.tasks, function( row ) {
					return row.name == subject;
				} );

				expect( mine.recordCount ).toBe( 1 );
				expect( mine.type ).toBe( "mail" );

				admin action="removeSpoolerTask" type="web" password=server.WEBADMINPASSWORD id=mine.id;
			});

		});
	}
}
