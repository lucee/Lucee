component extends="org.lucee.cfml.test.LuceeTestCase" labels="smtp" {
	function beforeAll(){
		variables.uri = createURI("LDEV1537");
	}
	function run( testResults , testBox ) {
		describe( "test case for LDEV-1537", function() {
			it(title = "Checking mail spooler retries emails without from address", skip=isAvailable(),  body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm"
				);
				expect(local.result.filecontent.trim()).toBe('true');
			});
			it(title = "Checking cfmail tag with from attribute, from address with colon", skip=isAvailable(), body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test2.cfm"
				);
				expect(local.result.filecontent.trim()).toBe('from:81@gmail.com');
			});

			it(title = "Checking cfmail tag with to attribute, to address with colon", skip=isAvailable(), body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test3.cfm"
				);
				expect(local.result.filecontent.trim()).toBe('true');
			});
			
			it(title = "Checking cfmail tag with bcc attribute, bcc with colon", skip=isAvailable(), body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test4.cfm"
				);
				expect(local.result.filecontent.trim()).toBe('bcc:81@gmail.com');
			});
			it(title = "Checking cfmail tag with cc attribute, cc with colon", skip=isAvailable(), body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test5.cfm"
				);
				expect(local.result.filecontent.trim()).toBe('cc:81@gmail.com');
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
