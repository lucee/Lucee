// Regex mode-switching reflection: this.regex.type, legacy useJavaAsRegexEngine,
// and getApplicationSettings().regex round-trip. LDEV-4310 regression lives here.
component extends="org.lucee.cfml.test.LuceeTestCase" labels="regex" {

	function run( testResults, testBox ) {
		describe("regex mode switching — getApplicationSettings().regex reflection", function() {

			it( title="regex_type_java", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : fixtureURL("regex_type_java/index.cfm" )
				);
				expect( result.filecontent.trim() ).toBe( '{"TYPE":"java"}' );
			});

			it( title="regex_type_perl", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : fixtureURL("regex_type_perl/index.cfm" )
				);
				expect( result.filecontent.trim() ).toBe( '{"TYPE":"perl"}' );
			});

			it( title="useJavaAsRegexEngine_false check useJavaAsRegexEngine", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : fixtureURL("useJavaAsRegexEngine_false/index.cfm" ),
					url: {
						check: "useJavaAsRegexEngine"
					}
				);
				expect( result.filecontent.trim() ).toBe( serializeJSON("false") ); 
			});

			it( title="useJavaAsRegexEngine_true, check useJavaAsRegexEngine", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : fixtureURL("useJavaAsRegexEngine_true/index.cfm" ),
					url: { 
						check: "useJavaAsRegexEngine"
					}
				);
				expect( result.filecontent.trim() ).toBe( serializeJSON("true") ); 
			});

			it( title="useJavaAsRegexEngine_false, check regex", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : fixtureURL("useJavaAsRegexEngine_false/index.cfm" ),
					url: { 
						check: "regex"
					}
				);
				expect( result.filecontent.trim() ).toBe( '{"TYPE":"perl"}' ); 
			});

			it( title="useJavaAsRegexEngine_true, check regex", body=function( currentSpec ){
				local.result = _InternalRequest(
					template : fixtureURL("useJavaAsRegexEngine_true/index.cfm" ),
					url: { 
						check: "regex"
					}
				);
				expect( result.filecontent.trim() ).toBe( '{"TYPE":"java"}' );
			});

		});
	}

	// Build a server-resolvable URL for an _InternalRequest fixture.
	// Under script-runner / mvn test, the bootstrap creates a /test mapping at
	// the test root. Under browser/curl, cgi.script_name carries whatever
	// mapping is actually serving the request (e.g. /test71 on a dev instance).
	private string function fixtureURL( required string sub ) {
		if ( cgi.request_url eq "http://localhost/index.cfm" ) {
			return "/test/general/regex/Modes/" & sub;
		}
		return getDirectoryFromPath( cgi.script_name ) & "Modes/" & sub;
	}

}
