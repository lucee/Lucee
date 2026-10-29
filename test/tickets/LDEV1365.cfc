component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	function run( testResults, testBox ) {

		describe( "LDEV-1365 query.len()", function() {

			it( "returns the record count, the same as recordCount()", function() {
				var q = queryNew( "id", "integer", [ [ 1 ], [ 2 ], [ 3 ] ] );
				expect( q.len() ).toBe( 3 );
				expect( q.len() ).toBe( q.recordCount() );
				expect( q.len() ).toBe( q.recordCount );
			});

			it( "returns 0 for an empty query", function() {
				var q = queryNew( "id" );
				expect( q.len() ).toBe( 0 );
			});

			it( "has the same signature as the other len member functions", function() {
				expect( [ 1, 2 ].len() ).toBe( 2 );
				expect( "abc".len() ).toBe( 3 );
				expect( queryNew( "a", "varchar", [ [ "x" ] ] ).len() ).toBe( 1 );
			});

			it( "works with the function syntax and is the same as queryRecordCount()", function() {
				var q = queryNew( "id", "integer", [ [ 1 ], [ 2 ] ] );
				expect( queryRecordCount( q ) ).toBe( 2 );
				expect( q.len() ).toBe( queryRecordCount( q ) );
			});

			it( "a column named len keeps working", function() {
				var q = queryNew( "len", "integer", [ [ 7 ], [ 8 ] ] );
				expect( q.len ).toBe( 7 );
				expect( q[ "len" ] ).toBe( 7 );
				expect( q.len() ).toBe( 2 );
			});

		});
	}

}
