component extends="org.lucee.cfml.test.LuceeTestCase" labels="cookie" {

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV2900", function() {
			it( title='check cfcookie tag defaults, httponly, samesite=strict, path', body=function( currentSpec ) {
				var uri = createURI( "LDEV2900" );
				var sessionReq = _InternalRequest(
					template : "#uri#/tag-defaults/index.cfm",
					url: {
						samesite: "strict"
					}
				);
				//dumpResult( sessionReq );

				var str = getCookieFromHeaders(sessionReq.headers, "value" );
				//dumpResult( str );

				expect( len( trim( str ) ) ).toBeGT( 0 );

				var sct = toCookieStruct( str );
				//dumpResult( sct );
				expect( structKeyExists( sct, "HTTPOnly" ) ).toBeTrue();
				expect( sct.path ).toBe( "/test" );
				expect( sct.VALUE ).toBe( "LDEV2900" );
				expect( structKeyExists( sct, "SameSite" ) ).toBeTrue("samesite attribute should exist [#str#]");
				expect( sct.Samesite ).toBe( "strict" );
			});

			it( title='check cfcookie tag defaults, httponly, samesite=lax, path', body=function( currentSpec ) {
				var uri = createURI( "LDEV2900" );
				var sessionReq = _InternalRequest(
					template : "#uri#/tag-defaults/index.cfm",
					url: {
						samesite: "lax"
					}
				);
				//dumpResult( sessionReq );

				var str = getCookieFromHeaders(sessionReq.headers, "value" );
				//dumpResult( str );

				expect( len( trim( str ) ) ).toBeGT( 0 );

				var sct = toCookieStruct( str );
				//dumpResult( sct );
				expect( structKeyExists( sct, "HTTPOnly" ) ).toBeTrue();
				expect( sct.path ).toBe( "/test" );
				expect( sct.VALUE ).toBe( "LDEV2900" );
				expect( structKeyExists( sct, "SameSite" ) ).toBeTrue("samesite attribute should exist [#str#]");
				expect( sct.Samesite ).toBe( "lax" );
			});

			it( title='check cfcookie tag defaults, httponly, no samesite, path', body=function( currentSpec ) {
				var uri = createURI( "LDEV2900" );
				var sessionReq = _InternalRequest(
					template : "#uri#/tag-defaults/index.cfm"
				);
				//dumpResult( sessionReq );

				var str = getCookieFromHeaders(sessionReq.headers, "value" );
				//dumpResult( str );

				expect( len( trim( str ) ) ).toBeGT( 0 );

				var sct = toCookieStruct( str );
				//dumpResult( sct );
				expect( structKeyExists( sct, "HTTPOnly" ) ).toBeTrue();
				expect( sct.path ).toBe( "/test" );
				expect( sct.VALUE ).toBe( "LDEV2900" );
				expect( structKeyExists( sct, "SameSite" ) ).toBeFalse("samesite attribute should not be set [#str#]");
				expect( sct.Samesite?:"" ).toBe( "" );
			});

			it( title='check cfcookie tag defaults, httponly, no samesite, path', body=function( currentSpec ) {
				var uri = createURI( "LDEV2900" );
				var sessionReq = _InternalRequest(
					template : "#uri#/tag-defaults/index.cfm",
					url: {
						samesite: ""
					}
				);
				//dumpResult( sessionReq );

				var str = getCookieFromHeaders(sessionReq.headers, "value" );
				//dumpResult( str );

				expect( len( trim( str ) ) ).toBeGT( 0 );

				var sct = toCookieStruct( str );
				//dumpResult( sct );
				expect( structKeyExists( sct, "HTTPOnly" ) ).toBeTrue();
				expect( sct.path ).toBe( "/test" );
				expect( sct.VALUE ).toBe( "LDEV2900" );
				expect( structKeyExists( sct, "SameSite" ) ).toBeFalse("samesite attribute should not be set [#str#]");
				expect( sct.Samesite?:"" ).toBe( "" );
			});

			it( title='check cfcookie tag defaults, httponly, no samesite, path, tag samesite', body=function( currentSpec ) {
				var uri = createURI( "LDEV2900" );
				var sessionReq = _InternalRequest(
					template : "#uri#/tag-defaults/index.cfm",
					url: {
						tagSamesite: "none"
					}
				);
				//dumpResult( sessionReq );

				var str = getCookieFromHeaders(sessionReq.headers, "value" );
				//dumpResult( str );

				expect( len( trim( str ) ) ).toBeGT( 0 );

				var sct = toCookieStruct( str );
				//dumpResult( sct );
				expect( structKeyExists( sct, "HTTPOnly" ) ).toBeTrue();
				expect( sct.path ).toBe( "/test" );
				expect( sct.VALUE ).toBe( "LDEV2900" );
				expect( structKeyExists( sct, "SameSite" ) ).toBeTrue("samesite attribute should be set [#str#]");
				expect( sct.Samesite ).toBe( "none" );
			});

			it( title='checking sessionCookie keys & values on getApplicationSettings()', body=function( currentSpec ) {
				var uri = createURI( "LDEV2900" );
				var sessionCookie = _InternalRequest(
					template : "#uri#/session-cookie/index.cfm"
				).filecontent.trim();
				var result = deserializeJSON(sessionCookie);

				expect( result.SAMESITE ).toBe("strict");
				expect( result.HTTPONLY ).toBeTrue();
				expect( result.DOMAIN ).toBe("www.edu.com");
				expect( result.PATH ).toBe("\test");
				expect( result.TIMEOUT ).toBe("1.0");
				expect( result.SECURE ).toBeTrue();

			});
		
		});

	};

	private string function getCookieFromHeaders( struct headers, string name ){
		var arr = arguments.headers[ 'Set-Cookie' ];
		var str = '';
		if ( isSimpleValue( arr ) ) arr = [ arr ]; // single cookies don't end up in an array
		loop array=arr item="local.entry" {
			if( findNoCase( arguments.name & '=', entry ) eq 1 )
				str = entry;
		}
		return str;
	}

	private struct function toCookieStruct( string str ){
		var arr = listToArray( str,';' );
		var sct={};
		loop array=arr item="local.entry" {
			sct[ trim( listFirst( entry, '=' ) ) ] = listLen( entry, '=' ) == 1 ? "" : trim( listLast( entry, '=' ) );
		}
		return sct;
	}

	private function dumpResult(r){
		// systemOutput( "---", true );
		// systemOutput( r, true );
		// systemOutput( "---", true );
 	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}
}