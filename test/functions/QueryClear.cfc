component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		
		describe( title = "Test suite for queryClear", body = function() {

			it( title = 'Checking with to queries with the same columns',body = function( currentSpec ) {
				var q = queryNew("id,name","Integer,Varchar", 
				[ 
				{id=1,name="One"}, 
				{id=2,name="Two"}, 
				{id=3,name="Three"} 
				]);

				var qry=queryClear(q);

				assertEquals(0,q.recordcount);
				assertEquals(0,qry.recordcount);
				assertEquals("id,name",qry.columnlist);
			});
		});

	}
}