component extends = "org.lucee.cfml.test.LuceeTestCase"{

	function beforeAll(){
		afterAll();
		variables.uri = createURI("LDEV2326");
		directoryCreate("#variables.uri#/pdf");
		cfloop( from = "1" to = "2" index = "i" ){
        	cfdocument(format = "PDF" filename = "#variables.uri#/pdf/#i#.pdf" overwrite = "true"){
            	writeOutput("lucee");
        	}
    	}
	}

	function afterAll(){
		variables.uri = createURI("LDEV2326");
		if(directoryExists("#variables.uri#/pdf")){
			directoryDelete("#variables.uri#/pdf", true)
		}
	}

	function run( testResults , testBox ) {
		describe( "test case for LDEV-2326", function() {
			it(title = "PDF action = merge with overwrite = false", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/test.cfm",
					forms : { scene = 1 }
				);
				expect(result.filecontent).toBe(true);
			});

			it(title = "PDF action=merge with overwrite = true", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/test.cfm",
					forms : { scene = 2 }
				);
				expect(result.filecontent).toBe(true);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&calledName;
	}
}