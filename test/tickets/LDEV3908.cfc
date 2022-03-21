component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true {

	function run( testResults , testBox ) {
		describe( "Test case for LDEV-3908", function() {
			it(title="Check cfstoredproc exception in MSSQL using raiseError()", skip="#notHasMssql()#", body=function( currentSpec ) {
				var result = _internalRequest(
					template=createURI("LDEV3908/LDEV3908.cfm")
				);
				res = deSerializeJSON(trim(result.fileContent));
				expect(res[1]).toBe("ExceptionType:database");
				expect(res[2]).toBe("ExceptionMessage:The time type of `Invalid` is invalid. Must be either MINS, HRS or DAYS.");
			});
		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private function notHasMssql() {
		return structCount(server.getDatasource("mssql")) == 0;
	}
}