component extends = "org.lucee.cfml.test.LuceeTestCase" {
	
	function beforeAll(){
		variables.uri = createURI("LDEV3060");
	}	

	function run( testresults , testbox ) {
		describe( "testcase for LDEV-3054", function () {
			it( title="comonent with sameline '{'",body = function ( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms :	{scene=1}
				);
				expect(trim(result.filecontent)).toBe("invalid syntax before [::]");
			});

			it( title="comonent with next line '{'",body= function ( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms :	{scene=2}
				);
				expect(trim(result.filecontent)).toBe("invalid syntax before [::]");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}