component extends="org.lucee.cfml.test.LuceeTestCase" labels="collection" {
	function run( testResults, textbox ) {
		describe("testcase for collectionEach()", function() {
			variables.people = [ { name = "Alice", age = 32 }, { name = "Bob", age = 29 }, { name = "Eve", age = 41 }];
			it(title="checking collectionEach() function", body=function( currentSpec ) {
				var result = "";
				CollectionEach(people, function(p) {
					result &= p.name & ", ";
				});
				assertEquals('Alice, Bob, Eve,', trim(result));
			});
			
			it(title="checking collection.each() function", body=function( currentSpec ) {
				var result = "";
				people.each(function(p) {
					result &= p.name & ", ";
				});
				assertEquals('Alice, Bob, Eve,', trim(result));
			});
		});
	}
}