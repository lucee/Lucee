component extends="org.lucee.cfml.test.LuceeTestCase" labels="http" skip=true {

	function beforeAll() {
		variables.updateProvider = "http://#cgi.server_name#:8888";

		if( !structKeyExists(request, "WebAdminPassword") )
			request.WebAdminPassword = "password";

		restInitApplication( dirPath="#expandPath(createURI("LDEV4688"))#", serviceMapping="info", password="#request.WebAdminPassword#" );
	}

	function run( testResults , testBox ) {
		describe( "test case for LDEV-4688", function() {

			it( title="checking custom error response using cfthrow", body=function( currentSpec ) {

				http url="#variables.updateProvider#/rest/info/test/getId" method="GET" result="result";

				expect(result.filecontent).toBe("no id found");
				expect(result.statuscode).toBe(404);
			});

			it( title="Checks the sub-resource location, the httpMethod attribute is not specified, but the restPath is specified", body=function( currentSpec ) {

				http url="#variables.updateProvider#/rest/info/test/getBoolean" method="GET" result="result";

				expect(result.filecontent).toBe("Subresource locator error.");
				expect(result.statuscode).toBe(500);
			});

			it( title="Checks the sub-resource location, the httpMethod attribute is not specified, but the restPath is specified and it returns the component", body=function( currentSpec ) {

				http url="#variables.updateProvider#/rest/info/test/getComponent" method="GET" result="result";

				expect(result.filecontent).toBe(true);
				expect(result.statuscode).toBe(200);
			});

			it( title="Checks for rest path not specified at function level but httpMethod is specified", body=function( currentSpec ) {

				http url="#variables.updateProvider#/rest/info/test/" method="GET" result="result";

				expect(result.filecontent).toBe(10);
				expect(result.statuscode).toBe(200);
			});

		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

}