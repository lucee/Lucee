/**
 * LDEV-4220: cfimap getAll fails with "This operation is not allowed on a closed folder" on a slow server / big mailbox.
 * A small TCP proxy (in this test) sits between cfimap and the imap server and slows down every FETCH, so getAll
 * runs longer than the mail connection pool's idle limit (60s). The pool then closed the store of the client that was
 * still reading. This test takes about 75 seconds.
 */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="imap,mail" {

	variables.imapCfg = server.getTestService( "imap" );
	variables.smtpCfg = server.getTestService( "smtp" );
	if ( !notHasServices() ) {
		variables.imapHost = variables.imapCfg.SERVER;
		variables.imapPort = variables.imapCfg.PORT_INSECURE;
		variables.smtpHost = variables.smtpCfg.SERVER;
		variables.smtpPort = variables.smtpCfg.PORT_INSECURE;
		variables.password = variables.imapCfg.PASSWORD;
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-4220 cfimap getAll that runs longer than the connection pool idle time", skip=notHasServices(), body=function() {

			it( title="a slow getAll (> 60s) is not closed under its feet by the pool", body=function( currentSpec ) {
				var user = newMailbox( 8 );
				// every FETCH is held back 8s for the first 70s, so getAll runs past the pool's 60s idle limit
				var proxy = startProxy( delayMs=8000, slowForMs=70000 );
				var err = "";
				var start = getTickCount();
				try {
					imap action="getAll" name="local.qry" attributeCollection=imapArgs( user, proxy.get( "port" ) );
				}
				catch ( any e ) {
					err = e.message;
					systemOutput( "LDEV4220 getAll error: [#e.type#] #e.message#", true );
				}
				finally {
					stopProxy( proxy );
				}
				systemOutput( "LDEV4220 getAll took #getTickCount() - start#ms, FETCH commands: #proxy.get( "fetches" ).get()#", true );
				expect( err ).notToInclude( "not allowed on a closed folder" );
				expect( err ).toBe( "" );
				expect( local.qry.recordCount ).toBe( 8 );
			});

		});
	}

	private boolean function notHasServices() {
		return structCount( variables.imapCfg ) == 0 || structCount( variables.smtpCfg ) == 0;
	}

	// without proxyPort: direct to the imap server
	private struct function imapArgs( required string user, numeric proxyPort=0 ) {
		return {
			server: arguments.proxyPort ? "127.0.0.1" : variables.imapHost,
			port: arguments.proxyPort ? arguments.proxyPort : variables.imapPort,
			username: arguments.user,
			password: variables.password,
			secure: false,
			timeout: 30
		};
	}

	// sends {count} mails to a fresh mailbox and waits until they arrived (direct, not through the proxy)
	private string function newMailbox( required numeric count ) {
		var user = "ldev4220_" & lCase( left( replace( createUUID(), "-", "", "all" ), 16 ) ) & "@localhost";
		loop from=1 to=arguments.count index="local.i" {
			mail to=user from="ldev4220@localhost" subject="LDEV-4220 mail #i#"
					server=variables.smtpHost port=variables.smtpPort spoolEnable=false {
				echo( repeatString( "LDEV-4220 mail #i# ", 50 ) );
			}
		}
		var start = getTickCount();
		while ( getTickCount() - start < 10000 ) {
			imap action="getHeaderOnly" name="local.q" attributeCollection=imapArgs( user );
			if ( q.recordCount >= arguments.count ) break;
			sleep( 200 );
		}
		return user;
	}

	/**
	 * TCP proxy 127.0.0.1:<random> -> imap server. Holds back every FETCH command {delayMs} for the first
	 * {slowForMs}, like a slow server with a big mailbox.
	 */
	private struct function startProxy( required numeric delayMs, required numeric slowForMs ) {
		var proxy = createObject( "java", "java.util.concurrent.ConcurrentHashMap" ).init();
		var ss = createObject( "java", "java.net.ServerSocket" ).init( 0, 50, createObject( "java", "java.net.InetAddress" ).getByName( "127.0.0.1" ) );
		ss.setSoTimeout( 200 );
		proxy.put( "ss", ss );
		proxy.put( "port", ss.getLocalPort() );
		proxy.put( "fetches", createObject( "java", "java.util.concurrent.atomic.AtomicInteger" ).init( 0 ) );
		proxy.put( "started", getTickCount() );
		proxy.put( "delayMs", arguments.delayMs );
		proxy.put( "slowForMs", arguments.slowForMs );
		proxy.put( "stop", false );
		proxy.put( "sockets", createObject( "java", "java.util.concurrent.CopyOnWriteArrayList" ).init() );
		proxy.put( "host", variables.imapHost );
		proxy.put( "targetPort", variables.imapPort );
		thread name="ldev4220-accept-#createUUID()#" proxy=proxy {
			var p = attributes.proxy;
			while ( !p.get( "stop" ) ) {
				try {
					var c = p.get( "ss" ).accept();
				}
				catch ( any e ) {
					continue; // accept timeout, check the stop flag
				}
				var t = createObject( "java", "java.net.Socket" ).init( p.get( "host" ), javaCast( "int", p.get( "targetPort" ) ) );
				p.get( "sockets" ).add( c );
				p.get( "sockets" ).add( t );
				thread name="ldev4220-up-#createUUID()#" proxy=p c=c t=t {
					pump( attributes.proxy, attributes.c, attributes.t, true );
				}
				thread name="ldev4220-down-#createUUID()#" proxy=p c=c t=t {
					pump( attributes.proxy, attributes.t, attributes.c, false );
				}
			}
		}
		return proxy;
	}

	private void function pump( required proxy, required from, required to, required boolean upstream ) {
		var buf = createObject( "java", "java.lang.reflect.Array" ).newInstance( createObject( "java", "java.lang.Byte" ).TYPE, 8192 );
		var in = arguments.from.getInputStream();
		var out = arguments.to.getOutputStream();
		try {
			while ( true ) {
				var n = in.read( buf );
				if ( n < 0 ) break;
				if ( arguments.upstream ) {
					var chunk = createObject( "java", "java.lang.String" ).init( buf, 0, n, "ISO-8859-1" );
					if ( findNoCase( " FETCH ", chunk ) ) {
						arguments.proxy.get( "fetches" ).incrementAndGet();
						if ( getTickCount() - arguments.proxy.get( "started" ) < arguments.proxy.get( "slowForMs" ) ) sleep( arguments.proxy.get( "delayMs" ) );
					}
				}
				out.write( buf, 0, n );
				out.flush();
			}
		}
		catch ( any e ) {
			// the other direction closed the sockets
		}
		try { arguments.from.close(); } catch ( any e ) {}
		try { arguments.to.close(); } catch ( any e ) {}
	}

	private void function stopProxy( required proxy ) {
		arguments.proxy.put( "stop", true );
		try { arguments.proxy.get( "ss" ).close(); } catch ( any e ) {}
		loop array=arguments.proxy.get( "sockets" ) item="local.s" {
			try { s.close(); } catch ( any e ) {}
		}
	}
}
