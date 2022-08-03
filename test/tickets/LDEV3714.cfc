component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.uri = createURI("LDEV3714");
	}

	function run( testResults, textbox ) {
		describe("testcase for LDEV-3714", function(){
			it(title = "this scope as input then it invoke same page method", body = function ( currentSpec ){
				try {
					z = invoke(this, "foo", { x: "this scope" });
				} catch (any e) {
					z = e.message;
				}
				expect(trim(z)).toBe("this scope");
			});			
			it(title = "Variables scope as input to invoke same cfm page method", body = function ( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\LDEV3714.cfm",
					forms : { scene = 1 }
				);
				expect(result.filecontent.trim()).toBe("variables scope");
			});
			it(title = "Empty string as input then it invoke same cfm page method", body = function ( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\LDEV3714.cfm",
					forms : { scene = 2 }
				);
				expect(result.filecontent.trim()).toBe("empty string in cfm page");
			});
			it(title = "Empty string as input then it invoke same page method", body = function ( currentSpec ){
				try {
					z = invoke("", "foo", { x: "empty string in cfc" });
				} catch (any e) {
					z = e.message;
				}
				expect(trim(z)).toBe("empty string in cfc");
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