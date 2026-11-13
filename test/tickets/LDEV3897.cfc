component extends="org.lucee.cfml.test.LuceeTestCase" labels="queue" {

	function run( testResults, testBox ) {
		describe( "LDEV-3897: request queue timeout", function() {

			it( title="control: a request that gets a slot in the queue is executed", body=function( currentSpec ) {
				var res = withFullQueue( 1500, function() {
					return callRequest();
				}, false );
				expect( res.status ).toBe( 200 );
				expect( res.filecontent ).toBe( "LDEV3897-executed" );
			});

			it( title="ThreadQueue.enter throws a RequestQueueTimeoutException when the timeout is reached", body=function( currentSpec ) {
				var queue = createObject( "java", "lucee.runtime.engine.ThreadQueueImpl" ).init( 2, 1 );
				var pc = getPageContext();
				var caught = "none";
				var msg = "";
				withQueueTimeout( 300, function() {
					queue.enter( pc );
					try {
						queue.enter( pc );
					}
					catch ( "lucee.runtime.engine.RequestQueueTimeoutException" e ) {
						caught = "RequestQueueTimeoutException";
						msg = e.message;
					}
					catch ( "java.io.IOException" e ) {
						caught = "plain IOException";
						msg = e.message;
					}
					finally {
						queue.exit( pc );
					}
				});
				expect( caught ).toBe( "RequestQueueTimeoutException" );
				expect( msg ).toInclude( "[300 ms]" );
				expect( msg ).toInclude( "server is too busy" );
			});

			it( title="a request that times out in the queue gets a 503 with Retry-After and a plain body and is not executed", body=function( currentSpec ) {
				var res = withFullQueue( 1500, function() {
					return callRequest();
				}, true );
				expect( res.status ).toBe( 503 );
				expect( res.retryAfter ).toBe( "2", "Retry-After is the queue timeout in seconds, rounded up" );
				expect( res.contentType ).toInclude( "text/plain" );
				expect( res.filecontent ).toInclude( "too busy" );
				expect( res.filecontent ).notToInclude( "LDEV3897-executed" );
			});

		});
	}

	// the request has to go through the engine's servlet handling (CFMLEngine.serviceCFML), an _InternalRequest does not
	private struct function callRequest() {
		var engine = createObject( "java", "lucee.loader.engine.CFMLEngineFactory" ).getInstance();
		var creation = engine.getCreationUtil();
		var servlet = getPageContext().getServlet();
		var root = createObject( "java", "java.io.File" ).init( expandPath( "/" ) );
		var out = createObject( "java", "java.io.ByteArrayOutputStream" ).init();
		var req = creation.createHttpServletRequest( root, "localhost", "/test/tickets/LDEV3897/index.cfm", "", [], {}, {}, {}, nullValue() );
		var rsp = creation.createHttpServletResponse( out );
		// own thread, the engine releases the thread's PageContext when the request ends
		var thread_name = "LDEV3897-" & createUUID();
		thread name=thread_name servlet=servlet req=req rsp=rsp engine=engine {
			attributes.engine.serviceCFML( attributes.servlet, attributes.req, attributes.rsp );
		}
		thread action="join" name=thread_name timeout=30000;
		if ( !isNull( cfthread[ thread_name ].error ) ) throw( object=cfthread[ thread_name ].error );
		return {
			"status": rsp.getStatus(),
			"retryAfter": isNull( rsp.getHeader( "Retry-After" ) ) ? "" : rsp.getHeader( "Retry-After" ),
			"contentType": isNull( rsp.getContentType() ) ? "" : rsp.getContentType(),
			"filecontent": out.toString( "UTF-8" )
		};
	}

	// replaces the server's request queue by one with a single slot, optionally taken by this thread, and a short queue timeout
	private any function withFullQueue( required numeric timeout, required function body, required boolean fillSlot ) {
		var cs = createObject( "java", "lucee.runtime.config.ConfigUtil" ).getConfigServerImpl( getPageContext().getConfig() );
		var original = cs.getThreadQueue();
		var queue = createObject( "java", "lucee.runtime.engine.ThreadQueueImpl" ).init( 2, 1 );
		var pc = getPageContext();
		var entered = false;
		var result = "";
		withQueueTimeout( arguments.timeout, function() {
			cs.setThreadQueue( queue );
			try {
				if ( fillSlot ) {
					queue.enter( pc );
					entered = true;
				}
				result = body();
			}
			finally {
				if ( entered ) queue.exit( pc );
				cs.setThreadQueue( original );
			}
		});
		return result;
	}

	private void function withQueueTimeout( required numeric timeout, required function body ) {
		var cs = createObject( "java", "lucee.runtime.config.ConfigUtil" ).getConfigServerImpl( getPageContext().getConfig() );
		var system = createObject( "java", "java.lang.System" );
		var prev = system.getProperty( "lucee.queue.timeout" );
		system.setProperty( "lucee.queue.timeout", toString( arguments.timeout ) );
		cs.resetQueueTimeout();
		try {
			body();
		}
		finally {
			if ( isNull( prev ) ) system.clearProperty( "lucee.queue.timeout" );
			else system.setProperty( "lucee.queue.timeout", prev );
			cs.resetQueueTimeout();
		}
	}

}
