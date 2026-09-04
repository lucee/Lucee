component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "Test suite for LDEV-6455", function() {

			it( title="a spooled task contributed by an extension can be read back", body = function( currentSpec ) {
				var subject = "LDEV6455-" & createUUID();

				// Queue a mail without ever sending it: the send time is a day out, so the spooler
				// leaves the task alone and this test needs no mail server. What matters is that
				// the task class comes from the mail extension rather than from the core.
				mail to="receiver@lucee.org" from="sender@lucee.org" subject=subject
						server="localhost" port=25 spoolEnable=true sendTime=dateAdd( "d", 1, now() ) {
					echo( "LDEV-6455" );
				}

				// Listing reads every task back from disk. A task whose class the core class
				// loader cannot see failed to deserialize and was then deleted, so the queued
				// mail disappeared without ever being sent and without an error to the caller.
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
