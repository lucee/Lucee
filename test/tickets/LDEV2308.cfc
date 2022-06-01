component extends="org.lucee.cfml.test.LuceeTestCase" labels="thread,cookie,session" {

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV2308", function() {
			it( title='JSessionID cookie should not be set by cfthread, no session', body=function( currentSpec ) {
				uri = createURI("LDEV2308");
				local.result = _InternalRequest(
					template : "#uri#\no-session\testThreadCookies.cfm"
				);
				//dumpResult(local.result);
			 	expect( structCount(result.cookies ) ).toBe( 0 );
				//expect( structKeyExists(result.cookies, "CFID" ) ).toBeFalse();
				//expect( structKeyExists(result.cookies, "JsessionId" ) ).toBeFalse();
			});

			it( title='JSessionID cookie should not be set by cfthread, set no client cookies', body=function( currentSpec ) {
				uri = createURI("LDEV2308");
				local.result = _InternalRequest(
					template : "#uri#\no-cookies\testThreadCookies.cfm"
				);
				//dumpResult(local.result);
			 	expect( structCount(result.cookies ) ).toBe( 0 );
				//expect( structKeyExists(result.cookies, "CFID" ) ).toBeFalse();
				//expect( structKeyExists(result.cookies, "JsessionId" ) ).toBeFalse();
			});

			it( title='JSessionID cookie should not be set by cfthread, cfml session', body=function( currentSpec ) {
				uri = createURI("LDEV2308");
				local.result = _InternalRequest(
					template : "#uri#\cfml-session\testThreadCookies.cfm"
				);
				//dumpResult(local.result);
				expect( structCount(result.cookies ) ).toBeGT( 0 );
				expect( structKeyExists(result.cookies, "CFID" ) ).toBeTrue();
				expect( structKeyExists(result.cookies, "JsessionId" ) ).toBeFalse();
			});

			it( title='CFID cookie should not be set by cfthread, j2ee session', body=function( currentSpec ) {
				uri = createURI("LDEV2308");
				local.result = _InternalRequest(
					template : "#uri#\j2ee-session\testThreadCookies.cfm"
				);
				//dumpResult(local.result);
				expect( structCount(result.cookies ) ).toBeGT( 0 );
			});
		});
	}

	private function dumpResult(r){
		systemOutput("", true);
		systemOutput(r.cookies, true);
		systemOutput(r.headers, true);
		systemOutput("", true);
	}
	
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI&""&calledName;
	}
}





