component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-365", function() {
			it("Checking 'abort' in 'cfscript' without semi-colon", function( currentSpec ){
				local.result = MakeRequest("1");
				expect(result.filecontent.trim()).toBe(1);
			});

			it("Checking 'abort' in 'cfscript' ends with semi-colon", function( currentSpec ){
				local.result = MakeRequest("2");
				expect(result.filecontent.trim()).toBe(1);
			});
		});
	}
	// Private Functions //
	private any function MakeRequest(Scene){
		uri=createURI("LDEV0365/test.cfm");
		local.result = _InternalRequest(
			template:uri,
			forms:{Scene=arguments.Scene}
		);
		return local.result;
	}
	
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}