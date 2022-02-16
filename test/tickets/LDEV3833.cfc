component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults,testBox ) {
		describe("Testcase for LDEV-3833", function() {
			it( title="Checking runAsnyc() to get the applictionContext and scopes from pageContext", body=function( currentSpec ) {
				cfapplication (name="LDEV3833", mappings={"/test":expandpath("./test")});
				request.testReq = "testReq";
				application.testApp = "testApp";
				url.testURL = "testURL";
				form.testFORM = "testFORM";
				variables.testVar = "testVar";
				variables.result = runAsync(() => return [request, variables, application, url, form, getApplicationMetadata()]).get();
				sleep(50);
				expect(structKeyExists(result[1], "testReq")).toBeTrue();
				expect(structKeyExists(result[2], "testVar")).toBeTrue();
				expect(result[3].applicationname).toBe("LDEV3833");
				expect(structKeyExists(result[3], "testApp")).toBeTrue();
				expect(structKeyExists(result[6].mappings, "/test")).toBeTrue();
			});
			it( title="Checking runAsnyc() to get the Form and URL scopes from pageContext", skip=true, body=function( currentSpec ) {		
				expect(structKeyExists(variables.result[4], "testURL")).toBeTrue();
				expect(structKeyExists(variables.result[5], "testFORM")).toBeTrue();
			});

			it( title="Checking runAsnyc() to pass the application context and variable scopes to pageContext", body=function( currentSpec ) {
				runAsync(() => {
					variables.testVarAsync = "testVarAsync";
					application.testAppAsync = "testAppAsync";
				});
				sleep(50);
				expect(structKeyExists(variables, "testVarAsync")).toBeTrue();
				expect(structKeyExists(application, "testAppAsync")).toBeTrue();
			});
			it( title="Checking runAsnyc() to pass the request, url, form scopes to pageContext", skip=true, body=function( currentSpec ) {
				runAsync(() => {
					request.testReqAsync = "testReqAsync";
					url.testURLAsync = "testURLAsync";
					form.testFORMAsync = "testFORMAsync";
				});
				sleep(50);
				expect(structKeyExists(request, "testReqAsync")).toBeTrue();
				expect(structKeyExists(url, "testURLAsync")).toBeTrue();
				expect(structKeyExists(form, "testFORMAsync")).toBeTrue();
			});
		}); 
	}
}