component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-1294", function() {
			it(title = "Using query.sort() Member function, returns a query", body = function( currentSpec ) {
				numbers1 = queryNew("id,en,mi", "integer,varchar,varchar", [
					[1,"one","tahi"],
					[2,"two","rua"],
					[3,"three","toru"],
					[4,"four","wha"]
				]);
				reversed = numbers1.sort(function(n1,n2){
					return n2.id - n1.id;
				});
				expect(isboolean(reversed)).toBe('true');
			});

			it(title = "Using querysort() function, returns a boolean value", body = function( currentSpec ) {
				number2 = queryNew("id,en,mi", "integer,varchar,varchar", [
					[1,"one","tahi"],
					[2,"two","rua"],
					[3,"three","toru"],
					[4,"four","wha"]
				]);
				sorted = querysort(number2, function(n1,n2){
					return n2.id - n1.id;
				});
				expect(isboolean(sorted)).toBe('true'); 
			});
		});
	}
}