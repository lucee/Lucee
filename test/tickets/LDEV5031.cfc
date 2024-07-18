component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-5031", function() {

			it("Checking QOQ with unrelated columns in aggregate order by", function( currentSpec ){
				var testQuery = queryNew("amount, relatedData, unrelateddata","integer, varchar, integer");
				testQuery.addRow({amount: 1, relatedData: "a", unrelateddata: 1});
				testQuery.addRow({amount: 2, relatedData: "a", unrelateddata: 2});
				testQuery.addRow({amount: 3, relatedData: "a", unrelateddata: 3});

				// should produce a single row
				local.result = queryExecute("
					select sum(amount) as sum
					from testQuery 
					where 1=1 
					order by unrelatedData
				", {}, {dbtype="query"});

				expect(local.result.recordCount).toBe(1);
				expect(local.result.sum).toBe(6);
			});

	}
}