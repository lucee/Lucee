component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1917");
	}
	function run( testResults , testBox ) {
		if(!hasCredentials()) return;
		describe( "test suite for LDEV-1917()", function() {
			it(title = "cfprocparam passes null instead of empty strings with NVARCHAR cfsqltype", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm"
				);
				expect(local.result.filecontent.trim()).toBeTrue();
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