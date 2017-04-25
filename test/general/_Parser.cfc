component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("Parser");
	}

	function run( testResults , testBox ) {
		describe( "Test suite for Parser", function() {
			describe( "Checking array and structure with invalid variable notation", function() {
				it( title='Checking array value with invalid variable notation', body=function( currentSpec ) {
					local.result = _InternalRequest(
						template:"#variables.uri#/test.cfm",
						forms:{Scene=1}
					);
					expect(local.result.filecontent.trim()).toBe("invalid identifier");
				});

				it( title='Checking array variable with invalid variable notation', body=function( currentSpec ) {
					local.result = _InternalRequest(
						template:"#variables.uri#/test.cfm",
						forms:{Scene=2}
					);
					expect(local.result.filecontent.trim()).toBe("invalid identifier");
				});

				it( title='Checking structure value with invalid variable notation', body=function( currentSpec ) {
					local.result = _InternalRequest(
						template:"#variables.uri#/test.cfm",
						forms:{Scene=3}
					);
					expect(local.result.filecontent.trim()).toBe("invalid identifier");
				});

				it( title='Checking structure variable with invalid variable notation', body=function( currentSpec ) {
					local.result = _InternalRequest(
						template:"#variables.uri#/test.cfm",
						forms:{Scene=4}
					);
					expect(local.result.filecontent.trim()).toBe("invalid identifier");
				});
			});

			describe( "Checking with interpreter", function() {
				it( title='array  value with evaluate function', body=function( currentSpec ) {
					local.result = _InternalRequest(
						template:"#variables.uri#/test.cfm",
						forms:{Scene=5}
					);
					expect(local.result.filecontent.trim()).toBe("Syntax Error, invalid Expression [['a','b','c'].3]");
				});

				it( title='Checking array variable with invalid variable notation', body=function( currentSpec ) {
					local.result = _InternalRequest(
						template:"#variables.uri#/test.cfm",
						forms:{Scene=6}
					);
					expect(local.result.filecontent.trim()).toBe("invalid identifier");
				});

				it( title='Checking structure value with invalid variable notation', body=function( currentSpec ) {
					local.result = _InternalRequest(
						template:"#variables.uri#/test.cfm",
						forms:{Scene=7}
					);
					expect(local.result.filecontent.trim()).toBe("Syntax Error, invalid Expression [{'3':'C'}.3]");
				});

				it( title='Checking structure variable with invalid variable notation', body=function( currentSpec ) {
					local.result = _InternalRequest(
						template:"#variables.uri#/test.cfm",
						forms:{Scene=8}
					);
					expect(local.result.filecontent.trim()).toBe("invalid identifier");
				});
			});

			describe( "Checking variable name with isvalid() function", function() {
				it( title='array  value with evaluate function', body=function( currentSpec ) {
					local.result = _InternalRequest(
						template:"#variables.uri#/test.cfm",
						forms:{Scene=9}
					);
					expect(local.result.filecontent.trim()).toBe("True");
				});

				it( title='Checking array variable with invalid variable notation', body=function( currentSpec ) {
					local.result = _InternalRequest(
						template:"#variables.uri#/test.cfm",
						forms:{Scene=10}
					);
					expect(local.result.filecontent.trim()).toBe("True");
				});

				it( title='Checking structure value with invalid variable notation', body=function( currentSpec ) {
					local.result = _InternalRequest(
						template:"#variables.uri#/test.cfm",
						forms:{Scene=11}
					);
					expect(local.result.filecontent.trim()).toBe("false");
				});
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}