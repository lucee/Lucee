component extends = "org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV2560");
	}

	function run( testResults, testbox ){
		describe("Test case for LDEV2560", function(){
			it(title="Check - cfspreadsheet with headerrow='2'", body = function( currentSpec ){
				local.result = _InternalRequest(
					template : uri&"/test.cfm",
					form : { scene = 1}
				);
				expect(trim(result.filecontent)).tobe('1,A,R');
			});
			it(title="Check - cfspreadsheet with headerrow='1'", body = function( currentSpec ){
				local.result = _InternalRequest(
					template : uri&"/test.cfm",
					form : { scene = 2}
				);
				expect(trim(result.filecontent)).tobe('Id,Name,Name');
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}