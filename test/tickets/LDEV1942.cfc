component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1942");
	}
	function run( testResults , testBox ) {
		if(!hasCredentials()) return;
		describe( "test suite for LDEV-1942() without null support", function() {
			it(title = "Session stored on datasource", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{setNull=false,Storage="myDataSource"}
				);
				expect(local.result.fileContent.trim()).toBe('false');
			});
			it(title = "Session stored on cache", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{setNull=false,Storage="myCache"}
				);
				expect(local.result.fileContent.trim()).toBe('false');
			});
		});

		describe( "test suite for LDEV-1942() with null support", function() {
			it(title = "Session stored on datasource", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{setNull=true,Storage="myDataSource"}
				);
				expect(local.result.fileContent.trim()).toBe('false');
			});
			it(title = "Session stored on cache ", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{setNull=true,Storage="myCache"}
				);
				expect(local.result.fileContent.trim()).toBe('false');
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private boolean function hasCredentials() {
		return (structCount(server.getDatasource("mysql")) gt 0);
	}
}