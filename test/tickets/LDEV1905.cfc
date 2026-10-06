/*
 * LDEV-1905: a spooled mail stores the SMTP server details (including username and password) inside its
 * task file (remote-client/open/*.tsk), in plain text. Because the servers are stored with the task, a queued
 * mail also never notices when the mail server settings are changed in the admin.
 * The first two specs use a future sendTime (nothing is sent). The third one uses a GreenMail SMTP mock.
 * All three fail until it is decided how a spooled mail should keep its credentials (see the ticket), so they
 * are skipped unless LDEV1905_RUN=true is set, to keep CI green. Remove the skip together with the fix.
 */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" javaSettings='{
		"maven": [
			"com.icegreen:greenmail:2.1.7"
		]
	}' {

	import "com.icegreen.greenmail.util.ServerSetup";
	import "com.icegreen.greenmail.util.GreenMail";

	variables.port = 30259;
	variables.host = "127.0.0.1";

	function beforeAll() {
		variables.smtp = new GreenMail( new ServerSetup( variables.port, nullValue(), ServerSetup::PROTOCOL_SMTP ) );
		variables.smtp.start();
	}

	function afterAll() {
		if ( !isNull( variables.smtp ) ) variables.smtp.stop();
	}

	function run( testResults, testBox ) {
		describe( "LDEV-1905 mail server details stored inside each spooled mail task", function() {

			it( title="the password given in the cfmail tag is not stored in plain text in the task file", skip=notEnabled(), body=function( currentSpec ) {
				var subject = "LDEV1905-tag-" & createUUID();
				var pw = "TagPw" & replace( createUUID(), "-", "", "all" );
				mail to="receiver@lucee.org" from="sender@lucee.org" subject=subject server=variables.host port="1"
						username="spooluser" password=pw spoolEnable=true sendTime=dateAdd( "d", 1, now() ) {
					echo( "LDEV-1905" );
				}
				try {
					var res = scanTaskFiles( subject, pw );
					expect( res.files ).toBeGT( 0, "spooled task file not found" );
					expect( res.hits ).toBe( 0, "the smtp password is stored in plain text in the task file" );
				}
				finally {
					removeTasks( subject );
				}
			});

			it( title="the password of a mail server configured in the admin is not stored in the task file", skip=notEnabled(), body=function( currentSpec ) {
				var subject = "LDEV1905-admin-" & createUUID();
				var pw = "AdminPw" & replace( createUUID(), "-", "", "all" );
				var hostname = "ldev1905-" & lCase( left( replace( createUUID(), "-", "", "all" ), 12 ) ) & ".invalid";
				admin action="updateMailServer" type="server" password=server.SERVERADMINPASSWORD
					hostname=hostname port="1" dbusername="spooluser" dbpassword=pw id="new"
					life=createTimeSpan( 0, 0, 1, 0 ) idle=createTimeSpan( 0, 0, 0, 10 );
				try {
					// no server attribute, so the configured mail servers are used
					mail to="receiver@lucee.org" from="sender@lucee.org" subject=subject
							spoolEnable=true sendTime=dateAdd( "d", 1, now() ) {
						echo( "LDEV-1905" );
					}
					var res = scanTaskFiles( subject, pw );
					expect( res.files ).toBeGT( 0, "spooled task file not found" );
					expect( res.hits ).toBe( 0, "the password of the configured mail server is stored in plain text in the task file" );
				}
				finally {
					removeTasks( subject );
					admin action="removeMailServer" type="server" password=server.SERVERADMINPASSWORD hostname=hostname username="spooluser";
				}
			});

			it( title="a queued mail uses the current mail server settings, not the ones from when it was queued", skip=notEnabled(), body=function( currentSpec ) {
				var subject = "LDEV1905-change-" & createUUID();
				var hostname = "ldev1905-" & lCase( left( replace( createUUID(), "-", "", "all" ), 12 ) ) & ".invalid";
				variables.smtp.purgeEmailFromAllMailboxes();
				// wrong settings first (a host that doesn't exist)
				admin action="updateMailServer" type="server" password=server.SERVERADMINPASSWORD
					hostname=hostname port="1" dbusername="" dbpassword="" id="new"
					life=createTimeSpan( 0, 0, 1, 0 ) idle=createTimeSpan( 0, 0, 0, 10 );
				var fixed = false;
				try {
					mail to="receiver@lucee.org" from="sender@lucee.org" subject=subject
							spoolEnable=true sendTime=dateAdd( "d", 1, now() ) {
						echo( "LDEV-1905" );
					}
					// the admin fixes the mail server settings
					admin action="removeMailServer" type="server" password=server.SERVERADMINPASSWORD hostname=hostname username="";
					admin action="updateMailServer" type="server" password=server.SERVERADMINPASSWORD
						hostname=variables.host port=variables.port dbusername="" dbpassword="" id="new"
						life=createTimeSpan( 0, 0, 1, 0 ) idle=createTimeSpan( 0, 0, 0, 10 );
					fixed = true;

					// and retries the queued mail
					var id = taskId( subject );
					expect( len( id ) ).toBeGT( 0, "spooled task not found" );
					var err = "";
					try {
						admin action="executeSpoolerTask" type="server" password=server.SERVERADMINPASSWORD id=id;
					}
					catch ( e ) {
						err = e.message;
					}
					var received = 0;
					for ( var m in variables.smtp.getReceivedMessages() ) {
						if ( m.getSubject() == subject ) received++;
					}
					systemOutput( "LDEV1905 retry after changing the mail server: received=#received# error=[#left( err, 200 )#]", true );
					expect( received ).toBe( 1, "the queued mail was retried with the old (stored) mail server settings: " & err );
				}
				finally {
					removeTasks( subject );
					admin action="removeMailServer" type="server" password=server.SERVERADMINPASSWORD hostname=( fixed ? variables.host : hostname ) username="";
				}
			});
		});
	}

	private boolean function notEnabled() {
		return ( server.system.environment.LDEV1905_RUN ?: "" ) != "true";
	}

	// counts the task files that contain the subject, and of those, the ones that also contain the secret
	private struct function scanTaskFiles( required string subject, required string secret ) {
		var res = { files: 0, hits: 0 };
		var dirs = [ getPageContext().getConfig().getRemoteClientDirectory().getAbsolutePath(), expandPath( "{lucee-server}" ), expandPath( "{lucee-web}" ) ];
		var seen = {};
		for ( var dir in dirs ) {
			if ( !directoryExists( dir ) ) continue;
			for ( var f in directoryList( dir, true, "path", "*.tsk" ) ) {
				if ( structKeyExists( seen, f ) ) continue;
				seen[ f ] = true;
				var s = toString( fileReadBinary( f ), "iso-8859-1" );
				if ( find( arguments.subject, s ) ) {
					res.files++;
					if ( find( arguments.secret, s ) ) res.hits++;
				}
			}
		}
		systemOutput( "LDEV1905 task files with subject: #res.files#, containing the plain password: #res.hits#", true );
		return res;
	}

	private string function taskId( required string subject ) {
		admin action="getSpoolerTasks" type="server" password=server.SERVERADMINPASSWORD startrow="1" maxrow="10000" returnVariable="local.tasks";
		for ( var row in local.tasks ) {
			if ( row.name == arguments.subject ) return row.id;
		}
		return "";
	}

	private function removeTasks( required string subject ) {
		admin action="getSpoolerTasks" type="server" password=server.SERVERADMINPASSWORD startrow="1" maxrow="10000" returnVariable="local.tasks";
		for ( var row in local.tasks ) {
			if ( row.name == arguments.subject ) admin action="removeSpoolerTask" type="server" password=server.SERVERADMINPASSWORD id=row.id;
		}
	}
}
