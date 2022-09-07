component extends = "org.lucee.cfml.test.LuceeTestCase" labels="xml" {
	function beforeAll(){
		variables.uri = createURI("LDEV1676");
	}	

	function run( testresults , testbox ) {
		describe( "testcase for LDEV-1676", function () {
			it( title="Check xmlFeatures externalGeneralEntities=true",body = function ( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\LDEV1676.cfm",
					forms :	{scene=1}
				).filecontent;
				expect(trim(result)).toBe("http://update.lucee.org/rest/update/provider/echoGet/cgi");
			});

			it( title="Check xmlFeatures externalGeneralEntities=false",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\LDEV1676.cfm",
					forms :	{scene=2}
				).filecontent;
				expect(trim(result)).toInclude("security restrictions set by XMLFeatures");
			});
			
			it( title="Check xmlFeatures disallowDoctypeDecl=true",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\LDEV1676.cfm",
					forms :	{scene=3}
				).filecontent;
				expect(trim(result)).toInclude("DOCTYPE");
			});

		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}