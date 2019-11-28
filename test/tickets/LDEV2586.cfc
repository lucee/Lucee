component extends = "org.lucee.cfml.test.LuceeTestCase"{

	function beforeAll() {
		variables.uri = createURI("LDEV2586");
	}

	function run( testResults , testBox ) {
		describe( "Test case for LDEV-2586", function() {
			it(title = " cfqueryparam does handle decimal value = 1000 with maxLength = 8 ", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : uri&"/LDEV2586.cfm",
					forms : {Scene = 1}
				);
				expect(result.filecontent).toBe('1000');
			});

			it(title = " cfqueryparam doesn't handle deciaml value = 1000 with maxLength = 7", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : uri&"/LDEV2586.cfm",
					forms : {Scene = 2}
				);
				expect(result.filecontent).toBe('1000');
			});

			it(title = " cfqueryparam does handle decimal value = 23.45 with maxLength = 5", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : uri&"/LDEV2586.cfm",
					forms : {Scene = 3}
				);
				expect(result.filecontent).toBe('23');
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
