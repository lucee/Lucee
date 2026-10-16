component extends="org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll() {
		variables.uri = createURI("LDEV69");
	}
	function run( testResults, testBox ) {
		describe( "Testcase for LDEV-69", function() {
			it( title="param with associative array syntax URL[""number""]", body=function() {
				try {
					var result = _InternalRequest( template: uri & "/url.cfm" ).fileContent.trim();
				} catch(any e) { result = e.message; }
				expect( result ).toBe( "5" );
			});
			it( title="param with associative array syntax form[""x""]", body=function() {
				try {
					var result = _InternalRequest( template: uri & "/form.cfm" ).fileContent.trim();
				} catch(any e) { result = e.message; }
				expect( result ).toBe( "hello" );
			});
			it( title="param with associative array syntax variables[""k""]", body=function() {
				try {
					var result = _InternalRequest( template: uri & "/variables.cfm" ).fileContent.trim();
				} catch(any e) { result = e.message; }
				expect( result ).toBe( "v" );
			});
		});
	}
	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}
