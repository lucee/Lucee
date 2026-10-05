/**
 * LDEV-5823: cfimap action="delete" ignores the folder attribute, INBOX is hard-coded.
 * Uses the imap + smtp test services (greenmail in CI, auth disabled so every login creates its own mailbox).
 */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="imap,mail" {

	variables.imapCfg = server.getTestService( "imap" );
	variables.smtpCfg = server.getTestService( "smtp" );

	function run( testResults, testBox ) {
		describe( title="LDEV-5823 cfimap action=delete with folder", skip=notHasServices(), body=function() {

			it( title="deletes the message from the given folder, not from INBOX", body=function( currentSpec ) {
				var user = newMailbox( 2 );
				var folderName = "LDEV5823";

				imap action="createFolder" folder=folderName attributeCollection=imapArgs( user );
				imap action="moveMail" folder="INBOX" newFolder=folderName messageNumber="1" attributeCollection=imapArgs( user );
				expect( countMails( user, "INBOX" ) ).toBe( 1, "precondition: INBOX after move" );
				expect( countMails( user, folderName ) ).toBe( 1, "precondition: folder after move" );

				imap action="delete" folder=folderName messageNumber="1" attributeCollection=imapArgs( user );

				expect( countMails( user, folderName ) ).toBe( 0, "the message in folder [#folderName#] was not deleted" );
				expect( countMails( user, "INBOX" ) ).toBe( 1, "a message in INBOX was deleted instead of the one in [#folderName#]" );
			});

			it( title="without folder still deletes from INBOX", body=function( currentSpec ) {
				var user = newMailbox( 2 );
				imap action="delete" messageNumber="1" attributeCollection=imapArgs( user );
				expect( countMails( user, "INBOX" ) ).toBe( 1 );
			});

		});
	}

	private boolean function notHasServices() {
		return structCount( variables.imapCfg ) == 0 || structCount( variables.smtpCfg ) == 0;
	}

	private struct function imapArgs( required string user ) {
		return {
			server: variables.imapCfg.SERVER,
			port: variables.imapCfg.PORT_INSECURE,
			username: arguments.user,
			password: variables.imapCfg.PASSWORD,
			secure: false
		};
	}

	private numeric function countMails( required string user, required string folder ) {
		imap action="getHeaderOnly" folder=arguments.folder name="local.qry" attributeCollection=imapArgs( arguments.user );
		return local.qry.recordCount;
	}

	// sends {count} mails to a fresh mailbox and waits until they arrived
	private string function newMailbox( required numeric count ) {
		var user = "ldev5823_" & lCase( left( replace( createUUID(), "-", "", "all" ), 16 ) ) & "@localhost";
		loop from=1 to=arguments.count index="local.i" {
			mail to=user from="ldev5823@localhost" subject="LDEV-5823 mail #i#"
					server=variables.smtpCfg.SERVER port=variables.smtpCfg.PORT_INSECURE spoolEnable=false {
				echo( "LDEV-5823 mail #i#" );
			}
		}
		var start = getTickCount();
		while ( countMails( user, "INBOX" ) < arguments.count && getTickCount() - start < 10000 ) sleep( 200 );
		return user;
	}
}
