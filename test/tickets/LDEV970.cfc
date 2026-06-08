component extends="org.lucee.cfml.test.LuceeTestCase" labels="cookie" {

	function run( testResults, testBox ) {
		describe( "LDEV-970 cookie name case preservation", function() {

			beforeEach( function() {
				// enable preserve case default so we can test the new behaviour
				var impl = createObject( "java", "lucee.runtime.type.scope.CookieImpl" );
				impl.PRESERVE_CASE_DEFAULT = true;
			} );

			afterEach( function() {
				// restore original default
				var impl = createObject( "java", "lucee.runtime.type.scope.CookieImpl" );
				impl.PRESERVE_CASE_DEFAULT = false;
			} );

			var uri = createURI( "LDEV970" );

			it( "dot notation preserves case with sysprop enabled", function() {
				var res = _InternalRequest( template: "#uri#/index.cfm", url: { method: "dot" } );
				expect( getCookieName( res.headers, "testcookie" ) ).toBe( "testCookie" );
			} );

			it( "bracket notation preserves case with sysprop enabled", function() {
				var res = _InternalRequest( template: "#uri#/index.cfm", url: { method: "bracket" } );
				expect( getCookieName( res.headers, "testcookie" ) ).toBe( "testCookie" );
			} );

			it( "struct assignment preserves case with sysprop enabled", function() {
				var res = _InternalRequest( template: "#uri#/index.cfm", url: { method: "struct" } );
				expect( getCookieName( res.headers, "testcookie" ) ).toBe( "testCookie" );
			} );

			it( "cfcookie tag preserves case with sysprop enabled", function() {
				var res = _InternalRequest( template: "#uri#/index.cfm", url: { method: "tag" } );
				expect( getCookieName( res.headers, "testcookie" ) ).toBe( "testCookie" );
			} );

			it( "explicit preservecase=true always preserves regardless of sysprop", function() {
				var impl = createObject( "java", "lucee.runtime.type.scope.CookieImpl" );
				impl.PRESERVE_CASE_DEFAULT = false;
				var res = _InternalRequest( template: "#uri#/index.cfm", url: { method: "struct", pc: "true" } );
				expect( getCookieName( res.headers, "testcookie" ) ).toBe( "testCookie" );
			} );

			it( "explicit preservecase=false always uppercases regardless of sysprop", function() {
				var res = _InternalRequest( template: "#uri#/index.cfm", url: { method: "struct", pc: "false" } );
				expect( getCookieName( res.headers, "testcookie" ) ).toBe( "TESTCOOKIE" );
			} );

			it( "explicit preservecase=false on cfcookie tag always uppercases", function() {
				var res = _InternalRequest( template: "#uri#/index.cfm", url: { method: "tag", pc: "false" } );
				expect( getCookieName( res.headers, "testcookie" ) ).toBe( "TESTCOOKIE" );
			} );

			it( "default behaviour (sysprop=false) uppercases cookie name", function() {
				var impl = createObject( "java", "lucee.runtime.type.scope.CookieImpl" );
				impl.PRESERVE_CASE_DEFAULT = false;
				var res = _InternalRequest( template: "#uri#/index.cfm", url: { method: "bracket" } );
				expect( getCookieName( res.headers, "testcookie" ) ).toBe( "TESTCOOKIE" );
			} );

		} );
	}

	// finds the raw cookie name in Set-Cookie headers by case-insensitive match
	private string function getCookieName( required struct headers, required string name ) {
		var setCookies = arguments.headers[ 'Set-Cookie' ] ?: [];
		if ( isSimpleValue( setCookies ) ) setCookies = [ setCookies ];
		loop array=setCookies item="local.entry" {
			var rawName = trim( listFirst( entry, '=' ) );
			if ( compareNoCase( rawName, arguments.name ) eq 0 )
				return rawName;
		}
		return "";
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}

}
