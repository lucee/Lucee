component extends="org.lucee.cfml.test.LuceeTestCase" labels="collection" {
	function run( testResults, textbox ) {
		describe("testcase for collectionEvery()", function() {
			variables.people = [ { name = "Alice", age = 32 }, { name = "Bob", age = 31 }, { name = "Eve", age = 33 }];
			it(title="checking collectionEvery() function", body=function( currentSpec ) {
				assertEquals('true', collectionEvery(people, function(p) { return p.age > 30;}));
				assertEquals('false', collectionEvery(people, function(p) { return p.age > 32;}));
			});
			it(title="checking collection.every() function", body=function( currentSpec ) {
				assertEquals('true', people.every(function(p) { return p.age > 30;}));
				assertEquals('false', people.every(function(p) { return p.age > 32;}));
			});
		});
	}
}