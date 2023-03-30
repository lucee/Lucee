component extends="org.lucee.cfml.test.LuceeTestCase" labels="smtp" {
	function beforeAll(){
		variables.uri = createURI("LDEV3845");
	}
	function run( testResults , testBox ) {
		describe( "test case for LDEV-1537", function() {
			it(title = "Checking cfmail tag with a utf8 email address", skip=isAvailable(), body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/index.cfm",
					form: {
						email: "läs@lucee.org"
					}
				);
				expect(local.result.filecontent.trim()).toBe('ok');
			});

			it(title = "Checking cfmail tag with a non-utf8 email address", skip=isAvailable(), body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/index.cfm",
					form: {
						email: "las@lucee.org"
					}
				);
				expect(local.result.filecontent.trim()).toBe('ok');
			});
		});
	}

	private boolean function isAvailable(){
		return (len(server.getTestService("smtp")) eq 0);
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
