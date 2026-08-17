component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){
		describe( "Testcase for LDEV-6452 - a Prop based config update must fail when the setting is defined via a system property / environment variable", function(){

			// ConfigServerImpl.metaMainLoggerName is backed by the system property / env var "lucee.logging.main"
			// and is updated (via Prop.write) by the admin action "updateMainLog". A value defined via
			// system property / environment variable takes precedence over the config file, so updating it must
			// throw instead of silently writing a value that will be ignored.
			var propName = "lucee.logging.main";
			
			it( title="updateMainLog should throw while the setting is defined via a system property", body=function(){
				// "application" is also the default value, so defining it does not change any behaviour
				java.lang.System::setProperty( propName, "application" );
				try {
					var msg = "";
					try {
						admin
							action="updateMainLog"
							type="server"
							password=request.SERVERADMINPASSWORD
							mainLogger="something";
						fail( "updateMainLog should have thrown, because [#propName#] is defined via a system property" );
					}
					catch( any e ){
						msg = e.message;
					}
					expect( msg ).toInclude( "defined via the system property", "expected the error to explain the precedence, got: [#msg#]" );
					expect( msg ).toInclude( propName );
					expect( msg ).toInclude( "mainLogger" );
				}
				finally {
					java.lang.System::clearProperty( propName );
				}
			});

			it( title="updateMainLog should work again once the system property is removed", body=function(){
				// sanity check: without the system property the same update must not raise the precedence error
				java.lang.System::clearProperty( propName );
				var errored = false;
				var msg = "";
				try {
					admin
						action="updateMainLog"
						type="server"
						password=request.SERVERADMINPASSWORD
						mainLogger="application"; // keep the default so we do not change behaviour
				}
				catch( any e ){
					errored = true;
					msg = e.message;
				}
				expect( errored ).toBeFalse( "updateMainLog should not raise the precedence error without the system property, got: [#msg#]" );
			});

		});
	}
}
