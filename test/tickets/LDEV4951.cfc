component extends = "org.lucee.cfml.test.LuceeTestCase"  {

    function run( testResults, testBox ) {
        describe( "Testcase for LDEV-4951", function() {
            it( title="use the same function listener more than once in a request", body=function( currentSpec ) {
				loop times=10 {
					dump(arrayLen([]):{});
				}
			});
		});
    }
}
