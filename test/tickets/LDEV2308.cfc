component extends="org.lucee.cfml.test.LuceeTestCase" labels="thread" {

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV2308", function() {
			it( title='JSessionID cookie should not be set by cfthread, no session', body=function( currentSpec ) {
				uri = createURI("LDEV2308");
				local.result = _InternalRequest(
					template : "#uri#\no-session\testThreadCookies.cfm"
				);
			 	//systemOutput(local.result.cookies, true);
			 	expect( structCount(result.cookies ) ).toBe( 0 );
				expect( structKeyExists(result.cookies, "CFID" ) ).toBeFalse();
				expect( structKeyExists(result.cookies, "JsessionId" ) ).toBeFalse();
			});

			it( title='JSessionID cookie should not be set by cfthread, set no client cookies', body=function( currentSpec ) {
				uri = createURI("LDEV2308");
				local.result = _InternalRequest(
					template : "#uri#\no-cookies\testThreadCookies.cfm"
				);
			 	//systemOutput(local.result.cookies, true);
			 	expect( structCount(result.cookies ) ).toBe( 0 );
				expect( structKeyExists(result.cookies, "CFID" ) ).toBeFalse();
				expect( structKeyExists(result.cookies, "JsessionId" ) ).toBeFalse();
			});

			it( title='JSessionID cookie should not be set by cfthread, cfml session', body=function( currentSpec ) {
				uri = createURI("LDEV2308");
				local.result = _InternalRequest(
					template : "#uri#\cfml-session\testThreadCookies.cfm"
				);
				//systemOutput(local.result.cookies, true);
				expect( structCount(result.cookies ) ).toBeGT( 0 );
				expect( structKeyExists(result.cookies, "CFID" ) ).toBeTrue();
				expect( structKeyExists(result.cookies, "JsessionId" ) ).toBeFalse();
			});
		});
	}
	
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI&""&calledName;
	}
}





