component extends="org.lucee.cfml.test.LuceeTestCase" labels="config" {

	// Lucee 7.x system property / environment variable names that are supported again as aliases in 8.0.
	// The old name is only used when the new name is not set. Every spec clears the system properties it sets
	// and resets the affected config values again, so the server ends up with its previous state.

	function beforeAll() {
		variables.System = createObject( "java", "java.lang.System" );
		variables.cs = createObject( "java", "lucee.runtime.config.ConfigUtil" ).getConfigServerImpl( getPageContext() );
		variables.AL = createObject( "java", "lucee.runtime.listener.ApplicationListener" );
	}

	function run( testResults, testBox ) {
		describe( "Lucee 7 system property / env var names still work as aliases", function() {

			it( title="lucee.application.listener is an alias for lucee.listener.type", body=function() {
				try {
					System.setProperty( "lucee.application.listener", "modern" );
					cs.resetListenerType();
					expect( cs.getListenerType() ).toBe( AL.TYPE_MODERN );

					// the new name wins over the old one
					System.setProperty( "lucee.listener.type", "mixed" );
					cs.resetListenerType();
					expect( cs.getListenerType() ).toBe( AL.TYPE_MIXED );
				}
				finally {
					System.clearProperty( "lucee.application.listener" );
					System.clearProperty( "lucee.listener.type" );
					cs.resetListenerType();
				}
			});

			it( title="lucee.application.mode is an alias for lucee.listener.mode", body=function() {
				try {
					System.setProperty( "lucee.application.mode", "root" );
					cs.resetListenerMode();
					expect( cs.getListenerMode() ).toBe( AL.MODE_ROOT );

					System.setProperty( "lucee.listener.mode", "currenttoroot" );
					cs.resetListenerMode();
					expect( cs.getListenerMode() ).toBe( AL.MODE_CURRENT2ROOT );
				}
				finally {
					System.clearProperty( "lucee.application.mode" );
					System.clearProperty( "lucee.listener.mode" );
					cs.resetListenerMode();
				}
			});

			it( title="lower case lucee.requesttimeout.*threshold system properties work", body=function() {
				try {
					System.setProperty( "lucee.requesttimeout.memorythreshold", "0.75" );
					System.setProperty( "lucee.requesttimeout.cputhreshold", "0.5" );
					System.setProperty( "lucee.requesttimeout.concurrentrequestthreshold", "42" );
					cs.resetRequestTimeoutMemoryThreshold();
					cs.resetRequestTimeoutCPUThreshold();
					cs.resetRequestTimeoutConcurrentRequestThreshold();
					expect( round( cs.getRequestTimeoutMemoryThreshold() * 100 ) ).toBe( 75 );
					expect( round( cs.getRequestTimeoutCPUThreshold() * 100 ) ).toBe( 50 );
					expect( cs.getRequestTimeoutConcurrentRequestThreshold() ).toBe( 42 );
				}
				finally {
					System.clearProperty( "lucee.requesttimeout.memorythreshold" );
					System.clearProperty( "lucee.requesttimeout.cputhreshold" );
					System.clearProperty( "lucee.requesttimeout.concurrentrequestthreshold" );
					cs.resetRequestTimeoutMemoryThreshold();
					cs.resetRequestTimeoutCPUThreshold();
					cs.resetRequestTimeoutConcurrentRequestThreshold();
				}
			});

			it( title="lucee.debugging.options enables the listed debug options", body=function() {
				try {
					System.setProperty( "lucee.debugging.options", " Template , queryUsage" );
					cs.resetDebugOptions();
					expect( cs.getDebuggingTemplate() ).toBeTrue();
					expect( cs.getDebuggingQueryUsage() ).toBeTrue();

					// an option defined via its own (new) name wins over the legacy list
					System.setProperty( "lucee.monitoring.debuggingTemplate", "false" );
					cs.resetDebugOptions();
					expect( cs.getDebuggingTemplate() ).toBeFalse();
					expect( cs.getDebuggingQueryUsage() ).toBeTrue();
				}
				finally {
					System.clearProperty( "lucee.debugging.options" );
					System.clearProperty( "lucee.monitoring.debuggingTemplate" );
					cs.resetDebugOptions();
				}
			});

			it( title="lucee.maven.default.repositories is used before the default repositories", body=function() {
				var defaults = createObject( "java", "lucee.runtime.config.maven.MavenUpdateProvider" ).DEFAULT_REPOSITORIES_RELEASES;
				var repoUrl = "https://maven.example.invalid/repo/";
				try {
					System.setProperty( "lucee.maven.default.repositories", repoUrl );
					cs.resetMavenRepository();
					var reps = cs.getMavenRepository();
					expect( reps[ 1 ].getUrl() ).toBe( repoUrl );
					expect( arrayLen( reps ) ).toBe( arrayLen( defaults ) + 1 );
					expect( reps[ 2 ].getUrl() ).toBe( defaults[ 1 ].getUrl() );
				}
				finally {
					System.clearProperty( "lucee.maven.default.repositories" );
					cs.resetMavenRepository();
				}
				expect( arrayLen( cs.getMavenRepository() ) ).toBe( arrayLen( defaults ) );
			});

		});
	}
}
