component extends="org.lucee.cfml.test.LuceeTestCase" labels="cookie" {

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV2900", function() {
			it( title='check cfcookie tag defaults, httponly, samesite=strict, path', body=function( currentSpec ) {
				uri = createURI( "LDEV2900" );
				local.sessionReq = _InternalRequest(
					template : "#uri#/tag-defaults/index.cfm",
					url: {
						samesite: "strict"
					}
				);
				//dumpResult( sessionReq );

				local.str = getCookieFromHeaders(sessionReq.headers, "value" );
				//dumpResult( str );

				expect( len( trim( str ) ) ).toBeGT( 0 );

				local.sct = toCookieStruct( str );
				//dumpResult( sct );
				expect( structKeyExists( sct, "HTTPOnly" ) ).toBeTrue();
				expect( sct.path ).toBe( "/test" );
				expect( sct.VALUE ).toBe( "LDEV2900" );
				expect( structKeyExists( sct, "SameSite" ) ).toBeTrue("samesite attribute should exist [#str#]");
				expect( sct.Samesite ).toBe( "strict" );
			});

			it( title='check cfcookie tag defaults, httponly, samesite=lax, path', body=function( currentSpec ) {
				uri = createURI( "LDEV2900" );
				local.sessionReq = _InternalRequest(
					template : "#uri#/tag-defaults/index.cfm",
					url: {
						samesite: "lax"
					}
				);
				//dumpResult( sessionReq );

				local.str = getCookieFromHeaders(sessionReq.headers, "value" );
				//dumpResult( str );

				expect( len( trim( str ) ) ).toBeGT( 0 );

				local.sct = toCookieStruct( str );
				//dumpResult( sct );
				expect( structKeyExists( sct, "HTTPOnly" ) ).toBeTrue();
				expect( sct.path ).toBe( "/test" );
				expect( sct.VALUE ).toBe( "LDEV2900" );
				expect( structKeyExists( sct, "SameSite" ) ).toBeTrue("samesite attribute should exist [#str#]");
				expect( sct.Samesite ).toBe( "lax" );
			});

			it( title='check cfcookie tag defaults, httponly, no samesite, path', body=function( currentSpec ) {
				uri = createURI( "LDEV2900" );
				local.sessionReq = _InternalRequest(
					template : "#uri#/tag-defaults/index.cfm"
				);
				//dumpResult( sessionReq );

				local.str = getCookieFromHeaders(sessionReq.headers, "value" );
				//dumpResult( str );

				expect( len( trim( str ) ) ).toBeGT( 0 );

				local.sct = toCookieStruct( str );
				//dumpResult( sct );
				expect( structKeyExists( sct, "HTTPOnly" ) ).toBeTrue();
				expect( sct.path ).toBe( "/test" );
				expect( sct.VALUE ).toBe( "LDEV2900" );
				expect( structKeyExists( sct, "SameSite" ) ).toBeFalse("samesite attribute should not be set [#str#]");
				expect( sct.Samesite?:"" ).toBe( "" );
			});

			it( title='check cfcookie tag defaults, httponly, no samesite, path', body=function( currentSpec ) {
				uri = createURI( "LDEV2900" );
				local.sessionReq = _InternalRequest(
					template : "#uri#/tag-defaults/index.cfm",
					url: {
						samesite: ""
					}
				);
				//dumpResult( sessionReq );

				local.str = getCookieFromHeaders(sessionReq.headers, "value" );
				//dumpResult( str );

				expect( len( trim( str ) ) ).toBeGT( 0 );

				local.sct = toCookieStruct( str );
				//dumpResult( sct );
				expect( structKeyExists( sct, "HTTPOnly" ) ).toBeTrue();
				expect( sct.path ).toBe( "/test" );
				expect( sct.VALUE ).toBe( "LDEV2900" );
				expect( structKeyExists( sct, "SameSite" ) ).toBeFalse("samesite attribute should not be set [#str#]");
				expect( sct.Samesite?:"" ).toBe( "" );
			});

			it( title='check cfcookie tag defaults, httponly, no samesite, path, tag samesite', body=function( currentSpec ) {
				uri = createURI( "LDEV2900" );
				local.sessionReq = _InternalRequest(
					template : "#uri#/tag-defaults/index.cfm",
					url: {
						tagSamesite: "none"
					}
				);
				//dumpResult( sessionReq );

				local.str = getCookieFromHeaders(sessionReq.headers, "value" );
				//dumpResult( str );

				expect( len( trim( str ) ) ).toBeGT( 0 );

				local.sct = toCookieStruct( str );
				//dumpResult( sct );
				expect( structKeyExists( sct, "HTTPOnly" ) ).toBeTrue();
				expect( sct.path ).toBe( "/test" );
				expect( sct.VALUE ).toBe( "LDEV2900" );
				expect( structKeyExists( sct, "SameSite" ) ).toBeTrue("samesite attribute should be set [#str#]");
				expect( sct.Samesite ).toBe( "none" );
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