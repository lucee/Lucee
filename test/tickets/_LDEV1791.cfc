component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-1791", function() {
			it(title = "Checking the value of query group attribute with querynew()", body = function( currentSpec ) {
				var myQuery = queryNew("testID,title", "INTEGER,VARCHAR", [[013,"event 1"],[0013,"event 2"],[00013,"event 3"]]);
				var count = 0 ;
				loop query="myQuery" group="testID" {
					count++;
				}
				expect(count).toBe("1");
			});

			it(title = "Checking the value of query group attribute with querysetcell() ", body = function( currentSpec ) {
				var myQuery=QueryNew("testID,title") ;
				QueryAddRow(myQuery,3) ;

				QuerySetCell(myQuery, "testID", 013, 1);
				QuerySetCell(myQuery, "title", "event 1", 1);

				QuerySetCell(myQuery, "testID", 0013, 2);
				QuerySetCell(myQuery, "title", "event 2", 2);

				QuerySetCell(myQuery, "testID", 00013, 3);
				QuerySetCell(myQuery, "title", "event 3", 3);

				var count = 0 ;
				loop query="myQuery" group="testID" {
					count = count + 1 ;
				}
				expect(count).toBe("3");
			});
		});
	}
}
