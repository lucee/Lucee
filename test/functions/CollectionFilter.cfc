component extends="org.lucee.cfml.test.LuceeTestCase" labels="collection" {
	function run( testResults, textbox ) {
		describe("testcase for collectionFilter()", function() {
			variables.people = [ { name = "Alice", age = 32 }, { name = "Bob", age = 31 }, { name = "Eve", age = 33 }];
			it(title="checking collectionFilter() function", body=function( currentSpec ) {
				var result = collectionFilter(people, function(p) { return p.age > 32;})
				assertEquals("true", structkeyExists(result[1],"name"));
				assertEquals("true", structkeyExists(result[1],"age"));
			});
			
			it(title="checking collection.filter() function", body=function( currentSpec ) {
				var result = people.filter(function(p) { return p.age < 32;})
				assertEquals("true", structkeyExists(result[1],"name"));
				assertEquals("true", structkeyExists(result[1],"age"));
			});
		});
	}
}
