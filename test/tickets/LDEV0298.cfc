component extends="org.lucee.cfml.test.LuceeTestCase"{
	public function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-298 ( checking with the files in same folders )", body=function() {
			it(title="Creating object for a component which has an init() with package access, from a cfm file of same folder", body=function( currentSpec ) {
				uri = createURI("LDEV0298/test.cfm");
				result = _InternalRequest(
					template:uri,
					forms:{Scene:1}
				);
				expect(left(result.fileContent.trim(), 100)).toBe("");
			});

			it(title="Creating object for a component which has an init() with package access, from another component of same folder", body=function( currentSpec ) {
				uri = createURI("LDEV0298/test.cfm");
				result = _InternalRequest(
					template:uri,
					forms:{Scene:2}
				);
				expect(left(result.fileContent.trim(), 100)).toBe("");
			});
		});

		describe( title="Test suite for LDEV-298 ( checking with the files in different folders )", body=function() {
			it(title="Creating object for a component which has an init() with package access, from a cfm file of different folder", body=function( currentSpec ) {
				uri = createURI("LDEV0298/test.cfm");
				result = _InternalRequest(
					template:uri,
					forms:{Scene:3}
				);
				expect(left(result.fileContent.trim(), 100)).toBe("");
			});

			it(title="Creating object for a component which has an init() with package access, from another component of different folder", body=function( currentSpec ) {
				uri = createURI("LDEV0298/test.cfm");
				result = _InternalRequest(
					template:uri,
					forms:{Scene:4}
				);
				expect(left(result.fileContent.trim(), 100)).toBe("");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}