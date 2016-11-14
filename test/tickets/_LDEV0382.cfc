component extends="org.lucee.cfml.test.LuceeTestCase"{
	public function beforeAll(){
	}

	public function afterAll(){
	}

	public function run( testResults , testBox ) {
		describe( title="Testing cflock states", body=function() {
			it(title='Case 1', body=function( currentSpec ) {
				uri = createURI("LDEV0382/test.cfm");
				local.result = _InternalRequest(
					template:uri
				);
				assertEquals("RUNNING|WAITING|COMPLETED|WAITING", local.result.fileContent.trim());
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}