component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.uri = createURI("LDEV2629");
	}

	function run( testResults , testBox ) {
		describe( "test suite for LDEV2629", function() {
			it(title = "Test with cfchart using cfscript", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV2629.cfm",
					forms : {scene=1}
				);
				expect(isimagefile("#uri#/LDEV2629.png")).toBe(true);
			});
		});
	}

	function afterAll() {
		if(fileExists("#uri#/LDEV2629.png")) {
			fileDelete("#uri#/LDEV2629.png");
		}
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}