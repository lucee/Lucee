component extends="org.lucee.cfml.test.LuceeTestCase" labels="cookie" {

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV4756 Partitioned Cookies - tag cfcookie", function() {
			it( title='check cfcookie tag defaults, secure, partitioned, path', body=function( currentSpec ) {
				uri = createURI( "LDEV4756" );
				local.sessionReq = _InternalRequest(
					template : "#uri#/tag-defaults/index.cfm",
					url: {
						secure: true,
						partitioned: true,
						path: "/",
						tagDefaults: false
					}
				);
				//dumpResult( sessionReq );

				local.str = getCookieFromHeaders(sessionReq.headers, "value" );
				//dumpResult( str );

				expect( len( trim( str ) ) ).toBeGT( 0 );

				local.sct = toCookieStruct( str );
				//dumpResult( sct );
				expect( sct.path ).toBe( "/" );
				expect( sct.VALUE ).toBe( "LDEV4756" );
				expect( structKeyExists( sct, "Partitioned" ) ).toBeTrue("Partitioned attribute should exist [#str#]");
				expect( structKeyExists( sct, "secure" ) ).toBeTrue();
			});

			it( title='check cfcookie tag secure, partitioned, path', body=function( currentSpec ) {
				uri = createURI( "LDEV4756" );
				local.sessionReq = _InternalRequest(
					template : "#uri#/tag-defaults/index.cfm",
					url: {
						secure: true,
						partitioned: true,
						path: "/",
						tagDefaults: true
					}
				);
				//dumpResult( sessionReq );

				local.str = getCookieFromHeaders(sessionReq.headers, "value" );
				//dumpResult( str );

				expect( len( trim( str ) ) ).toBeGT( 0 );

				local.sct = toCookieStruct( str );
				//dumpResult( sct );
				expect( sct.path ).toBe( "/" );
				expect( sct.VALUE ).toBe( "LDEV4756" );
				expect( structKeyExists( sct, "Partitioned" ) ).toBeTrue("Partitioned attribute should exist [#str#]");
				expect( structKeyExists( sct, "secure" ) ).toBeTrue();
			});

			// currently not enforcing these client side business rules

			xit( title='check cfcookie tag Partitioned, no path', body=function( currentSpec ) {
				uri = createURI( "LDEV4756" );
				expect( function(){
					local.sessionReq = _InternalRequest(
					template : "#uri#/tag-defaults/index.cfm",
					url: {
						secure: true,
						partitioned: true,
						path: "",
						tagDefaults: false
					}
				);
				}).toThrow(); // Partitioned requires path="/"
			});

			xit( title='check cfcookie tag Partitioned, no secure', body=function( currentSpec ) {
				uri = createURI( "LDEV4756" );
				expect( function(){
					local.sessionReq = _InternalRequest(
					template : "#uri#/tag-defaults/index.cfm",
					url: {
						secure: true,
						partitioned: true,
						path: "/",
						tagDefaults: false
					}
				);
				}).toThrow(); // Partitioned requires secure="/"
			});

			xit( title='check cfcookie tag Partitioned, no secure, no path', body=function( currentSpec ) {
				uri = createURI( "LDEV4756" );
				expect( function(){
					local.sessionReq = _InternalRequest(
					template : "#uri#/tag-defaults/index.cfm",
					url: {
						secure: false,
						partitioned: true,
						path: "",
						tagDefaults: false
					}
				);
				}).toThrow(); // Partitioned requires path="/" and secure
			});
		});

		describe( "Test suite for LDEV4756 Partitioned Session cookies ", function() {
			it( title='check cfml session cookie partitioned: true', body=function( currentSpec ) {
				uri = createURI( "LDEV4756" );
				local.sessionReq = _InternalRequest(
					template : "#uri#\session-cookie\index.cfm",
					url: {
						partitioned: true
					}
				);
				//dumpResult( sessionReq );
				local.str = getCookieFromHeaders(sessionReq.headers, "cfid" );
				//dumpResult( str );
				expect( len( trim( str ) ) ).toBeGT( 0 );
				local.sct = toCookieStruct( str );
				// dumpResult( sct );
				expect( structKeyExists( sct, "Partitioned" ) ).toBeTrue();
			});

			it( title='check cfml session cookie partitioned: false', body=function( currentSpec ) {
				uri = createURI( "LDEV4756" );
				local.sessionReq = _InternalRequest(
					template : "#uri#\session-cookie\index.cfm",
					url: {
						partitioned: false
					}
				);
				//dumpResult( sessionReq );
				local.str = getCookieFromHeaders(sessionReq.headers, "cfid" );
				//dumpResult( str );
				expect( len( trim( str ) ) ).toBeGT( 0 );
				local.sct = toCookieStruct( str );
				// dumpResult( sct );
				expect( structKeyExists( sct, "Partitioned" ) ).toBeFalse();
			});

			it( title='check cfml session cookie partitioned: unset', body=function( currentSpec ) {
				uri = createURI( "LDEV4756" );
				local.sessionReq = _InternalRequest(
					template : "#uri#\session-cookie\index.cfm",
					url: {
						partitioned: ""
					}
				);
				//dumpResult( sessionReq );
				local.str = getCookieFromHeaders(sessionReq.headers, "cfid" );
				//dumpResult( str );
				expect( len( trim( str ) ) ).toBeGT( 0 );
				local.sct = toCookieStruct( str );
				// dumpResult( sct );
				expect( structKeyExists( sct, "Partitioned" ) ).toBeFalse();

			});
		});

		describe( "Test suite for LDEV4756 Partitioned Session Cookies - getApplicationSettings() ", function() {
		
			it( title='checking sessionCookie keys & values on getApplicationSettings() partitioned: true', body=function( currentSpec ) {
				uri = createURI( "LDEV4756" );
				local.sessionCookie = _InternalRequest(
					template : "#uri#/session-cookie/index.cfm",
					url: {
						partitioned: true
					}
				).filecontent.trim();
				result = deserializeJSON( sessionCookie );

				expect( result.SAMESITE ).toBe("none");
				expect( result.HTTPONLY ).toBeTrue();
				expect( result.DOMAIN ).toBe("www.lucee.org");
				expect( result.PATH ).toBe("/");
				expect( result.TIMEOUT ).toBe("1.0");
				expect( result.SECURE ).toBeTrue();
				expect( result.PARTITIONED ).toBeTrue();

			});

			 it( title='checking sessionCookie keys & values on getApplicationSettings(), partitioned: false', body=function( currentSpec ) {
				uri = createURI( "LDEV4756" );
				local.sessionCookie = _InternalRequest(
					template : "#uri#/session-cookie/index.cfm",
					url: {
						partitioned: false
					}
				).filecontent.trim();
				result = deserializeJSON( sessionCookie );

				expect( result.SAMESITE ).toBe("none");
				expect( result.HTTPONLY ).toBeTrue();
				expect( result.DOMAIN ).toBe("www.lucee.org");
				expect( result.PATH ).toBe("/");
				expect( result.TIMEOUT ).toBe("1.0");
				expect( result.SECURE ).toBeTrue();
				expect( result.PARTITIONED ).toBeFalse();

			});

		});

	};

	private string function getCookieFromHeaders( struct headers, string name ){
		local.arr = arguments.headers[ 'Set-Cookie' ];
		local.str = '';
		loop array=arr item="local.entry" {
			if( findNoCase( arguments.name & '=', entry ) eq 1 )
				str = entry;
		}
		return str;
	}

	private struct function toCookieStruct( string str ){
		local.arr = listToArray( str,';' );
		local.sct={};
		loop array=arr item="local.entry" {
			sct[ trim( listFirst( entry, '=' ) ) ] = listLen( entry, '=' ) == 1 ? "" : trim( listLast( entry, '=' ) );
		}
		return sct;
	}

	private function dumpResult(r){
		systemOutput( "---", true );
		systemOutput( r, true );
		systemOutput( "---", true );
 	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}
}