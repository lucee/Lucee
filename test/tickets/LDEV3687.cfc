component extends="org.lucee.cfml.test.LuceeTestCase" labels="smtp" {
	function beforeAll(){
		variables.uri = createURI("LDEV3687");
	}
	function run( testResults , testBox ) {
		describe( "test case for LDEV-3687", function() {
			it(title = "Checking 'cc' of mail with trailing spaces",  skip=isAvailable(), body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/LDEV3687.cfm",
					forms:"Scene=1"
				);
				expect(local.result.filecontent.trim()).toBe('success');
			});
			
			it(title = "Checking 'from' of mail using display name with comma",  skip=isAvailable(), body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/LDEV3687.cfm",
					forms:{Scene=2}
				);
				expect(local.result.filecontent.trim()).toBe('success');
			});
			
			it(title = "Checking 'to' of mail with trailing spaces",  skip=isAvailable(), body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/LDEV3687.cfm",
					forms:{Scene=3}
				);
				expect(local.result.filecontent.trim()).toBe('success');
			});

			it(title = "Checking 'bcc' of mail with trailing spaces",  skip=isAvailable(), body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/LDEV3687.cfm",
					forms:{Scene=4}
				);
				expect(local.result.filecontent.trim()).toBe('success');
			});
		});
	}

	private boolean function isAvailable(){
		return isEmpty( server.getTestService( "smtp" ) );
	}
	
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrentTemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}
