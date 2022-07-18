component extends="org.lucee.cfml.test.LuceeTestCase"  labels="mysql,orm" {
	function run(  testResults , testBox ) {
		describe( title="Test suite for LDEV-1569",  skip=checkMySqlEnvVarsAvailable(), body=function() {
			it(title="checking SerializeJSON() with ORM keys having NULL value", body = function( currentSpec ) {
				var uri=createURI("LDEV1569/test.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe('{"Name":null,"ID":2}');
			});

			it(title="checking SerializeJSON() with Query having NULL values ", body = function( currentSpec ) {
				var qry = QueryNew("ID,Name");
				QueryAddRow(qry);
				QuerySetCell(qry,"ID",2);
				QuerySetCell(qry,"Name", null);


				var qEx = queryExecute("select * from qry",{}, {
					returntype: "array",
					dbtype: "query"
				});
				expect(serializeJSON(qEx)).toBe('[{"Name":null,"ID":2}]');
			});
		});
	}
	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private boolean function checkMySqlEnvVarsAvailable() {
		// getting the credentials from the environment variables
		var mySQL = server.getDatasource("mysql");
		return structIsEmpty(mySQL);
	}
}
