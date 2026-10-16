component extends="org.lucee.cfml.test.LuceeTestCase" labels="internalrequest,cookie" {

	function run( testResults, testBox ) {
		describe( "LDEV-6374 InternalRequest preserves Set-Cookie order", function() {

			it( "duplicate Set-Cookie headers keep insertion order", function() {
				var uri = createURI( "LDEV6374" );
				var result = _InternalRequest( template: "#uri#/setcookies.cfm" );
				var cfidHeaders = _getCfidSetCookies( result );
				expect( arrayLen( cfidHeaders ) ).toBe( 2 );
				expect( find( "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", cfidHeaders[ 1 ] ) ).toBeGT( 0 );
				expect( find( "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb", cfidHeaders[ 2 ] ) ).toBeGT( 0 );
				expect( result.cookies.cfid ).toBe( "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb" );
			});

			it( "sessionRotate last cfid Set-Cookie matches session scope", function() {
				var uri = createURI( "LDEV6374" );
				var incomingCfid = createGUID();
				var result = _InternalRequest(
					template: "#uri#/sessionrotate.cfm",
					cookies: { cfid: incomingCfid, cftoken: 0 }
				);
				var afterRotate = result.request.cfidAfterSessionRotate;
				var cfidHeaders = _getCfidSetCookies( result );
				expect( arrayLen( cfidHeaders ) ).toBeGT( 0 );
				expect( result.session.cfid ).toBe( afterRotate );
				expect( result.cookies.cfid ).toBe( afterRotate );
				expect( find( afterRotate, cfidHeaders[ arrayLen( cfidHeaders ) ] ) ).toBeGT( 0 );
				expect( afterRotate ).notToBe( incomingCfid );
			});

		});
	}

	private array function _getCfidSetCookies( required struct result ) {
		var setCookies = arguments.result.headers[ "Set-Cookie" ] ?: [];
		var cfidHeaders = [];
		for ( var header in setCookies ) {
			if ( lCase( listFirst( header, "=" ) ) == "cfid" ) {
				arrayAppend( cfidHeaders, header );
			}
		}
		return cfidHeaders;
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}

}
