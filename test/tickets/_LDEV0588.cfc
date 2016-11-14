component extends="org.lucee.cfml.test.LuceeTestCase"{
	public function beforeAll(){
	}

	public function afterAll(){
	}

	public function run( testResults , testBox ) {
		describe( title="Testing CFHTTP with various methods", body=function() {
			it(title='PATCH', body=function( currentSpec ) {
				uri=createURI("LDEV0588/test.cfm");
				local.result=_InternalRequest(
					template:uri,
					forms:{Scene="PATCH"}
				);
				if( result.filecontent.trim() != "PATCH")
					assertEquals("PATCH", "Error: Cannot process request!");
			});
			it(title='GET', body=function( currentSpec ) {
				uri=createURI("LDEV0588/test.cfm");
				local.result=_InternalRequest(
					template:uri,
					forms:{Scene="GET"}
				);
				assertEquals("GET", result.filecontent.trim());
			});
			it(title='POST', body=function( currentSpec ) {
				uri=createURI("LDEV0588/test.cfm");
				local.result=_InternalRequest(
					template:uri,
					forms:{Scene="POST"}
				);
				assertEquals("POST", result.filecontent.trim());
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}