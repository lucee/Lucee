component extends="org.lucee.cfml.test.LuceeTestCase"	{

	function run(testResults, testBox) {

		variables.animals = {
			cow: "moo",
			pig: "oink",
			cat: "meow"
		};

		variables.orderedStruct = [
			one:   1,
			two:   2,
			three: 3,
			four:  4,
			five:  5,
			six:   6,
			seven: 7
		];

		describe(title="Test suite for structValueArray", body=function() {

			it(title="Test function structValueArray()", body=function(currentSpec) {
				var values = structValueArray(animals);
				assertTrue(isArray(values));
				assertEquals(3, arrayLen(values));
				assertTrue(arrayContains(values, "moo"));
				assertTrue(arrayContains(values, "oink"));
				assertTrue(arrayContains(values, "meow"));
			});

			it(title="Test method struct.valueArray()", body=function(currentSpec) {
				var values = animals.valueArray();
				assertTrue(isArray(values));
				assertEquals(3, arrayLen(values));
				assertTrue(arrayContains(values, "moo"));
				assertTrue(arrayContains(values, "oink"));
				assertTrue(arrayContains(values, "meow"));
			});

			it(title="Test function structValueArray() on Ordered Struct", body=function(currentSpec) {
				var orderedValues = orderedStruct.valueArray();
				assertEquals(7, arrayLen(orderedValues));
				loop array=orderedValues index="local.ix" item="local.el" {
					assertEquals(ix, el);
				}
			});
		})
	}
}