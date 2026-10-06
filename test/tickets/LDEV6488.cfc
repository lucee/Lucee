/**
 * LDEV-6488: cfimap / cfpop with secure=true fail with SSLHandshakeException "No appropriate protocol" on some
 * Java 21 setups, because MailClient never sets mail.<type>.ssl.protocols (the SMTP side does since LDEV-5893),
 * so JavaMail's SocketFetcher falls back to whatever the socket reports as enabled.
 * The handshake failure itself depends on the reporter's JVM/provider setup and doesn't happen on a stock JDK, so
 * this test checks (1) secure IMAP / POP3 connections work, (2) the session of a secure client has
 * mail.<type>.ssl.protocols set to TLSv1.2+ protocols only and (3) the JVM setting mail.imap.ssl.protocols reaches
 * the handshake (the reporter tried -Dmail.imaps.ssl.protocols, which had no effect).
 * Uses the imap + pop test services (greenmail in CI, self-signed certificate, so the test trusts it for the
 * duration of each spec via lucee.ssl.checkserveridentity=false).
 */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="imap,pop,mail" {

	variables.imapCfg = server.getTestService( "imap" );
	variables.popCfg = server.getTestService( "pop" );

	function run( testResults, testBox ) {
		describe( title="LDEV-6488 cfimap / cfpop secure=true", skip=notHasServices(), body=function() {

			it( title="cfimap secure=true connects", body=function( currentSpec ) {
				withTrustAll( function() {
					imap action="getHeaderOnly" name="local.q" server=imapCfg.SERVER port=imapCfg.PORT_SECURE
						username=newUser() password=imapCfg.PASSWORD secure=true;
					expect( q.recordCount ).toBe( 0 );
				});
			});

			it( title="cfpop secure=true connects", body=function( currentSpec ) {
				withTrustAll( function() {
					pop action="getHeaderOnly" name="local.q" server=popCfg.SERVER port=popCfg.PORT_SECURE
						username=newUser() password=popCfg.PASSWORD secure=true;
					expect( q.recordCount ).toBe( 0 );
				});
			});

			it( title="secure IMAP session sets mail.imap.ssl.protocols (TLSv1.2+ only)", body=function( currentSpec ) {
				expectTls12Plus( sslProtocols( 1, "imap", imapCfg ) );
			});

			it( title="secure POP3 session sets mail.pop3.ssl.protocols (TLSv1.2+ only)", body=function( currentSpec ) {
				expectTls12Plus( sslProtocols( 0, "pop3", popCfg ) );
			});

			it( title="the system property mail.imap.ssl.protocols is used for the handshake", body=function( currentSpec ) {
				// TLSv1.1 is disabled on current JDKs, so if the setting reaches JavaMail the handshake fails
				// with the reporter's "No appropriate protocol", if it's ignored the connection works
				var System = createObject( "java", "java.lang.System" );
				var key = "mail.imap.ssl.protocols";
				var old = System.getProperty( key );
				var err = "";
				System.setProperty( key, "TLSv1.1" );
				try {
					withTrustAll( function() {
						imap action="getHeaderOnly" name="local.q" server=imapCfg.SERVER port=imapCfg.PORT_SECURE
							username=newUser() password=imapCfg.PASSWORD secure=true;
					});
				}
				catch ( any e ) {
					err = e.message;
				}
				finally {
					if ( isNull( old ) ) System.clearProperty( key );
					else System.setProperty( key, old );
				}
				systemOutput( "LDEV6488 TLSv1.1 only: [#err#]", true );
				expect( err ).toInclude( "No appropriate protocol", "mail.imap.ssl.protocols was ignored" );
			});

		});
	}

	private boolean function notHasServices() {
		return structCount( variables.imapCfg ) == 0 || structCount( variables.popCfg ) == 0;
	}

	private string function newUser() {
		return "ldev6488_" & lCase( left( replace( createUUID(), "-", "", "all" ), 16 ) ) & "@localhost";
	}

	private void function expectTls12Plus( required string protocols ) {
		systemOutput( "LDEV6488 ssl.protocols: [#arguments.protocols#]", true );
		expect( arguments.protocols ).notToBe( "", "mail.<type>.ssl.protocols is not set for a secure connection" );
		loop list=arguments.protocols delimiters=" ," item="local.p" {
			expect( listFindNoCase( "TLSv1.2,TLSv1.3", p ) ).toBeGT( 0, "unexpected protocol [#p#] in [#arguments.protocols#]" );
		}
	}

	// starts a secure MailClient (type 0=pop3, 1=imap) and returns mail.<prefix>.ssl.protocols of its session
	private string function sslProtocols( required numeric type, required string prefix, required struct cfg ) {
		var result = "";
		withTrustAll( function() {
			var cls = mailClientClass();
			var mc = staticMethod( cls, "getInstance", 8 ).invoke( javaCast( "null", "" ), [ javaCast( "int", type ), cfg.SERVER,
				javaCast( "int", cfg.PORT_SECURE ), newUser(), cfg.PASSWORD, true, "ldev6488-" & createUUID(), "" ] );
			try {
				var c = mc.getClass();
				while ( c.getSimpleName() != "MailClient" ) c = c.getSuperclass();
				var f = c.getDeclaredField( "_session" );
				f.setAccessible( true );
				result = f.get( mc ).getProperties().getProperty( "mail.#prefix#.ssl.protocols", "" );
			}
			finally {
				staticMethod( cls, "removeInstance", 1 ).invoke( javaCast( "null", "" ), [ mc ] );
			}
		});
		return result;
	}

	// the MailClient class cfimap really uses, loaded through the class loader of the cfimap tag class:
	// 7.1+ org.lucee.extension.mail.MailClient from the mail extension, before lucee.runtime.net.mail.MailClient in core.
	// (createObject with the extension's maven coordinates can resolve jakarta.mail from another class loader)
	private function mailClientClass() {
		loop array=getPageContext().getConfig().getTLDs() item="local.tld" {
			var tag = tld.getTag( "imap" );
			if ( isNull( tag ) ) continue;
			var tagClass = tag.getTagClassDefinition().getClazz();
			var name = left( tagClass.getName(), 25 ) == "org.lucee.extension.mail." ? "org.lucee.extension.mail.MailClient" : "lucee.runtime.net.mail.MailClient";
			return tagClass.getClassLoader().loadClass( name );
		}
		throw "no imap tag found";
	}

	private function staticMethod( required cls, required string name, required numeric paramCount ) {
		loop array=arguments.cls.getMethods() item="local.m" {
			if ( m.getName() == arguments.name && arrayLen( m.getParameterTypes() ) == arguments.paramCount ) return m;
		}
		throw "method [#arguments.name#] not found in [#arguments.cls.getName()#]";
	}

	// greenmail uses a self-signed certificate
	private void function withTrustAll( required function fn ) {
		var System = createObject( "java", "java.lang.System" );
		var key = "lucee.ssl.checkserveridentity";
		var old = System.getProperty( key );
		System.setProperty( key, "false" );
		try {
			arguments.fn();
		}
		finally {
			if ( isNull( old ) ) System.clearProperty( key );
			else System.setProperty( key, old );
		}
	}
}
