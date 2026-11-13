component extends="org.lucee.cfml.test.LuceeTestCase" labels="controller" {

	function run( testResults, testBox ) {

		describe( "LDEV-3368 delay the first run of the background controller tasks", function() {

			var Controler = createObject( "java", "lucee.runtime.engine.Controler" );

			describe( "startup delay setting", function() {

				it( "defaults to 2 minutes", function() {
					expect( Controler.DEFAULT_STARTUP_DELAY_SECONDS ).toBe( 120 );
					expect( Controler.parseStartupDelay( javacast( "null", "" ) ) ).toBe( 120 * 1000 );
					expect( Controler.parseStartupDelay( "" ) ).toBe( 120 * 1000 );
					expect( Controler.parseStartupDelay( "   " ) ).toBe( 120 * 1000 );
				});

				it( "is configured in seconds", function() {
					expect( Controler.parseStartupDelay( "30" ) ).toBe( 30 * 1000 );
					expect( Controler.parseStartupDelay( " 45 " ) ).toBe( 45 * 1000 );
					expect( Controler.parseStartupDelay( "0" ) ).toBe( 0 );
				});

				it( "falls back to the default on an invalid value", function() {
					expect( Controler.parseStartupDelay( "abc" ) ).toBe( 120 * 1000 );
				});

				it( "treats a negative value as no delay", function() {
					expect( Controler.parseStartupDelay( "-5" ) ).toBe( 0 );
				});

				it( "is documented in sysprop-envvar.json", function() {
					var settings = getSystemPropOrEnvVar();
					var found = false;
					loop array=settings item="local.setting" {
						if ( setting.sysprop == "lucee.controller.startup.delay" ) {
							found = true;
							expect( setting.envvar ).toBe( "LUCEE_CONTROLLER_STARTUP_DELAY" );
							expect( setting.default ).toBe( 120 );
						}
					}
					expect( found ).toBeTrue();
				});

			});

			describe( "first run of the periodic tasks", function() {

				it( "without delay the first run is due one second after start, as before", function() {
					var start = 1000000;
					expect( Controler.getInitialLastRun( start, 0, 10000 ) + 10000 ).toBe( start + 1000 );
					expect( Controler.getInitialLastRun( start, 0, 60000 ) + 60000 ).toBe( start + 1000 );
					expect( Controler.getInitialLastRun( start, 0, 300000 ) + 300000 ).toBe( start + 1000 );
				});

				it( "with delay the first run is due one second after the delay has passed", function() {
					var start = 1000000;
					var delay = 120 * 1000;
					expect( Controler.getInitialLastRun( start, delay, 10000 ) + 10000 ).toBe( start + delay + 1000 );
					expect( Controler.getInitialLastRun( start, delay, 60000 ) + 60000 ).toBe( start + delay + 1000 );
					expect( Controler.getInitialLastRun( start, delay, 300000 ) + 300000 ).toBe( start + delay + 1000 );
				});

			});

		});
	}

}
