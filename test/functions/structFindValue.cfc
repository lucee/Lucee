component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, testBox ) {
		describe( "Test case for structFindValue()", function() {
			it( title = "Checking structFindValue()", body = function( currentSpec ) {
				findAnimals = { cow : "moo", pig : "oink", cat : "meow" };
				//it'll return pig
				getKey = structFindValue(findAnimals,"oink");
				expect(getKey[1].key).toBe("pig");
			});   
			it( title = "Checking struct.Findvalue()", body = function( currentSpec ) {
				findAnimals = { cow : "moo", pig : "oink", cat : "oink" };
				//it'll return both cat and pig
				getKey = findAnimals.FindValue("oink","all");
				expect(arrayLen(getKey)).toBe(2);
			});
		});
	}
}