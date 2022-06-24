component extends="org.lucee.cfml.test.LuceeTestCase" labels="smtp" {
	function beforeAll(){
		variables.uri = createURI("LDEV1537");
	}
	function run( testResults , testBox ) {
		describe( "test case for LDEV-1537", function() {
			it(title = "Checking cfmail tag without a FROM address throws", skip=isAvailable(), body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm"
				);
				expect(local.result.filecontent.trim()).notToBe('ok');
			});
			it(title = "Checking cfmail tag with FROM address containing a colon throws", skip=isAvailable(), body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test2.cfm"
				);
				expect(local.result.filecontent.trim()).notToBe('ok');
			});

			it(title = "Checking cfmail tag with TO attribute containing a colon throws", skip=isAvailable(), body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test3.cfm"
				);
				expect(local.result.filecontent.trim()).notToBe('ok');
			});
			
			it(title = "Checking cfmail tag with BCC address containing a colon throws", skip=isAvailable(), body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test4.cfm"
				);
				expect(local.result.filecontent.trim()).notToBe('ok');
			});
			it(title = "Checking cfmail tag with CC address containing a colon throws", skip=isAvailable(), body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test5.cfm"
				);
				expect(local.result.filecontent.trim()).notToBe('ok');
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
