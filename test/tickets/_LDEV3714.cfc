component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true{

	function beforeAll() {
		variables.uri = createURI("LDEV3714");
	}

	function run( testResults, textbox ) {
		describe("testcase for LDEV-3714", function(){
			it(title="this scope as input then it invoke same cfc method", body=function ( currentSpec ){
				try {
					z = invoke(this, "foo", { x: "y" });
				} catch (any e) {
					z = e.message;
				}
				expect(trim(z)).toBe("y");
			});
			it(title = "Variables scope as input to invoke same cfm page method", body=function ( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\LDEV3714.cfm"
				);
				expect(trim(result.filecontent)).toBe("y");
			});
			it(title="Empty string as input then it invoke same cfc method", body=function ( currentSpec ){
				try {
					z = invoke("", "foo", { x: "y" });
				} catch (any e) {
					z = e.message;
				}
				expect(trim(z)).toBe("y");
			});
		});
	}

	function foo(x) { 
		return arguments.x;
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}
