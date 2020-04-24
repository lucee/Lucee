component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.uri = createURI("LDEV2693");
	}

	function run( testResults , testBox ) {

		describe( "test suite for LDEV2693", function() {

			it(title = "cfthrow using cfscript", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV2693.cfm",
					forms:	{scene=1}
				);
				expect("Access Denied").toBe(local.result.filecontent);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}