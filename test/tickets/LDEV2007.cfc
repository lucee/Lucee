component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV2007");
	}

	function run( testResults , testBox ) {
		describe( "Test case for LDEV-2007", function() {
			it(title = "Checking index loop with invalid attribute(endRow) ", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:1}
				);
				expect(local.result.filecontent.trim()).toBe(true);
			});

			it(title = "Checking condition loop with invalid attribute(delimiters) ", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:2}
				);
				expect(local.result.filecontent.trim()).toBe(true);
			});

			it(title = "Checking array loop with invalid attribute(startRow) ", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:3}
				);
				expect(local.result.filecontent.trim()).toBe(true);
			});
			it(title = "Checking collection loop with invalid attribute(step) ", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:4}
				);
				expect(local.result.filecontent.trim()).toBe(true);
			});
			it(title = "Checking list loop with invalid attributes(from and to) ", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:5}
				);
				expect(local.result.filecontent.trim()).toBe(true);
			});
			it(title = "Checking query loop with invalid attribute(item) ", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:6}
				);
				expect(local.result.filecontent.trim()).toBe(true);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
