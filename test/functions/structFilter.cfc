component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "Test case for structFilter()", function() {
			it( title = "Checking structFilter()", body = function( currentSpec ) {
				animals = { moo : "moo", pig : "oink", snail : "" };
				getAnimals = StructFilter(animals, function(key) {
					if (Len(animals[key])) {
						return true;
					}
					return false;
				});	
				expect(structkeyexists(getAnimals,"snail")).toBe(false);
			});

			it( title = "Checking struct.Filter()", body = function( currentSpec ) {
				animals = { moo : "moo", pig : "oink", snail : "" };
				getAnimals = animals.Filter(function(key) {
					if (Len(animals[key])) {
						return true;
					}
					return false;
				});	
				expect(structkeyexists(getAnimals,"snail")).toBe(false);
			});
		});
	}
}