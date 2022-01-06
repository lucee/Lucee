component extends = "org.lucee.cfml.test.LuceeTestCase" skip="true"{
	function beforeAll(){
		variables.uri=createURI("LDEV3802");
		variables.file = GetDirectoryFromPath(getcurrentTemplatepath())&'LDEV3802\result.txt';
		afterAll();
	}

	function run ( testResults , testBox ) {
		describe("Testcase for LDEV-3802", function() {	
			it( title="Calling CFC using relative path inside the long running thread", body=function( currentSpec ) {
				_InternalRequest(
					template : "#uri#\LDEV3802.cfm"
				);
				sleep(100);
				expect(trim(fileRead(file))).tobe("success");
			});	
		});
	}

	function afterAll() {
		if (fileExists(file)) fileDelete(file);
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
    }
}
