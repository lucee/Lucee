component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1069", function() {
			it('Checking structKeyTranslate()',  function( currentSpec ) {
				uri=createURI("LDEV1069/index.cfm");
				myStruct = structNew();
				myStruct["result"] = structNew();
				myStruct.result["field-one"] = "Test";
				myStruct.result["submit-button"] = "Test";
				myStruct["result.field-one"] = "Test";
				myStruct["result.submit-button"] = "Test";
				local.result=_InternalRequest(
					template:uri,
					forms:myStruct
				);
				assertEquals("",left(result.filecontent.trim(), 100));
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}