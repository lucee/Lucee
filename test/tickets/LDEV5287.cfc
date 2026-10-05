component extends="org.lucee.cfml.test.LuceeTestCase" labels="cookie" {

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV5287", function() {
			it( title='set cookie should set only one header', body=function( currentSpec ) {
				uri = createURI("LDEV5287");
				local.result = _InternalRequest(
					template : "#uri#/setMultipleCookiesSameName.cfm"
				);
				_dumpResult(local.result);
			 	expect( structCount( result.cookies ) ).toBe( 1 );
				var _cookies = _getCookies( result, "ldev5287" );
				expect( len( _cookies ) ).toBe( 1, "cookies returned [#_cookies.toJson()#]" );
				expect( result.cookies.ldev5287 ).toBe( 3 );
			});
		});
	}

	private function _getCookies( result, name ){
		var headers = result.headers[ "Set-Cookie" ] ?: [];
		var matches = [];
		for ( var header in headers ){
			if ( listFirst( header, "=" ) eq arguments.name )
				arrayAppend( matches, header );
		}
		return matches;
	}

	private function _dumpResult( result ){
		systemOutput( result.headers[ "Set-Cookie" ] ?: "[]", true );
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI&""&calledName;
	}
}