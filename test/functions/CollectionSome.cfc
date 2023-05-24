component extends="org.lucee.cfml.test.LuceeTestCase" labels="collection" {
	function run( testResults, textbox ) {
		describe("testcase for collectionSome()", function() {
			variables.people = [ { name = "Alice", age = 32 }, { name = "Bob", age = 31 }, { name = "Eve", age = 33 }];
			it(title="checking collectionSome() function", body=function( currentSpec ) {
				assertEquals('true', collectionSome(people, function(p) { return p.age > 30;}));
				assertEquals('true', collectionSome(people, function(p) { return p.age > 32;}));
				assertEquals('false', collectionSome(people, function(p) { return p.age < 10;}));
			});
			it(title="checking collection.some() function", body=function( currentSpec ) {
				assertEquals('true', people.some(function(p) { return p.age > 30;}));
				assertEquals('true', people.some(function(p) { return p.age > 32;}));
				assertEquals('false', people.some(function(p) { return p.age < 23;}));
			});
		});
	}
}