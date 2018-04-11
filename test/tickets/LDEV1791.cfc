component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-1791", function() {
			it(title = "Checking the value of query group attribute", body = function( currentSpec ) {
				var myQuery=QueryNew("testID,title") ;
				QueryAddRow(myQuery,2) ;

				QuerySetCell(myQuery, "testID", 013, 1);
				QuerySetCell(myQuery, "title", "event 1", 1);

				QuerySetCell(myQuery, "testID", 0013, 2);
				QuerySetCell(myQuery, "title", "event 2", 2);

				var count = 0 ;
				loop query="myQuery" group="testID" {
					var count = count + 1 ;
				}
				result = count;
				expect(result).toBe("2");
			});
		});
	}
}
