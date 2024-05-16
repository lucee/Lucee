component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		
		describe( title = "Test suite for QueryRowSwap", body = function() {

			it( title = 'swap rows',body = function( currentSpec ) {
				
				var qry=queryNew("id,name", "integer,varchar",[
			    [1, "a"],
			    [2, "b"],
			    [3, "c"]
			    ]);
			    
			    var swapped=QueryRowSwap(qry,2,3) 
			    
				assertEquals("1,3,2",valueList(qry.id));
				assertEquals("a,c,b",valueList(swapped.name));
			});
		});

	}
}