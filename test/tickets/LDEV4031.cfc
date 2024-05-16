component extends="org.lucee.cfml.test.LuceeTestCase"  skip=true{

	function run( testResults , testBox ) {
		describe( "Testcase for LDEV-4031", function() {
			it( title="create mapping in the onRequest", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#createURI("LDEV4031")#/LDEV4031.cfm"
				).filecontent;
				expect(trim(result)).toBe("LDEV4031");
			});

			it( title="using expandpath('path') without slash(/) before creating a mapping in the onRequest", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#createURI("LDEV4031")#/LDEV4031.cfm",
					forms : {scene=1}
				).filecontent;
				expect(trim(result)).toBe("LDEV4031");
			});
			
			it( title="using expandpath('path') with slash(/) before creating a mapping in the onRequest", body=function( currentSpec ) {
				try {
					local.result = _InternalRequest(
						template : "#createURI("LDEV4031")#/LDEV4031.cfm",
						forms : {scene=2}
					).filecontent;
				}
				catch(any e) {
					local.result = e.message;
				}
				expect(trim(result)).toBe("LDEV4031");
			});
		});
	}
	
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
} 