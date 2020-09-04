component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		afterAll();
		variables.uri = createURI("LDEV2506");
		directoryCreate("#variables.uri#/pdf");
	}

	function afterAll(){
		variables.uri = createURI("LDEV2506");
		if(directoryExists("#variables.uri#/pdf")){
			directoryDelete("#variables.uri#/pdf", true)
		}
	}

	function run( testResults , testBox ) {
		describe( "Test case for LDEV-2506", function() {
			it(title = "Checked cfdocument with type = 'modern' and 'classic'", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/test.cfm"
				);
				expect(trim(result.filecontent)).toBe('Created a pdf with type = "classic",Created a pdf with type = "modern"')
			});
		});
	};

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}