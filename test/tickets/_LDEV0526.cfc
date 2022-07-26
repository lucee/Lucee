component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-526", function() {
			it("query of query(dbtype='query') with an empty record", function( currentSpec ) {
				query = queryNew("id,name","Integer,Varchar"); 
				result = QueryExecute(
					sql="SELECT COUNT
					(*) AS noOfCounts FROM query",
					options=
					{dbtype="query"}
				);
				expect(result.noOfCounts).toBe(0);
			});

			it("query with a single record", function( currentSpec ) {
				query = queryNew("id", "Integer", {id=1});
				result = QueryExecute(
					sql="SELECT COUNT(*)
					 AS noOfCounts FROM query where id=2",
					options=
					{dbtype="query"}
				);
				expect(result.noOfCounts).toBe(0);
			});
		});
	}
	
	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
