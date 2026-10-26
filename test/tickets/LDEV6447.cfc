component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {

	function run( testResults, testBox ) {
		describe( "LDEV-6447: sessionInvalidate() session lifecycle", function() {
			it( title="cfml session: does not eagerly create a replacement session", body=function( currentSpec ) {
				assertLazyReplacement( "cfml" );
			});

			it( title="jee session: does not eagerly create a replacement session", body=function( currentSpec ) {
				assertLazyJeeReplacement();
			});
		});

		describe( "LDEV-6447: sessionInvalidate() with cache session storage", function() {
			it( title="removes the old session from storage and doesn't write it back", body=function( currentSpec ) {
				assertCacheStorageInvalidated( false );
			});

			it( title="removes the old session from storage and doesn't write it back with sessionCluster=true", body=function( currentSpec ) {
				assertCacheStorageInvalidated( true );
			});
		});

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

	private function assertLazyReplacement( required string sessionType ) {
		var result = _InternalRequest(
			template: createURI( "LDEV6447/lifecycle/invalidate.cfm" ),
			url: { sessionType: arguments.sessionType }
		);
		var data = deserializeJSON( result.filecontent.trim() );

		expect( data.oldSessionExists ).toBeFalse( "The old session should be removed" );
		expect( data.cfidChanged ).toBeTrue( "sessionInvalidate() should issue a new cfid" );
		expect( data.replacementCreatedEagerly ).toBeFalse( "sessionInvalidate() should not eagerly create a replacement session" );
		expect( data.sessionCreatedOnAccess ).toBeTrue( "Accessing SESSION should lazily create a new session" );
		expect( data.newSessionId ).notToBe( data.oldSessionId );
		expect( data.hasUserId ).toBeFalse( "The new session should not contain data from the old session" );
	}

	private function assertLazyJeeReplacement() {
		var uri = createURI( "LDEV6447/lifecycle" );
		var result = _InternalRequest(
			template: "#uri#/invalidate-jee.cfm",
			url: { sessionType: "jee" }
		);
		var data = deserializeJSON( result.filecontent.trim() );

		// the jee session id is the id of the HttpSession
		expect( data.oldHttpSessionId ).notToBeEmpty( "A jee session should be backed by an HttpSession" );
		expect( data.oldSessionId ).toBe( data.oldHttpSessionId );
		expect( data.oldSessionExists ).toBeFalse( "The old session should be removed" );
		expect( data.cfidChanged ).toBeTrue( "sessionInvalidate() should issue a new cfid" );
		expect( data.replacementCreatedEagerly ).toBeFalse( "sessionInvalidate() should not eagerly create a replacement session" );
		expect( data.sessionCreatedOnAccess ).toBeTrue( "Accessing SESSION should lazily create a new session" );
		expect( data.newSessionId ).toBe( data.newHttpSessionId );
		expect( data.newSessionId ).notToBe( data.oldSessionId );
		expect( data.hasUserId ).toBeFalse( "The new session should not contain data from the old session" );
		expect( result.session.sessionid ).toBe( data.newSessionId );
		var jsessionCookies = getSetCookies( result, "jsessionid" );
		expect( len( jsessionCookies ) ).toBe( 1, "cookies returned [#serializeJson( result.headers[ "Set-Cookie" ] ?: [] )#]" );
		expect( listRest( listFirst( jsessionCookies[ 1 ], ";" ), "=" ) ).toBe( data.newSessionId );

		// the old jsessionid no longer gives access to the invalidated session
		var old = _InternalRequest(
			template: "#uri#/check.cfm",
			url: { sessionType: "jee" },
			cookies: { jsessionid: data.oldSessionId }
		);
		var oldData = deserializeJSON( old.filecontent.trim() );
		expect( oldData.sessionId ).notToBe( data.oldSessionId );
		expect( oldData.sessionId ).notToBe( data.newSessionId );
		expect( oldData.hasUserId ).toBeFalse( "The old jsessionid should not give access to the old session data" );

		// the replacement jsessionid continues the replacement session
		var current = _InternalRequest(
			template: "#uri#/check.cfm",
			url: { sessionType: "jee" },
			cookies: { jsessionid: data.newSessionId }
		);
		expect( deserializeJSON( current.filecontent.trim() ).sessionId ).toBe( data.newSessionId );
	}

	private function assertCacheStorageInvalidated( required boolean sessionCluster ) {
		var uri = createURI( "LDEV6447/cache-storage" );

		var result = _InternalRequest(
			template: "#uri#/createSession.cfm",
			url: { sessionCluster: arguments.sessionCluster }
		);
		var created = deserializeJSON( result.filecontent.trim() );
		var oldCookies = { cfid: created.cfid, cftoken: created.cftoken };

		result = _InternalRequest(
			template: "#uri#/invalidateSession.cfm",
			url: { sessionCluster: arguments.sessionCluster },
			cookies: oldCookies
		);
		var invalidated = deserializeJSON( result.filecontent.trim() );
		expect( invalidated.oldSessionId ).toBe( created.sessionId );
		expect( invalidated.hadUserId ).toBeTrue();
		expect( invalidated.storedBeforeInvalidate ).toBeTrue( "The session should be in cache storage before sessionInvalidate()" );
		expect( invalidated.storedAfterInvalidate ).toBeFalse( "sessionInvalidate() should remove the old session from cache storage" );
		expect( invalidated.newSessionId ).notToBe( created.sessionId );
		expect( invalidated.hasUserId ).toBeFalse( "The new session should not contain data from the old session" );

		// a later request, after the invalidating request has stored its scopes, still using the old cookies
		result = _InternalRequest(
			template: "#uri#/checkSession.cfm",
			url: {
				sessionCluster: arguments.sessionCluster,
				oldStorageKey: invalidated.oldStorageKey,
				newStorageKey: invalidated.newStorageKey
			},
			cookies: oldCookies
		);
		var checked = deserializeJSON( result.filecontent.trim() );
		expect( checked.oldStored ).toBeFalse( "The old session should not be written back to cache storage under the old cfid" );
		expect( checked.newStored ).toBeTrue( "The replacement session should be stored under its new cfid" );
		expect( checked.hasUserId ).toBeFalse( "The old cfid should not give access to the old session data" );
	}

	private array function getSetCookies( required struct result, required string name ) {
		var headers = arguments.result.headers[ "Set-Cookie" ] ?: [];
		if ( isSimpleValue( headers ) ) headers = [ headers ];
		var matches = [];
		for ( var header in headers ) {
			if ( listFirst( header, "=" ) eq arguments.name ) arrayAppend( matches, header );
		}
		return matches;
	}

	private string function createURI( required string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}

	// these specs need a real servlet container, as the committed response handling and
	// HttpSession lifecycle can't be reproduced with internalRequest() under JSR-223
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
