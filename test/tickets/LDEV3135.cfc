component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll(){
		variables.uri = createURI("LDEV3135");
	}
	function run ( testResults , testbox ){
		describe( "Testcase for LDEV-3135", function(){
			it(title = "arrayPop function with single parameters", body = function( currentSpec ){
				arr = ["one","two","three","four","five"];
				res = arrayPop(arr);
				expect(res).toBe("five");
			});

			it(title = "arrayPop member function with single parameters", body = function( currentSpec ){
				arr = ["one","two","three","four","five"];
				res = arr.pop();
				expect(res).toBe("five");
			});

			it(title = "arrayPop function with two parameters", body = function( currentSpec ){
				try{
					local.result = _InternalRequest(
						template :  "#uri#/test.cfm"
					);
				}
				catch (any e) {
					result.filecontent = e.message;
				}
				expect(trim(result.filecontent)).toBe("Too many Attributes (1:2) in function call [arrayPop]");
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}