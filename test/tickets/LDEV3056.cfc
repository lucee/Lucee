component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.uri = createURI("LDEV3056");
	}

	function run( testResults, testBox ){
		describe( "Test case for LDEV-3056", function() {
			it( title = "Checked string value with numeric type and abs()", body = function( currentSpec ){
				var result = _Internalrequest(
					template : "#variables.uri#/test.cfm"
				)
				expect(trim(result.filecontent)).toBe(true);
			});
			
			it( title = "Checked abs() with string value", body = function( currentSpec ){
				var teststrValue = "0.08263888888888889";
				var absValue = abs(teststrValue);
				var res = teststrValue eq abs(teststrValue);
				expect(res).toBe(true);
			});
			it( title = "Checked abs() with numeric value", body = function( currentSpec ){
				var testnumValue = 0.08263888888888889;
				var absValue = abs(testnumValue);
				var res = testnumValue eq abs(testnumValue);
				expect(res).toBe(true);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}