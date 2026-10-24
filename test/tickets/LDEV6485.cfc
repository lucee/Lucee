component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {

	function beforeAll() {
		// the core does not ship javax.mail anymore (commons-email-all is removed in 9.0), an application that still uses it brings it, here from Maven
		variables.javaxMail = new component javaSettings='{"maven":["com.sun.mail:javax.mail:1.6.2"]}' {
			function getSession( required any props ) {
				return createObject( "java", "javax.mail.Session" ).getInstance( arguments.props );
			}
		};
	}

	function run( testResults, testBox ) {
		describe( "Test suite for LDEV-6485 (javax/jakarta mail Provider clash)", function() {

			it( title="cfmail does not fail with IMAPProvider not a subtype after javax.mail.Session was used", body=function( currentSpec ) {
				// Use javax.mail next to the jakarta based mail extension (on 7.1/8.0 the core shipped commons-email-all and polluted the request / TCCL path).
				// On the buggy mail extension this makes every subsequent jakarta Session.getInstance() fail.
				var props = createObject( "java", "java.util.Properties" ).init();
				var sess = variables.javaxMail.getSession( props );
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

					var sess = variables.javaxMail.getSession( createObject( "java", "java.util.Properties" ).init() );
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
