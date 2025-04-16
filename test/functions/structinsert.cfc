component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		variables.world = ["save":"water","clean":"wastes"];
		describe( title = "Test suite for structinsert", body = function() {

			it( title = 'Test case for structinsert function',body = function( currentSpec ) {
				var res = structinsert(world,"save","money",true);
				assertEquals('true',res);
				res = structinsert(world,"count","money");
				assertEquals('true',res);
			});

			it( title = 'Test case for structinsert member function',body = function( currentSpec ) {
				var res = world.insert("find","way");
				assertEquals(len(res),4);
				assertEquals(res.save,"money");
				assertEquals(res.clean,"wastes");
				assertEquals(res.count,"money");
				assertEquals(res.find,"way");

				res = world.insert("find","you",true);
				assertEquals(len(res),4);
				assertEquals(res.save,"money");
				assertEquals(res.clean,"wastes");
				assertEquals(res.count,"money");
				assertEquals(res.find,"you");
			});
		});
	}
}