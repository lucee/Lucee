component extends="org.lucee.cfml.test.LuceeTestCase" labels="config" {

	function beforeAll() {
		variables.dir = getTempDirectory() & "ldev6666-" & createUUID() & "/";
		directoryCreate( variables.dir );
		variables.res = createObject( "java", "lucee.commons.io.res.util.ResourceUtil" ).toResourceNotExisting( getPageContext(), variables.dir );
		variables.factory = createObject( "java", "lucee.runtime.config.ConfigFactoryImpl" );
	}

	function afterAll() {
		if ( directoryExists( variables.dir ) ) directoryDelete( variables.dir, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6666 base components are extracted before extensions get installed", function() {

			it( "the running server has the base components in place", function() {
				var serverComponents = getPageContext().getConfig().getConfigServerDir().getAbsolutePath() & "/components/";
				expect( fileExists( serverComponents & "org/lucee/cfml/Component.cfc" ) ).toBeTrue();
				expect( fileExists( serverComponents & "lucee/extension/gateway/TaskGateway.cfc" ) ).toBeTrue();
			});

			it( "deployBaseComponents extracts core and gateway base components", function() {
				expect( variables.factory.deployBaseComponents( variables.res, nullValue(), true ) ).toBeTrue();
				var base = variables.dir & "components/org/lucee/cfml/";
				for ( var name in [ "Component", "HelperBase", "Feed", "Http", "Query", "Result", "Administrator",
						"orm/IEventHandler", "orm/INamingStrategy", "test/LuceeTestCase", "test/LuceeTestSuite" ] ) {
					expect( fileExists( base & name & ".cfc" ) ).toBeTrue( name );
				}
				var gw = variables.dir & "components/lucee/extension/gateway/";
				for ( var name in [ "TaskGateway", "DummyGateway", "DirectoryWatcher", "DirectoryWatcherListener", "WatchService",
						"MailWatcher", "MailWatcherListener", "AsynchronousEvents", "AsynchronousEventsListener" ] ) {
					expect( fileExists( gw & name & ".cfc" ) ).toBeTrue( name );
				}
			});

			it( "deployBaseComponents only runs once per config directory", function() {
				expect( variables.factory.deployBaseComponents( variables.res, nullValue(), true ) ).toBeFalse();
			});
		});
	}
}
