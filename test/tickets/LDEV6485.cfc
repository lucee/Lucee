component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {

	function run( testResults, testBox ) {
		describe( "Test suite for LDEV-6485 (javax/jakarta mail Provider clash)", function() {

			it( title="cfmail does not fail with IMAPProvider not a subtype after javax.mail.Session was used", body=function( currentSpec ) {
				// Pollute the request / TCCL path with javax.mail (core still ships commons-email-all on 7.1/8.0).
				// On the buggy mail extension this makes every subsequent jakarta Session.getInstance() fail.
				var props = createObject( "java", "java.util.Properties" ).init();
				var sess = createObject( "java", "javax.mail.Session" ).getInstance( props );
				try { sess.getStore( "imap" ); } catch ( any e ) { /* ignore */ }

				var err = "";
				try {
					// Port 1 is closed on purpose: we only care that Session/Transport creation
					// does not throw the Provider subtype error. A connect failure is fine.
					mail to="a@lucee.org" from="b@lucee.org" subject="LDEV6485-#createUUID()#"
							server="127.0.0.1" port=1 spoolEnable=false timeout=1 {
						echo( "x" );
					}
				}
				catch ( any e ) {
					err = e.message ?: "";
				}
				expect( err ).notToInclude( "not a subtype" );
			});

			it( title="javax.mail.Session still works after cfmail (reverse direction)", body=function( currentSpec ) {
				var err = "";
				try {
					// Trigger jakarta path first (may connect-fail; ignore)
					try {
						mail to="a@lucee.org" from="b@lucee.org" subject="LDEV6485-rev-#createUUID()#"
								server="127.0.0.1" port=1 spoolEnable=false timeout=1 {
							echo( "x" );
						}
					} catch ( any ignore ) {}

					var sess = createObject( "java", "javax.mail.Session" ).getInstance(
						createObject( "java", "java.util.Properties" ).init()
					);
					sess.getStore( "imap" );
				}
				catch ( any e ) {
					err = e.message ?: "";
				}
				expect( err ).notToInclude( "not a subtype" );
			});

		});
	}
}
