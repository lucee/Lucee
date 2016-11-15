component extends="org.lucee.cfml.test.LuceeTestCase"{
	public function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-240 ( checking with the files in same folders )", body=function() {
			it(title="Creating object for a component which has an init() with package access, from a cfm file of same folder", body=function( currentSpec ) {
				uri = createURI("LDEV0240/test.cfm");
				result = _InternalRequest(
					template:uri,
					forms:{Scene:1}
				);
				expect(result.fileContent.trim()).toBe("");
			});

			it(title="Creating object for a component which has an init() with package access, from another component of same folder", body=function( currentSpec ) {
				uri = createURI("LDEV0240/test.cfm");
				result = _InternalRequest(
					template:uri,
					forms:{Scene:2}
				);
				expect(result.fileContent.trim()).toBe("");
			});
		});

		describe( title="Test suite for LDEV-240 ( checking with the files in different folders )", body=function() {
			it(title="Creating object for a component which has an init() with package access, from a cfm file of different folder", body=function( currentSpec ) {
				uri = createURI("LDEV0240/test/test.cfm");
				result = _InternalRequest(
					template:uri,
					forms:{Scene:1}
				);
				expect(result.fileContent.trim()).notToBe("");
			});

			it(title="Creating object for a component which has an init() with package access, from another component of different folder", body=function( currentSpec ) {
				uri = createURI("LDEV0240/test/test.cfm");
				result = _InternalRequest(
					template:uri,
					forms:{Scene:2}
				);
				expect(result.fileContent.trim()).notToBe("");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}