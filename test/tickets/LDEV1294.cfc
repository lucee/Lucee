component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" skip=true{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-1294", function() {
			it(title = "Using query.sort() Member function, should return a new query", body = function( currentSpec ) {
				var q_numbers = queryNew("id,en,mi", "integer,varchar,varchar", [
					[1,"one","tahi"],
					[2,"two","rua"],
					[3,"three","toru"],
					[4,"four","wha"]
				]);
				var q_reversed = q_numbers.sort( function( n1, n2 ){
					return n2.id - n1.id;
				});
				expect( q_reversed ).toBeQuery();
				expect( q_reversed.toJson() ).notToBe( q_numbers.toJson() );  // should return a new query, not modify the existing query
			});

			it(title = "querySort() function should return a new query", body = function( currentSpec ) {
				var q_numbers = queryNew("id,en,mi", "integer,varchar,varchar", [
					[1,"one","tahi"],
					[2,"two","rua"],
					[3,"three","toru"],
					[4,"four","wha"]
				]);
				q_sorted = querysort( q_numbers, function( n1, n2 ){
					return n2.id - n1.id;
				});
				expect( q_sorted ).toBeQuery(); 
				expect( q_ssorted.toJson() ).notToBe( q_numbers.toJson() );  // should return a new query, not modify the existing query

			});
		});
	}
}