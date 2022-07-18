component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {
	public function beforeAll(){
	}

	public function afterAll(){
	}

	public function run( testResults , testBox ) {
		describe( title="Testing imageGrayscale()", body=function() {
			it(title='Testing imageGrayscale()', body=function( currentSpec ) {
					uri=createURI("LDEV0595/test.cfm");
					local.result=_InternalRequest(
						template:uri
					);
					assertEquals("done", left(result.filecontent.trim(), 100));
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}