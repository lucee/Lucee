component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.uri = createURI("LDEV2971");
	}

	function run( testResults , testBox ) {
		describe( title = "Test case for LDEV2971", body = function() {
			it( title = "Checked arrayAppend() with empty {}", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/test.cfm",
					forms :	{ scene = 1 }
				)
				expect(trim(result.filecontent)).toBe(true);
			});
			it( title = "Checked arrayAppend() with numeric", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/test.cfm",
					forms :	{ scene = 2 }
				)
				expect(trim(result.filecontent)).toBe(true);
			});
			it( title = "Checked arrayAppend() with struct", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/test.cfm",
					forms :	{ scene = 3 }
				)
				expect(trim(result.filecontent)).toBe(true);
			});
			it( title = "Checked arrayAppend() with empty struct", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/test.cfm",
					forms :	{ scene = 4 }
				)
				expect(trim(result.filecontent)).toBe(true);
			});
			it( title = "Checked arrayAppend() with numeric", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/test.cfm",
					forms :	{ scene = 5 }
				)
				expect(trim(result.filecontent)).toBe(true);
			});
			it( title = "Checked arrayAppend() with structure", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/test.cfm",
					forms :	{ scene = 6 }
				)
				expect(trim(result.filecontent)).toBe(true);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}