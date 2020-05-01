component extends = "org.lucee.cfml.test.LuceeTestCase"{

	function beforeAll() {
		variables.uri = createURI("LDEV2581");
	}

	function run( testResults , testBox ) {
		describe( "test case for LDEV-2581", function() {
			it(title = "cfqueryparam with null = false", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : uri&"/LDEV2581.cfm",
					forms : {Scene = 1}
				);
				expect(result.filecontent).toBe(1);
			});

			it(title = "cfqueryparam not ignoring sqltype with null = true", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template: uri&"/LDEV2581.cfm",
					forms: {Scene = 2}
				);
				expect(result.filecontent).toBe(0);
			});

			it(title = "cfqueryparam with null = true and without cfsqltype", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template: uri&"/LDEV2581.cfm",
					forms: {Scene = 3}
				);
				expect(result.filecontent).toBe(0);
			});

			it(title = "cfqueryparam with null = true and with sqltype and without maxlength()", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template: uri&"/LDEV2581.cfm",
					forms: {Scene = 4}
				);
				expect(result.filecontent).toBe(0);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

}
