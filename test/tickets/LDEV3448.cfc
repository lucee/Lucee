component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV3448", function() {
			it( title='check cfml session cookie defaults, httponly, samesite=lax', body=function( currentSpec ) {
				uri = createURI( "LDEV3448" );
				local.sessionReq = _InternalRequest(
					template : "#uri#\session-cfml\index.cfm"
				);
				//dumpResult( sessionReq );

				local.str = getCookieFromHeaders(sessionReq.headers, "cfid" );
				//dumpResult( str );

				expect( len( trim( str ) ) ).toBeGT( 0 );
				local.sct = toCookieStruct( str );
				// dumpResult( sct );
				expect( structKeyExists( sct, "HTTPOnly" ) ).toBeTrue();
				expect( sct.Samesite ).toBe( "lax" );

			});

			it( title='check cfml session, httponly=false', body=function( currentSpec ) {
				uri = createURI( "LDEV3448" );
				local.sessionReq = _InternalRequest(
					template : "#uri#\session-cfml-no-httpOnly\index.cfm"
				);
				//dumpResult( sessionReq );

				local.str = getCookieFromHeaders(sessionReq.headers, "cfid" );
				//dumpResult( str );

				expect( len( trim( str ) ) ).toBeGT( 0 );
				local.sct = toCookieStruct( str );
				// dumpResult( sct );
				expect( structKeyExists( sct, "HTTPOnly" ) ).toBeFalse();
			});

			// TODO disabled, j2ee sessions aren't created by internal request (yet)
			it( title='check jee session cookie defaults, httponly, samesite=lax', skip=true, body=function( currentSpec ) {
				uri = createURI( "LDEV3448" );
				local.sessionReq = _InternalRequest(
					template : "#uri#\session-jee\index.cfm"
				);
				dumpResult( sessionReq );

				local.str = getCookieFromHeaders(sessionReq.headers, "jsessionid" );
				dumpResult( str );

				expect( len( trim( str ) ) ).toBeGT( 0 );

				local.sct = toCookieStruct( str );
				dumpResult( sct );
				expect( structKeyExists( sct, "HTTPOnly" ) ).toBeTrue();
				expect( sct.Samesite ).toBe( "lax" );

			});

		});
	}

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