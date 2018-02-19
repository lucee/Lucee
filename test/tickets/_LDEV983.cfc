component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV983");
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-0983", body=function() {
			it(title = "Checking cfinclude with runonce attribute", body = function( currentSpec ) {
				numberOfRuns=0;
				cfinclude(template="#variables.uri#/test.cfm", runonce="true");
				expect(numberOfRuns).toBe('1');
			});
			it(title = "Checking include with runonce attribute", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/includePage.cfm"
				);
				expect(local.result.filecontent.trim()).toBe('1');
			});
			it(title = "Checking include without runonce attribute", body = function( currentSpec ) {
				numberOfRuns=0;
				include template="#variables.uri#/test.cfm";
				expect(numberOfRuns).toBe('1');
			});
			it(title = "Checking include without any attributes", body = function( currentSpec ) {
				numberOfRuns=0;
				include "#variables.uri#/test.cfm";
				expect(numberOfRuns).toBe('1');
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
} 

