component extends="org.lucee.cfml.test.LuceeTestCase" labels="Directory" {

	function beforeAll() {
		variables.dir = getDirectoryFromPath(getCurrentTemplatePath()) & "LDEV4393";
		afterAll();
		if (!directoryExists(variables.dir)) directoryCreate(variables.dir);
		fileWrite("#variables.dir#/testFile.txt", "test");
	}

	function run( testResults, testBox ) {
		describe( title="test case for LDEV-4393", body=function() {
			it(title = "directoryList() UDF filter arguments", body = function( currentSpec ) {
				var result = {};
				filter = function() {
					result = arguments;
					return true;
					};
				directoryList(variables.dir, true, "name", filter);

				expect(structCount(result)).toBe(3);
				expect(result[1]).toBe("testFile.txt");
				expect(result[2]).toBe("file");
				expect(result[3]).toBe("txt");
			});
		});
	}

	function afterAll() {
		if (directoryExists(variables.dir)) directoryDelete(variables.dir, true);
	}
}
