component extends = "org.lucee.cfml.test.LuceeTestCase"{

	function run( testResults , testBox ) {
		describe( "test case for LDEV2509", function() {
			it(title = "checking with SQL_VARIANT data type", body = function( currentSpec ) {
				query datasource = "LDEV2509_DSN" name = "testQry"{
					echo("SELECT Title FROM LDEV2509");
				}
				expect(testQry.Title[1]).toBe('Lucee');
			});
		});
	}
}