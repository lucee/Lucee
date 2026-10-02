component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {

	function run( testResults, testBox ) {
		describe( "Test suite for LDEV-6484", function() {

			it( title="new mail().send() works (script-based mail helper component)", body=function( currentSpec ) {
				var subject = "LDEV6484-" & createUUID();

				// sendTime is a day out, so the task is only spooled and no mail server is needed
				var m = new mail(
					to="receiver@lucee.org",
					from="sender@lucee.org",
					subject=subject,
					body="LDEV-6484",
					server="localhost",
					port=25,
					spoolEnable=true,
					sendTime=dateAdd( "d", 1, now() )
				);
				m.send();

				admin action="getSpoolerTasks" type="server" password=server.SERVERADMINPASSWORD
						startrow="1" maxrow="1000" returnVariable="local.tasks";

				var mine = queryFilter( local.tasks, function( row ) {
					return row.name == subject;
				} );

				expect( mine.recordCount ).toBe( 1 );
				expect( mine.type ).toBe( "mail" );

				admin action="removeSpoolerTask" type="server" password=server.SERVERADMINPASSWORD id=mine.id;
			});

			it( title="new mail() with addParam() and addPart() can be sent", body=function( currentSpec ) {
				var subject = "LDEV6484-parts-" & createUUID();

				var m = new mail();
				m.setTo( "receiver@lucee.org" );
				m.setFrom( "sender@lucee.org" );
				m.setSubject( subject );
				m.setServer( "localhost" );
				m.setPort( 25 );
				// spoolEnable is only an alias of "async" in the mail extension, so pass it as a plain attribute
				m.setAttributes( spoolEnable=true, sendTime=dateAdd( "d", 1, now() ) );
				m.addParam( name="X-LDEV", value="6484" );
				m.addPart( type="text", body="plain LDEV-6484" );
				m.addPart( type="html", body="<b>html LDEV-6484</b>" );
				m.send();

				admin action="getSpoolerTasks" type="server" password=server.SERVERADMINPASSWORD
						startrow="1" maxrow="1000" returnVariable="local.tasks";

				var mine = queryFilter( local.tasks, function( row ) {
					return row.name == subject;
				} );

				expect( mine.recordCount ).toBe( 1 );

				admin action="removeSpoolerTask" type="server" password=server.SERVERADMINPASSWORD id=mine.id;
			});

		});
	}
}
