component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run(){
		describe( title="Test suite for LDEV-1110", body=function(){
			it(title="Checking cfinvoke(){...} without semicolon at the end", body=function(){
				var uri = createURI("LDEV1110/LDEV1110.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.fileContent.trim()).toBe("9.0");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}