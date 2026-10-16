component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {

		variables.world = {"save":"water","clean":"wastes"};
		variables.human = {"save":"money","clean":"food"};
		variables.legend = {"save":"energy","forget":"sadness"}
		describe( title = "Test suite for structAppend", body = function() {

			it( title = 'Test case for structAppend function',body = function( currentSpec ) {
				structappend(world,human);
				assertEquals("food", world.clean);
				assertEquals("money", world.save);
				structappend(world,legend);
				assertEquals("food", world.clean);
				assertEquals("energy", world.save);
				assertEquals("sadness", world.forget);
				structappend(human,legend);
				assertEquals("food", human.clean);
				assertEquals("energy", human.save);
				assertEquals("sadness", human.forget);
				structappend(legend,human,false);
				assertEquals("food", legend.clean);
				assertEquals("energy", legend.save);
				assertEquals("sadness", legend.forget);
				structappend(legend,{"save":"time"});
				assertEquals("food", legend.clean);
				assertEquals("time", legend.save);
				assertEquals("sadness", legend.forget);
			});

			it( title = 'Test case for structAppend member function',body = function( currentSpec ) {
				world.append(human);
				assertEquals("food", world.clean);
				assertEquals("energy", world.save);
				assertEquals("sadness", world.forget);
				world.append(legend);
				assertEquals("food", world.clean);
				assertEquals("time", world.save);
				assertEquals("sadness", world.forget);
				human.append(legend);
				assertEquals("food", human.clean);
				assertEquals("time", human.save);
				assertEquals("sadness", human.forget);
				legend.append(human,false);
				assertEquals("food", legend.clean);
				assertEquals("time", legend.save);
				assertEquals("sadness", legend.forget);
				legend.append({"save":"time"});
				assertEquals("food", legend.clean);
				assertEquals("time", legend.save);
				assertEquals("sadness", legend.forget);
			});

			it( title = 'Test case for structAppend with ordered structs',body = function( currentSpec ) {
				// Ordered structs using [:] notation should maintain insertion order
				var orderedWorld = ["save":"water","clean":"wastes"];
				var orderedHuman = ["save":"money","clean":"food"];
				var orderedLegend = ["save":"energy","forget":"sadness"];

				structappend(orderedWorld,orderedHuman);
				assertEquals("money", orderedWorld.save);
				assertEquals("food", orderedWorld.clean);

				structappend(orderedWorld,orderedLegend);
				assertEquals("energy", orderedWorld.save);
				assertEquals("food", orderedWorld.clean);
				assertEquals("sadness", orderedWorld.forget);

				structappend(orderedHuman,orderedLegend);
				assertEquals("energy", orderedHuman.save);
				assertEquals("food", orderedHuman.clean);
				assertEquals("sadness", orderedHuman.forget);

				structappend(orderedLegend,orderedHuman,false);
				assertEquals("energy", orderedLegend.save);
				assertEquals("sadness", orderedLegend.forget);
				assertEquals("food", orderedLegend.clean);
			});
		});

	}
}