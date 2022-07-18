
component extends="org.lucee.cfml.test.LuceeTestCase" labels="cookie" skip=true	{

	public void function testCookieEncode(){
		var uri = createURI("cookie/encode.cfm")
		local.res = _InternalRequest(
			template: uri
		);
		dumpCookies(local.res);

		expect( getCookie(res, "simple") ).toBe("lucee"); 

		expect( getCookie(res, "ENCODED_GUID") ).toBe("376B3346-463E-4F79-83FFE7C41451304A");
		expect( getCookie(res, "ENCODED_HTML") ).toBe("space%26%20%26%20space%20%26%20%26nbsp%3B");
		expect( getCookie(res, "ENCODED_DELIMS") ).toBe("%3B%2C%3D");
		
		expect( getCookie(res, "GUID") ).toBe("376B3346-463E-4F79-83FFE7C41451304A");
		expect( getCookie(res, "HTML") ).toBe("space& & space & &nbsp;");
		expect( getCookie(res, "DELIMS", true) ).toInclude("COOKIE_TEST3=;,=;"); // this will fail if not encoded as the delimiters should be encoded

		expect( getCookie(res, "preservecase_simple", true) ).toIncludeWithCase("preservecase_simple="); 
	}

	private function dumpCookies(res){
		systemOutput( "", true );
		var c = arguments.res.headers["Set-Cookie"];
		loop array=c item="local.cc"{
			systemOutput( cc, true );
		}
	}

	private function getCookie( res, string cookieName, boolean raw=false ){
		var c = arguments.res.headers["Set-Cookie"];
		loop array=c item="local.cc"{
			if ( listFirst( cc, "=" ) eq arguments.cookieName ){
				if ( arguments.raw )
					return cc;
				return listLast( listFirst( cc, ";" ), "=" );
			}
		}
		return "--cookie not found--";
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}
