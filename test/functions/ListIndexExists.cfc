component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for ListIndexExists", function() {
			it(title = "Checking with ListIndexExists", body = function( currentSpec ) {
				if(server.ColdFusion.ProductName EQ "railo"){
					assertEquals("yes", "#ListIndexExists("a,b,c,d",2)#");
					assertEquals("yes", "#ListIndexExists("a,b,c,d",4)#");
					assertEquals("no", "#ListIndexExists("a,b,c,d",5)#");
					assertEquals("no", "#ListIndexExists("a,b,c,d",5,'.,;')#");
					assertEquals("no", "#ListIndexExists(",,,,,,a,b,c,d",5,'.,;')#");
					assertEquals("no", "#ListIndexExists(",,,,,,a,b,c,d",5,'.,;',false)#");
					assertEquals("yes", "#ListIndexExists(",,,,,,a,b,c,d",5,'.,;',true)#");
				}
			});
		});	
	}
}