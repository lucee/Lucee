component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( title = "Testcase for ticket LDEV-3737", body = function() {
			it( title="checking cause", body=function( currentSpec ) {
				
				var msg="message";
				var causeMsg="cause message";
				var causeCauseMsg="cause cause message";

				var causeCause = createObject("Java", "java.lang.Exception").init(causeCauseMsg)
				var cause = createObject("Java", "java.lang.Exception").init(causeMsg,causeCause)
				var outer = createObject("Java", "java.lang.Exception").init(msg, cause)
				
				try {
					throw(object=outer)
				} catch (any e) {
					expect(e.Message).toBe(msg);
					expect(e.cause.Message).toBe(causeMsg);
					expect(e.cause.cause.Message).toBe(causeCauseMsg);
				}
			});
		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
