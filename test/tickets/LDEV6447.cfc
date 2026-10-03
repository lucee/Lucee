component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {

	function run( testResults, testBox ) {
		describe( "LDEV-6447: sessionInvalidate() JEE session lifecycle", function() {
			it( title="does not eagerly create a replacement JEE session", skip=isJsr223(), body=function( currentSpec ) {
				var data = request( "invalidate-before-commit.cfm" );
				expect( data.success ).toBeTrue( data.stacktrace ?: "no stacktrace" );
				expect( data.oldSessionInvalidated ).toBeTrue( "The existing HttpSession should be invalidated" );
				expect( data.replacementSessionExists ).toBeFalse( "sessionInvalidate() should not eagerly create a replacement HttpSession" );
				expect( data.sessionCreatedAfterAccess ).toBeTrue( "Accessing SESSION should lazily create a new HttpSession" );
				expect( data.oldSessionId ).notToBe( data.newSessionId, "The lazily-created session should have a new ID" );
			});

			it( title="invalidates an existing JEE session", skip=isJsr223(), body=function( currentSpec ) {
				var data = request( "invalidate-after-commit.cfm" );
				expect( data.success ).toBeTrue( data.stacktrace ?: "no stacktrace" );
				expect( data.sessionInvalidated ).toBeTrue( "The existing HttpSession should be invalidated" );
			});

			it( title="is a no-op when no JEE session exists", skip=isJsr223(), body=function( currentSpec ) {
				var data = request( "invalidate-after-commit-without-session.cfm" );
				expect( data.success ).toBeTrue( data.stacktrace ?: "no stacktrace" );
			});

		});
	}

	private boolean function isJsr223() {
		return cgi.request_url == "http://localhost/index.cfm";
	}

	private struct function request( required string template ) {
		var hostIdx = find( cgi.script_name, cgi.request_url );
		if ( hostIdx <= 0 ) {
			throw "Failed to extract host from CGI values";
		}

		var result = "";
		http method="get"
			url="#left( cgi.request_url, hostIdx - 1 )#/test/tickets/LDEV6447/jee-session/#template#"
			result="result";
		return deserializeJSON( result.filecontent );
	}
}
