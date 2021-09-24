
component extends="org.lucee.cfml.test.LuceeTestCase" labels="cookie"	{

	public void function testCookieEncode(){
		var uri = createURI("cookie/encode.cfm")
		local.res = _InternalRequest(
			template: uri,
			url: { encode:true }
		);
		dumpCookies(local.res);

		local.res = _InternalRequest(
			template: uri,
			url: { encode: false }
		);
		dumpCookies(local.res);

	}

	private function dumpCookies(res){
		systemOutput("", true);
		var c = arguments.res.headers["Set-Cookie"];
		loop array=c item="local.cc"{
			systemOutput(cc, true);
		}
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}
