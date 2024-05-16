component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run() {
		describe( title="Test suite for structdelete", body=function() {
			world = {"save":"water","clean":"wastes"};
			it( title='Test case for structdelete',body=function( currentSpec ) {
				structdelete(world,"save");
				assertEquals('{"clean":"wastes"}',serialize(world));
				structdelete(world,"clean");
				assertEquals('{}',serialize(world));
				
			});
		});

		describe( title="Test suite for structdelete", body=function() {
			world1 = {"save":"water","clean":"wastes"};
			it( title='Test case for structdelete member function',body=function( currentSpec ) {
				world1.delete("save");
				assertEquals('{"clean":"wastes"}',serialize(world1));
				world1.delete("clean");
				assertEquals('{}',serialize(world1));
			});

		});
	}
}