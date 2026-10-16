component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {

	function run( testResults, testBox ) {
		describe( "LDEV-5942: sessionRotate() should rotate JSESSIONID for J2EE sessions", function() {

			it( title="sessionRotate() rotates JSESSIONID for J2EE sessions", skip=isJsr223(), body=function( currentSpec ) {
				var result = test( template: "/jee-session/rotate.cfm" );
				var data = deserializeJSON( result.filecontent );
				expect( data.success ).toBeTrue( data.message ?: "no message" );
				expect( data.oldSessionId ).notToBe( data.newSessionId, "JSESSIONID should change after sessionRotate()" );
			});

			it( title="sessionRotate() preserves session data for J2EE sessions", skip=isJsr223(), body=function( currentSpec ) {
				var result = test( template: "/jee-session/rotate-with-data.cfm" );
				var data = deserializeJSON( result.filecontent );
				expect( data.success ).toBeTrue( data.message ?: "no message" );
				expect( data.oldSessionId ).notToBe( data.newSessionId, "JSESSIONID should change after sessionRotate()" );
				expect( data.dataPreserved ).toBeTrue( "Session data should be preserved after rotation" );
			});

		});

		describe( "LDEV-3248: sessionInvalidate() should invalidate JSESSIONID for J2EE sessions", function() {

			it( title="sessionInvalidate() invalidates JSESSIONID for J2EE sessions", skip=isJsr223(), body=function( currentSpec ) {
				var result = test( template: "/jee-session/invalidate.cfm" );
				var data = deserializeJSON( result.filecontent );
				expect( data.success ).toBeTrue( data.message ?: "no message" );
				expect( data.sessionInvalidated ).toBeTrue( "HttpSession should be invalidated after sessionInvalidate()" );
			});

		});
	}

	private function isJsr223() {
		// Skip when running via script-runner (JSR-223) as J2EE sessions require a real servlet container
		return ( cgi.request_url eq "http://localhost/index.cfm" );
	}

	private function test( template, args={} ) {
		if ( isJsr223() ) {
			// Running via script-runner, use internalRequest (but J2EE tests will be skipped)
			var uri = createURI( "LDEV5942" );
			var result = _InternalRequest(
				template: uri & arguments.template
			);
			return result;
		}
		else {
			// Running via Tomcat, use cfhttp which supports real J2EE sessions
			var hostIdx = find( cgi.script_name, cgi.request_url );
			if ( hostIdx gt 0 ) {
				var host = left( cgi.request_url, hostIdx - 1 );
				var webUrl = host & "/test/tickets/LDEV5942" & arguments.template;
			}
			else {
				throw "failed to extract host [#hostIdx#] from cgi [#cgi.script_name#], [#cgi.request_url#]";
			}
			var httpResult = "";
			http method="get" url="#webUrl#" result="httpResult" {
				structEach( arguments.args, function( k, v ) {
					httpparam name="#k#" value="#v#" type="url";
				});
			}

			return httpResult;
		}
	}

	private function dumpResult( r ) {
		systemOutput( "", true );
		systemOutput( "Result: " & serializeJson( r ), true );
		systemOutput( "", true );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}

}
