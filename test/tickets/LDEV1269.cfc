component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1269", body=function() {
			it( title="checking QoQ operations",skip=isMySqlNotSupported(), body=function( currentSpec ) {
				
				var uri = createURI("LDEV1269/test.cfm");
				var result = _InternalRequest(
						template:uri
				);
				expect(result.fileContent.trim()).toBe("1");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}

	private function isMySqlNotSupported() {
		return isEmpty(mySqlCredentials());
	}
	
	private struct function mySqlCredentials() {
		return server.getDatasource("mysql");
	}
}
