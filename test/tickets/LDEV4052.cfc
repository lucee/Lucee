component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function beforeAll(){
		variables.uri = createURI("LDEV4052");
	}

	function testInvalidApplicationCFC() {
		expect( function(){
			_InternalRequest(
			template:"#variables.uri#/index.cfm"
			)
		}).toThrow(); // should crash due to an invalid Application.cfc, in a browser it throws
	}
	
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrentTemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}
