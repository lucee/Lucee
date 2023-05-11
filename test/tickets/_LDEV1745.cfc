component extends="org.lucee.cfml.test.LuceeTestCase" labels="search"{
	function beforeAll(){
		variables.uri = createURI("LDEV1745");
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1745", function() {
			it( title='checking cfsearch with "+ Test Test"', body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:1}
				);
			});

			it( title='checking cfsearch with "+ Test Test Test"', body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:2}
				);
				expect(result.filecontent.trim()).toBe(0);
			});

			it( title='checking cfsearch with "+ Test Test Test Test', body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:3}
				);
				expect(result.filecontent.trim()).toBe(0);
			});

			it( title='checking cfsearch with "+ Test Test Test Test', body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:3}
				);
				expect(result.filecontent.trim()).toBe(0);
			});

			it( title='checking cfsearch with "Test Test Test Test', body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:4}
				);
				expect(result.filecontent.trim()).toBe(0);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}