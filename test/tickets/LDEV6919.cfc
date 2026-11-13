component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {

		describe( "LDEV-6919 requestTimeout attributes and settings are marked deprecated", function() {

			it( "cfsetting requestTimeout is deprecated", function() {
				var attr = getTagData( "cf", "setting" ).attributes.requestTimeout;
				expect( attr.status ).toBe( "deprecated" );
			});

			it( "cfapplication requestTimeout is deprecated", function() {
				var attr = getTagData( "cf", "application" ).attributes.requestTimeout;
				expect( attr.status ).toBe( "deprecated" );
			});

			it( "cfschedule requestTimeout is not deprecated (it is also the http timeout of the task)", function() {
				var attr = getTagData( "cf", "schedule" ).attributes.requestTimeout;
				expect( attr.status ).toBe( "implemented" );
			});

			it( "the request timeout system properties are deprecated", function() {
				var expected = [
					"lucee.requesttimeout",
					"lucee.requesttimeout.concurrentrequestthreshold",
					"lucee.requesttimeout.cputhreshold",
					"lucee.requesttimeout.memorythreshold"
				];
				var found = {};
				loop array=getSystemPropOrEnvVar() item="local.setting" {
					if ( arrayFindNoCase( expected, setting.sysprop ) ) {
						found[ setting.sysprop ] = setting.deprecated ?: "";
					}
				}
				loop array=expected item="local.sysprop" {
					expect( found ).toHaveKey( sysprop );
					expect( found[ sysprop ] ).toBe( "7.1.3" );
				}
			});

			it( "cfsetting requestTimeout still works", function() {
				var before = getPageContext().getRequestTimeout();
				try {
					setting requestTimeout=123;
					expect( getPageContext().getRequestTimeout() ).toBe( 123000 );
				}
				finally {
					getPageContext().setRequestTimeout( before );
				}
			});

		});
	}
}
