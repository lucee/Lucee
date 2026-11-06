component extends="org.lucee.cfml.test.LuceeTestCase" labels="cfexecute" {

	function run( testResults, textbox ) {
		describe( "LDEV-5504 - cfexecute worker runs on a virtual thread", function() {

			it( title = "cfexecute without a timeout returns the output", skip = isWindows(), body = function( currentSpec ) {
				cfexecute( name = "echo", arguments = "ldev5504", variable = "local.out" );
				expect( trim( out ) ).toBe( "ldev5504" );
			});

			it( title = "cfexecute with a timeout returns the output and the exit code", skip = isWindows(), body = function( currentSpec ) {
				cfexecute( name = "echo", arguments = "ldev5504", variable = "local.out", exitCodeVariable = "local.exitCode", timeout = 10 );
				expect( trim( out ) ).toBe( "ldev5504" );
				expect( exitCode ).toBe( 0 );
			});

			it( title = "cfexecute with a timeout throws when the timeout expires", skip = isWindows(), body = function( currentSpec ) {
				var start = getTickCount();
				var msg = "";
				try {
					cfexecute( name = "sleep", arguments = "5", timeout = 0.3, terminateOnTimeout = true );
				}
				catch ( any e ) {
					msg = e.message;
				}
				expect( msg ).toInclude( "expired" );
				expect( getTickCount() - start ).toBeLT( 4000 );
			});

			it( title = "the PageContext is bound to the request thread right after a timeout", skip = isWindows(), body = function( currentSpec ) {
				var pc = getPageContext();
				var requestThread = createObject( "java", "java.lang.Thread" ).currentThread();
				// the timed out worker is still unwinding right after the timeout, so repeat it to hit that window reliably
				loop times = 5 {
					var msg = "";
					try {
						cfexecute( name = "sleep", arguments = "5", timeout = 0.3, terminateOnTimeout = true );
					}
					catch ( any e ) {
						msg = e.message;
					}
					expect( msg ).toInclude( "expired" );
					// the worker must not keep the PageContext bound to itself
					expect( pc.getThread().equals( requestThread ) ).toBeTrue( "PageContext is bound to [#pc.getThread()#] instead of the request thread [#requestThread#]" );
				}
			});

			it( title = "cfexecute worker thread is a virtual thread where supported", skip = isWindows(), body = function( currentSpec ) {
				var pc = getPageContext();
				var parentThread = createObject( "java", "java.lang.Thread" ).currentThread();
				var tname = "ldev5504_" & createUUID().replace( "-", "", "all" );
				var raw = server.system.properties[ "lucee.allow.virtual.threads" ] ?: ( server.system.environment[ "LUCEE_ALLOW_VIRTUAL_THREADS" ] ?: "true" );
				var virtualAllowed = listFirst( server.java.version, "." ) gte 21 && raw != "false";

				// the PageContext is bound to the worker thread as long as cfexecute is running
				thread name = tname action = "run" pc = pc parentThread = parentThread {
					thread.found = false;
					thread.workerVirtual = false;
					until = getTickCount() + 8000;
					while ( getTickCount() < until ) {
						t = attributes.pc.getThread();
						if ( !isNull( t ) && !t.equals( attributes.parentThread ) ) {
							thread.found = true;
							thread.workerVirtual = t.isVirtual();
							break;
						}
						sleep( 20 );
					}
				}

				cfexecute( name = "sleep", arguments = "2", timeout = 10 );
				thread action = "join" name = tname;

				expect( isNull( cfthread[ tname ].error ) ).toBeTrue( serializeJSON( cfthread[ tname ] ) );
				expect( cfthread[ tname ].found ).toBeTrue();
				expect( cfthread[ tname ].workerVirtual ).toBe( virtualAllowed );
				// the PageContext is bound to the request thread again
				expect( pc.getThread().equals( parentThread ) ).toBeTrue();
			});

		});
	}

	private boolean function isWindows() {
		return findNoCase( "windows", server.os.name ) > 0;
	}

}
