component extends="org.lucee.cfml.test.LuceeTestCase" labels="regex" {

	function afterAll() {
		application action="update" regex={ type: "perl" };
	}

	function run( testResults, testBox ) {
		describe("LDEV-6435 — getApplicationSettings() must honestly report regex + AC class", function() {

			it( title="regex.type reports the AC's actual engine when set to java", body=function() {
				application action="update" regex={ type: "java" };
				var settings = getApplicationSettings();
				expect( settings.regex ).toBeStruct();
				expect( settings.regex.type ?: "" ).toBe( "java" );
			});

			it( title="regex.type reports the AC's actual engine when set to perl", body=function() {
				application action="update" regex={ type: "perl" };
				var settings = getApplicationSettings();
				expect( settings.regex.type ?: "" ).toBe( "perl" );
			});

			it( title="applicationContext key reports the AC class actually serving the request", body=function() {
				var settings = getApplicationSettings();
				expect( settings ).toHaveKey( "applicationContext" );
				// no Application.cfc/.cfm on path here → must be Classic
				expect( settings.applicationContext ).toBe( "ClassicApplicationContext" );
			});

			it( title="listenerType still reports the server-configured listener (unchanged)", body=function() {
				var settings = getApplicationSettings();
				expect( settings ).toHaveKey( "listenerType" );
				// listenerType is the server config — independent of which AC actually ran
				expect( settings.listenerType ).toBeString();
			});
		});
	}
}
