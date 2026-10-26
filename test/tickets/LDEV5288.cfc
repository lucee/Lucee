component extends="org.lucee.cfml.test.LuceeTestCase" labels="cookie,session" {

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV5288", function() {
			it( title='internalRequest should return jsessionId cookie', body=function( currentSpec ) {
				var result = _InternalRequest(
					template : "#createURI("LDEV5288")#/ldev5288-createJsession.cfm"
				);
				var _cookies = _getCookies( result, "jsessionId" );
				expect( len( _cookies ) ).toBe( 1, "cookies returned [#serializeJson( result.headers[ "Set-Cookie" ] ?: [] )#]" );
				var jsessionid = listRest( listFirst( _cookies[ 1 ], ";" ), "=" );
				expect( jsessionid ).notToBeEmpty();
				expect( result.session.sessionid ).toBe( jsessionid );
				expect( result.session.urltoken ).toInclude( "jsessionid=" & jsessionid );
			});

			it( title='internalRequest with the jsessionId cookie continues the same jee session', body=function( currentSpec ) {
				var uri = createURI("LDEV5288");
				var first = _InternalRequest(
					template : "#uri#/ldev5288-createJsession.cfm"
				);
				var jsessionid = first.session.sessionid;
				expect( first.session.counter ).toBe( 1 );

				// only the jsessionid, a jee session must not depend on cfid/cftoken
				var second = _InternalRequest(
					template : "#uri#/ldev5288-createJsession.cfm",
					cookies : { jsessionid: jsessionid }
				);
				expect( second.session.sessionid ).toBe( jsessionid );
				expect( second.session.counter ).toBe( 2 );

				// without the cookie, a new session is created
				var third = _InternalRequest(
					template : "#uri#/ldev5288-createJsession.cfm"
				);
				expect( third.session.sessionid ).notToBe( jsessionid );
				expect( third.session.counter ).toBe( 1 );
			});
		});
	}

	private function _getCookies( result, name ){
		var headers = result.headers[ "Set-Cookie" ] ?: [];
		if ( isSimpleValue( headers ) ) headers = [ headers ];
		var matches = [];
		for ( var header in headers ){
			if ( listFirst( header, "=" ) eq arguments.name )
				arrayAppend( matches, header );
		}
		return matches;
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
