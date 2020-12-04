component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		afterAll();
		variables.uri = createURI("LDEV2060");
		directoryCreate("#variables.uri#/testOne");
		filewrite("#variables.uri#/testOne/1.txt","I'm from testone, ");
		filewrite(expandpath('../1.txt'),"I'm from test");
	}

	function afterAll(){
		variables.uri = createURI("LDEV2060");
		if(directoryExists("#variables.uri#/testOne")){
			directoryDelete("#variables.uri#/testOne", true);
		}
		getFile = expandpath('../1.txt');
		if(fileExists(getFile)){
			fileDelete(getFile);
		}
	}

	function run ( testResults , testbox ) {
		describe( "Testcase for LDEV-2060" ,function () {
			it( title = "Checked with updateDefaultSecurityManager", body = function ( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm"
				);
				expect(trim(result.filecontent)).toBe("I'm from testone, I'm from test");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}