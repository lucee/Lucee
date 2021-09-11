component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-305", function() {
			it(title="checking property data type, attribute type='numeric' set with unsavedvalue='0' ", body = function( currentSpec ) {
				var uri=createURI("LDEV0305/App1/index.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe("success");
			});

			it(title="checking property data type, attribute ORMtype='numeric' set with unsavedvalue='0'", body = function( currentSpec ) {
				var uri=createURI("LDEV0305/App2/index.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe("success");
			});

			it(title="checking property data type, attribute type='numeric' ", body = function( currentSpec ) {
				var uri=createURI("LDEV0305/App3/index.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe("success");
			});
		});
	}
	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
