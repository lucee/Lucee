component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		
		describe( title = "Test suite for queryReverse", body = function() {

			it( title = 'Checking with to queries with the same columns',body = function( currentSpec ) {
				var qry=queryNew("id,name", "integer,varchar",[
			    [1, "a"],
			    [2, "b"],
			    [3, "c"]
			    ]);
			    var rev=QueryReverse(qry);

				assertEquals("1,2,3",valueList(qry.id));
				assertEquals("3,2,1",valueList(rev.id));
				assertEquals("c,b,a",valueList(rev.name));
			});
			
		});

	}
}