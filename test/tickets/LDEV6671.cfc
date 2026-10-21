component extends="org.lucee.cfml.test.LuceeTestCase" labels="maven" {

	function run( testResults, testBox ) {
		describe( title="LDEV-6671: Maven download policy messages must name an existing setting", body=function() {

			it( title="startup phase names lucee.maven.download.policy.startup", body=function( currentSpec ) {
				var engine = createObject( "java", "lucee.runtime.engine.CFMLEngineImpl" );
				var msg = engine.getDownloadPolicySetting( false );
				expect( msg ).toInclude( "'lucee.maven.download.policy.startup'" );
				expect( msg ).toInclude( "'LUCEE_MAVEN_DOWNLOAD_POLICY_STARTUP'" );
				expect( msg ).notToInclude( "'lucee.maven.download.policy'" );
			});

			it( title="runtime phase names lucee.maven.download.policy.runtime", body=function( currentSpec ) {
				var engine = createObject( "java", "lucee.runtime.engine.CFMLEngineImpl" );
				var msg = engine.getDownloadPolicySetting( true );
				expect( msg ).toInclude( "'lucee.maven.download.policy.runtime'" );
				expect( msg ).toInclude( "'LUCEE_MAVEN_DOWNLOAD_POLICY_RUNTIME'" );
				expect( msg ).notToInclude( "'lucee.maven.download.policy'" );
			});

			it( title="while serving requests the active setting is the runtime one", body=function( currentSpec ) {
				var engine = createObject( "java", "lucee.runtime.engine.CFMLEngineImpl" );
				expect( engine.getActiveDownloadPolicySetting() ).toBe( engine.getDownloadPolicySetting( true ) );
			});

		});
	}
}
