component extends="org.lucee.cfml.test.LuceeTestCase" labels="controller,deploy" {

	function run( testResults, testBox ) {

		describe( "LDEV-5018 allow disabling deploy folder polling", function() {

			var Controler = createObject( "java", "lucee.runtime.engine.Controler" );
			var ConfigServerImpl = createObject( "java", "lucee.runtime.config.ConfigServerImpl" );

			describe( "the deployWatch setting in .CFConfig.json", function() {

				it( "is enabled by default", function() {
					expect( ConfigServerImpl.metaDeployWatch.get( getConfigServer(), {}, false ) ).toBeTrue();
				});

				it( "can be disabled with deployWatch=false", function() {
					expect( ConfigServerImpl.metaDeployWatch.get( getConfigServer(), { "deployWatch": false }, false ) ).toBeFalse();
					expect( ConfigServerImpl.metaDeployWatch.get( getConfigServer(), { "deployWatch": "false" }, false ) ).toBeFalse();
				});

				it( "can be explicitly enabled with deployWatch=true", function() {
					expect( ConfigServerImpl.metaDeployWatch.get( getConfigServer(), { "deployWatch": true }, false ) ).toBeTrue();
				});

			});

			describe( "system properties / environment variables", function() {

				it( "are documented with the matching names", function() {
					var settings = envVars().entries;
					var props = {};
					loop array=settings item="local.setting" {
						props[ setting.sysprop ] = setting.envvar;
					}
					expect( props ).toHaveKey( "lucee.deployWatch" );
					expect( props[ "lucee.deployWatch" ] ).toBe( "LUCEE_DEPLOYWATCH" );
					expect( props ).toHaveKey( "lucee.deploy.watch" );
					expect( props[ "lucee.deploy.watch" ] ).toBe( "LUCEE_DEPLOY_WATCH" );
				});

				it( "lucee.deploy.watch overrides the config", function() {
					var System = createObject( "java", "java.lang.System" );
					System.setProperty( "lucee.deploy.watch", "false" );
					try {
						expect( ConfigServerImpl.metaDeployWatch.get( getConfigServer(), { "deployWatch": true }, true ) ).toBeFalse();
					}
					finally {
						System.clearProperty( "lucee.deploy.watch" );
					}
					expect( ConfigServerImpl.metaDeployWatch.get( getConfigServer(), { "deployWatch": true }, true ) ).toBeTrue();
				});

				it( "lucee.deployWatch overrides the config", function() {
					var System = createObject( "java", "java.lang.System" );
					System.setProperty( "lucee.deployWatch", "false" );
					try {
						expect( ConfigServerImpl.metaDeployWatch.get( getConfigServer(), {}, true ) ).toBeFalse();
					}
					finally {
						System.clearProperty( "lucee.deployWatch" );
					}
					expect( ConfigServerImpl.metaDeployWatch.get( getConfigServer(), {}, true ) ).toBeTrue();
				});

			});

			describe( "the background controller", function() {

				it( "checks the deploy folder every 10 seconds by default", function() {
					expect( getConfigServer().isDeployWatch() ).toBeTrue();
					expect( Controler.isDeployCheckDue( true, getConfigServer() ) ).toBeTrue();
				});

				it( "does nothing between the 10 second intervals", function() {
					expect( Controler.isDeployCheckDue( false, getConfigServer() ) ).toBeFalse();
				});

				it( "does not check the deploy folder when deployWatch is disabled", function() {
					var System = createObject( "java", "java.lang.System" );
					var cs = getConfigServer();
					System.setProperty( "lucee.deploy.watch", "false" );
					try {
						cs.resetDeployWatch();
						expect( cs.isDeployWatch() ).toBeFalse();
						expect( Controler.isDeployCheckDue( true, cs ) ).toBeFalse();
						expect( Controler.isDeployCheckDue( false, cs ) ).toBeFalse();
					}
					finally {
						System.clearProperty( "lucee.deploy.watch" );
						cs.resetDeployWatch();
					}
					expect( cs.isDeployWatch() ).toBeTrue();
					expect( Controler.isDeployCheckDue( true, cs ) ).toBeTrue();
				});

			});

		});
	}

	private function getConfigServer() {
		return createObject( "java", "lucee.runtime.config.ConfigUtil" ).getConfigServerImpl( getPageContext().getConfig() );
	}

}
