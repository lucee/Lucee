component extends="org.lucee.cfml.test.LuceeTestCase" skip=true{
	function run( testResults,testBox ){
		describe("Testcase for LDEV-3833", function(){
			it( title="Checking runAsnyc() to passes the applictionContext and scopes", body=function( currentSpec ){
				application action="create" name="LDEV3833" mappings={"/test":expandpath("./test")};
				request.testReq = "testReq";
				application.testApp = "testApp";
				url.testURL = "testURL";
				form.testFORM = "testFORM";
				variables.testVar = "testVar";
				result = runAsync(() => return [request, variables, application, url, form, getApplicationMetadata()]).get();
				expect(structKeyExists(result[1], "testReq")).toBeTrue();
				expect(structKeyExists(result[2], "testVar")).toBeTrue();
				expect(result[3].applicationname).toBe("LDEV3833");
				expect(structKeyExists(result[3], "testApp")).toBeTrue();
				expect(structKeyExists(result[4], "testURL")).toBeTrue();
				expect(structKeyExists(result[5], "testFORM")).toBeTrue();
				expect(structKeyExists(result[6].mappings, "/test")).toBeTrue();
			});
		}); 
	}
}