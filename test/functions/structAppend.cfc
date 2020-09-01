component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {

		world = {"save":"water","clean":"wastes"};
		human = {"save":"money","clean":"food"};
		legend = {"save":"energy","forget":"sadness"}
		describe( title = "Test suite for structAppend", body = function() {

			it( title = 'Test case for structAppend function',body = function( currentSpec ) {
				structappend(world,human);
				assertEquals('{"clean":"food","save":"money"}',serialize(world));
				structappend(world,legend);
				assertEquals('{"clean":"food","save":"energy","forget":"sadness"}',serialize(world));
				structappend(human,legend);
				assertEquals('{"clean":"food","save":"energy","forget":"sadness"}',serialize(human));
				structappend(legend,human,false);
				assertEquals('{"clean":"food","save":"energy","forget":"sadness"}',serialize(legend));
				structappend(legend,{"save":"time"});
				assertEquals('{"clean":"food","save":"time","forget":"sadness"}',serialize(legend));
			});

			it( title = 'Test case for structAppend member function',body = function( currentSpec ) {
				world.append(human);
				assertEquals('{"clean":"food","save":"energy","forget":"sadness"}',serialize(world));
				world.append(legend);
				assertEquals('{"clean":"food","save":"time","forget":"sadness"}',serialize(world));
				human.append(legend);
				assertEquals('{"clean":"food","save":"time","forget":"sadness"}',serialize(human));
				legend.append(human,false);
				assertEquals('{"clean":"food","save":"time","forget":"sadness"}',serialize(legend));
				legend.append({"save":"time"});
				assertEquals('{"clean":"food","save":"time","forget":"sadness"}',serialize(legend));
			});
		});

	}
}