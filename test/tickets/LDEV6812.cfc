component extends="org.lucee.cfml.test.LuceeTestCase" labels="extension,threads,virtual-threads" {

	function run( testResults, testBox ) {
		describe( "LDEV-6812: loading the extensions from virtual threads must not deadlock", function() {

			it( title="many virtual threads loading the extensions at the same time complete", body=function() {
				var cs = createObject( "java", "lucee.runtime.config.ConfigUtil" ).getConfigServerImpl( getPageContext().getConfig() );
				var cores = createObject( "java", "java.lang.Runtime" ).getRuntime().availableProcessors();

				// make sure the extensions are loaded once, then force the next access to load them again
				extensionList();

				// more concurrent callers than carrier threads (default parallelism is the number of cores)
				var callers = [];
				loop from=1 to=( cores * 4 ) index="local.i" {
					callers.append( i );
				}

				var threadName = "LDEV6812-" & createUUID();
				cs.resetRHExtensions();
				thread name=threadName callers=callers {
					each( attributes.callers, function( caller ) {
						extensionList();
					}, "virtual" );
				}

				// a platform thread runs the test, so a deadlock shows up as a timeout instead of a hanging test suite
				thread action="join" name=threadName timeout=120000;
				var result = cfthread[ threadName ];
				if ( result.status != "COMPLETED" ) {
					try {
						thread action="terminate" name=threadName;
					}
					catch ( any e ) {
						// the threads of a real deadlock cannot be terminated
					}
				}
				expect( result.status ).toBe( "COMPLETED", "loading the extensions from #callers.len()# virtual threads did not complete (deadlock)" );
				expect( structKeyExists( result, "error" ) ).toBeFalse( structKeyExists( result, "error" ) ? result.error.message : "" );
			});

		});
	}

}
