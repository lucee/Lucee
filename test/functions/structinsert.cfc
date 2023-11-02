component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		world = {"save":"water","clean":"wastes"};
		describe( title = "Test suite for structinsert", body = function() {

			it( title = 'Test case for structinsert function',body = function( currentSpec ) {
				res = structinsert(world,"save","money",true);
				assertEquals('true',res);
				res = structinsert(world,"count","money");
				assertEquals('true',res);
			});

			it( title = 'Test case for structinsert member function',body = function( currentSpec ) {
				res = world.insert("find","way");
				assertEquals('{"count":"money","find":"way","clean":"wastes","save":"money"}',serialize(res));
				res = world.insert("find","you",true);
				assertEquals('{"count":"money","find":"you","clean":"wastes","save":"money"}',serialize(res));
			});
		});
	}

}