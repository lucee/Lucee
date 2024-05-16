component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( title = "Test suite for structclear", body = function() {

			it( title = 'Test case for structclear function',body = function( currentSpec ) {
				world = {"save":"water","clean":"wastes"};
				structclear(world);
				assertEquals("TRUE",structisempty(world));
			});
		
			it( title = 'Test case for structclear member function',body = function( currentSpec ) {
				world = {"save":"water","clean":"wastes"};
				world.clear();
				assertEquals("TRUE",structisempty(world));
			});
		});
	}
}