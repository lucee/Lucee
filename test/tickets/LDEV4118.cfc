component extends = "org.lucee.cfml.test.LuceeTestCase" skip="true"{
	function run( testResults, textbox ) {
		describe("Testcase for LDEV-4118", function() {
			it(title="checking structGet() with numeric key", body=function( currentSpec ) {
				var animals = {cat: {activities: {sleep: "sleep",eat: "eat",drink: "drink"}}};
				try {
					var result = StructGet("animals.cat.activities.1") // Throws an error if the structGet() with a numeric key
				}
				catch(any e) {
					var result = e.message;
				}
				expect(SerializeJSON(result)).tobe("{}");
			});
		});
	}
}