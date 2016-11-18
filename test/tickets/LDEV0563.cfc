component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		// before all testcases
	}

	function afterAll(){
		// runs after all testcases
	}

	function run(){
		describe( title="Test suite for checking cftransaction with multiple datasources", body=function(){
			beforeEach(function(){
				// runs before each spec in this suite group
			});

			afterEach(function(){
				// Runs after each spec in this suite group
			});

			it(title="With valid table names, but different datasources", body=function(){
				uri = createURI("LDEV0563/test.cfm");
				result = _InternalRequest(
					template:uri,
					forms:{Scene=1}
				);
				errorMsg = listGetAt(result.fileContent.trim(), 1, "|", true);
				Count1 = listGetAt(result.fileContent.trim(), 2, "|", true);
				Count2 = listGetAt(result.fileContent.trim(), 3, "|", true);
				expect(errorMsg).toBe("can't use different connections inside a transaction");
				expect(Count1).toBe(1);
				expect(Count2).toBe(1);
			});

			it(title="With invalid table name to check first table is rolled back or not", body=function(){
				uri = createURI("LDEV0563/test.cfm");
				result = _InternalRequest(
					template:uri,
					forms:{Scene=2}
				);
				errorMsg = listGetAt(result.fileContent.trim(), 1, "|", true);
				Count1 = listGetAt(result.fileContent.trim(), 2, "|", true);
				Count2 = listGetAt(result.fileContent.trim(), 3, "|", true);
				expect(errorMsg).toBe("Table not found in statement [INSERT INTO users3]");
				expect(Count1).toBe(1);
				expect(Count2).toBe(1);
			});
		});
	}

	// Private functions
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}