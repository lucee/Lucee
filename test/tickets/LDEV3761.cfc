component extends = "org.lucee.cfml.test.LuceeTestCase" skip=false {

	function run( testResults , testBox ) {
		describe( "Test case for LDEV-3761", function() {
			it(title="check exception not catched",  body=function( currentSpec ) {
				var msg="";
				try{
					_internalRequest(
						template=createURI("LDEV3761/index.cfm"),
						url={type:"exception"}
					);
				}
				catch(e) {
					msg=e.message;
				}
				expect(msg).toBe("upsi dupsi!");
			});

			it(title="check exception catched", body=function( currentSpec ) {
				var result = _internalRequest(
					template=createURI("LDEV3761/index.cfm"),
					url={type:"exception"},
					throwonerror:false
				);
				expect(result.error.message).toBe("upsi dupsi!");
				expect(result.status_code).toBe(500);
			});

			it(title="check abort",  body=function( currentSpec ) {
				var result = _internalRequest(
					template=createURI("LDEV3761/index.cfm"),
					url={type:"abort"},
					throwonerror:false
				);
				expect(result.filecontent).toBe("");
				expect(result.status_code).toBe(200);
			});
		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}