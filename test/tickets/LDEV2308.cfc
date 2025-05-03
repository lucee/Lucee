component extends="org.lucee.cfml.test.LuceeTestCase" labels="thread,cookie,session" {

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV2308", function() {
			it( title='JSessionID cookie should not be set by cfthread, no session', body=function( currentSpec ) {
				var result = test(
					template : "/no-session/testThreadCookies.cfm"
				);
				//dumpResult(local.result);
			 	expect( structCount(result.cookies ) ).toBe( 0 );
			});

			it( title='JSessionID cookie should not be set by cfthread, set no client cookies', body=function( currentSpec ) {
				var uri = createURI("LDEV2308");
				var result = test(
					template : "/no-cookies/testThreadCookies.cfm"
				);
				//dumpResult(local.result);
				expect( structCount(result.cookies ) ).toBe( 0 );
			});

			it( title='JSessionID cookie should not be set by cfthread, cfml session', body=function( currentSpec ) {
				var uri = createURI("LDEV2308");
				var result = test(
					template : "/cfml-session/testThreadCookies.cfm"
				);
				//dumpResult(local.result);
				expect( structCount(result.cookies ) ).toBeGT( 0 );
				expect( structKeyExists(result.cookies, "CFID" ) ).toBeTrue();
				expect( structKeyExists(result.cookies, "JsessionId" ) ).toBeFalse();
			});

			// test disabled, see LDEV-4030 & LDEV-2954
			it( title='No cookies should be set by cfthread, j2ee session', skip=isJsr232(), body=function( currentSpec ) {				
				var result = test(
					template : "/j2ee-session/testThreadCookies.cfm"
				);
				dumpResult(local.result);
				systemOutput(result.cookies, true);
				expect( structCount(result.cookies ) ).toBe( 0 );
			});

		});
	}

	private function isJsr232(){
		return (cgi.request_url eq "http://localhost/index.cfm");
	}

	private function test(template, args={}){
		var jsr223 = isJsr232();
		if ( jsr223 ){ 
			var uri = createURI("LDEV2308");
			var result = internalRequest(
				template : uri & arguments.template
			);
			return result;
		} else {
			// running via a web browser, let's try http, which also supports jee sessions
			var hostIdx = find(cgi.script_name, cgi.request_url);
			if (hostIdx gt 0){
				var host = left(cgi.request_url, hostIdx-1);
				var webUrl = host & "/test/tickets/LDEV2308" & arguments.template;
				//systemOutput("could do http! testing via [#webUrl#]", true);
			} else {
				throw "failed to extract host [#hostIdx#] from cgi [#cgi.script_name#], [#cgi.request_url#]";
			}
			var httpResult = "";
			http method="get" url="#webUrl#" result="httpResult"{
				structEach(arguments.args, function(k,v){
					httpparam name="#k#" value="#v#" type="url";
				});
			}

			//debug(httpResult,"cfhttp - #template#");

			// force cfhttp result to be like internalRequest result;
			httpResult.cookies = queryToStruct(httpResult.cookies, "name");
			httpResult.headers = httpResult.responseHeader;
			//debug(httpResult,"cfhttp - #template#");
			/*
			expect( structCount( httpResult.cookies ) ).toBe( structCount( result.cookies ),
				"cfhttp [#httpResult.cookies.toJson()#] differs from internalRequest [#result.cookies.toJson()#]" );
			*/
		}
		return httpResult;
	}


	private function dumpResult(r){
		// systemOutput("", true);
		// systemOutput("Cookies: " & serializeJson(r.cookies), true);
		// systemOutput("Headers: " & serializeJson(r.headers), true);
		// systemOutput("", true);
	}

	private string function createURI(string calledName, boolean contract=false){
		var base = getDirectoryFromPath( getCurrentTemplatePath() );
		var baseURI = contract ? contractPath( base ) : "/test/#listLast(base,"\/")#";
		return baseURI & "/" & calledName;
	}
}