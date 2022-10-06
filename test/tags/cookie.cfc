
component extends="org.lucee.cfml.test.LuceeTestCase" labels="cookie" {

	public void function testCookieEncode(){
		var uri = createURI("cookie/encode.cfm")
		local.res = _InternalRequest(
			template: uri
		);
		//dumpCookies(local.res);

		expect( getCookie(res, "simple") ).toBe("lucee"); 

		expect( getCookie(res, "ENCODED_GUID") ).toBe("376B3346-463E-4F79-83FFE7C41451304A");
		expect( getCookie(res, "ENCODED_HTML",false,false) ).toBe("space%26%20%26%20space%20%26%20%26nbsp%3B");
		expect( getCookie(res, "ENCODED_DELIMS") ).toBe(";,=");
		
		expect( getCookie(res, "GUID") ).toBe("376B3346-463E-4F79-83FFE7C41451304A");
		expect( getCookie(res, "HTML") ).toBe("space& & space & &nbsp"); // TODO shouldn't there be a trailing ;?
		expect( getCookie(res, "DELIMS", true) ).toInclude(";,=;"); // this will fail if not encoded as the delimiters should be encoded

		expect( getCookie(res, "preservecase_simple", true) ).toIncludeWithCase("lucee"); 
	}

	public void function testCookieEncodeExtended() localmode=true {
		var uri = createURI("cookie/extendedTest.cfm")
		local.res = _InternalRequest(
			template: uri
		);
		//dumpCookies(local.res);

		include template="cookie/extendedData.cfm";
		loop array=#cookieTestData# item="c"{
			expect( getCookie( res, c.name,false,(c.encodevalue?:true) ) ).toBe( c.value );
		}
	}

	private function dumpCookies(res){
		systemOutput( "", true );
		var c = arguments.res.headers["Set-Cookie"];
		loop array=c item="local.cc"{
			systemOutput( cc, true );
		}
	}

	private function getCookie( res, string cookieName, boolean raw=false,encodevalue=true ){
		var c = arguments.res.headers["Set-Cookie"];
		loop array=c item="local.cc"{
			if ( listFirst( cc, "=" ) eq arguments.cookieName ){
				if ( arguments.raw )
					return cc;
				var tmp=listLast( listFirst( cc, ";" ), "=" );
				if(encodevalue) return urlDecode(tmp);
				return tmp;
			}
		}
		return "--cookie not found--";
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}
