component extends="org.lucee.cfml.test.LuceeTestCase" labels="collection" {
	function run( testResults, textbox ) {
		describe("testcase for CollectionMap()", function() {
			variables.people = [ { name = "Alice", age = 32 }, { name = "Bob", age = 29 }, { name = "Eve", age = 41 }];
			it(title="checking CollectionMap() function", body=function( currentSpec ) {
				var result = "";
				CollectionMap(people, function(p) { 
					result &= p.name & ", ";
				});
				assertEquals('Alice, Bob, Eve,', trim(result));
			});
			
			it(title="checking collection.map() function", body=function( currentSpec ) {
				var result = "";
				people.map(function(p) {
					result &= p.age & ", ";
				});
				assertEquals('32, 29, 41,', trim(result));
			});
		});
	}
}