<cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	
	variables.httpbin = server.getTestService("httpbin");
	variables.hasHttpbin = structCount(variables.httpbin) > 0;
	variables.httpbinUrl = variables.hasHttpbin ? "http://#variables.httpbin.server#:#variables.httpbin.port#" : "";
	
	//public function afterTests(){}
	
	public function setUp(){}

	public function testName() {
		if (!variables.hasHttpbin) return;
		cfexecute(name="curl #variables.httpbinUrl#/anything" ,variable="local.x");
		assertTrue(find('"headers"',x)>0);
	}

	public function testNameStringArg() {
		if (!variables.hasHttpbin) return;
		cfexecute(name="curl", arguments="#variables.httpbinUrl#/anything" ,variable="variables.x");
		assertTrue(find('"headers"',x)>0);
	}
	
	public function testNameStringArrayArg() {
		if (!variables.hasHttpbin) return;
		cfexecute(name="curl", arguments=["#variables.httpbinUrl#/anything"] ,variable="variables.x");
		assertTrue(find('"headers"',x)>0);
	}
	
	public function testDirectoryArg() {
		if (!variables.hasHttpbin) return;
		cfexecute(name="curl", arguments=["#variables.httpbinUrl#/anything"] ,variable="variables.x", directory=getTempDirectory());
		assertTrue(find('"headers"',x)>0);
	}

	public function testDirectoryTagClosed() {
		if (!variables.hasHttpbin) return;
		```
		<cfexecute name="curl" arguments="#variables.httpbinUrl#/anything" variable="variables.x" directory=#getTempDirectory()#></cfexecute>
		```
		assertTrue(find('"headers"',x)>0);
	}

	public function testDirectoryTagWithoutClosingTag() {
		if (!variables.hasHttpbin) return;
		```
		<cfexecute name="curl" arguments="#variables.httpbinUrl#/anything" variable="variables.x" directory=#getTempDirectory()#/>
		```
		assertTrue(find('"headers"',x)>0);
	}
	
	public function testTimeout() {
		if (!variables.hasHttpbin) return;
		try {
			cfexecute(name="curl", timeout="0.1", arguments="#variables.httpbinUrl#/anything" ,variable="variables.x");
		}
		catch(e) {
			expect(e.message).toInclude('expired', e.message); // this fails sometimes on CI
		}
	}

} 
</cfscript>
