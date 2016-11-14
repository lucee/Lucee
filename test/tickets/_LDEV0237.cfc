component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-237", function() {
			it("Checking QOQ with Union of two queries", function( currentSpec ){
				errorMsg = "";
				try {
					MathsStuds = queryNew( "id", "integer", [{id=1},{id=2},{id=3}]);
					ScienceStuds = queryNew( "id", "integer", [{id=3},{id=4},{id=5}]);

					combinedStud = new Query(
						dbtype = "query",
						sql = "SELECT id FROM query1 UNION SELECT id FROM query2",
						query1 = MathsStuds,
						query2 = ScienceStuds
					).execute().getResult();

					checkingExistenceOfintersectedRecord = new Query(
						dbtype = "query",
						sql = "SELECT COUNT(id) AS cnt FROM query1 WHERE id = 3",
						query1 = combinedStud
					).execute().getResult();
				} catch( any e ) {
					errorMsg = e.Detail;
				}

				expect(checkingExistenceOfintersectedRecord.cnt).toBe(1);
			});
		});
	}
}