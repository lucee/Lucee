component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.uri = createURI("LDEV2606");
	}

	function run( testResults , testBox ) {
		describe( "test suite for LDEV2606", function() {
			it(title = "cfupdate is not working without value in check box, BIT data type is not accept the checkbox value", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV2606.cfm",
					forms:	{scene=1}
				);
				expect(result.filecontent).toBe(1);
			});

			it(title = "cfupdate is works fine with value attribute in input type checkbox", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV2606.cfm",
					forms:	{scene=2}
				);
				expect(result.filecontent).toBe(1);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}