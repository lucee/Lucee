component  extends = "org.lucee.cfml.test.LuceeTestCase"  labels="pdf" {

	function beforeAll(){
		afterAll();
		variables.uri = createURI("LDEV3015");
		directoryCreate("#variables.uri#/pdf");
	}

	function afterAll(){
		variables.uri = createURI("LDEV3015");
		if(directoryExists("#variables.uri#/pdf")){
			directoryDelete("#variables.uri#/pdf", true)
		}
	}

	function run ( testResults , testbox ) {
		describe( "Testcase for LDEV-3015" ,function () {
			it( title = "Checkig action = extracttext with pdf without style", body = function ( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { scene = 1 }
				);
				getContent = xmlSearch(result.filecontent,"/DocText/TextPerPage/page");
				expect(trim(getcontent[1].xmlText)).toBe("This is PDF example document for the test without font styles.");
			});
			
			it( title = "Checkig action = extracttext with pdf with style", body=function ( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {scene = 2}
				);
				getContentOne = xmlSearch(result.filecontent,"/DocText/TextPerPage/page");
				expect(trim(getContentOne[1].xmlText)).toBe("This is PDF example document for the test with font styles.");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}