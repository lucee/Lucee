component extends="org.lucee.cfml.test.LuceeTestCase" labels="query"{

	private query function getTestQuery() {
		return queryNew( "id,key", "integer,varchar", [
			{ "id": 1, "key": "A" },
			{ "id": 2, "key": "B" },
			{ "id": 3, "key": "C" }
		] );
	}

	private void function testClosureFunctionPreservesRowPointer( required function closureCall ) {
		var qry = getTestQuery();
		var expectedKeys = [ "A", "B", "C" ];
		var iteration = 0;
		cfloop( query=qry ) {
			iteration++;
			closureCall( qry );
			expect( qry.currentrow ).toBe( iteration );
			expect( qry.key ).toBe( expectedKeys[ iteration ] );
		}
		expect( qry.currentrow ).toBe( 1 );
	}

	function run( testResults, testBox ) {

		describe( title="LDEV-2027: QueryFilter inside cfloop preserves row pointer", body=function() {

			it( title='qry.filter() member function inside cfloop preserves row pointer', body=function( currentSpec ) {
				testClosureFunctionPreservesRowPointer( function( qry ) {
					qry.filter( function( row ) { return row.id == 1; } );
				} );
			} );

			it( title='queryFilter() function inside cfloop preserves row pointer', body=function( currentSpec ) {
				testClosureFunctionPreservesRowPointer( function( qry ) {
					queryFilter( qry, function( row ) { return row.id == 1; } );
				} );
			} );

		} );

		describe( title="LDEV-2027: QueryMap inside cfloop preserves row pointer", body=function() {

			it( title='qry.map() member function inside cfloop preserves row pointer', body=function( currentSpec ) {
				testClosureFunctionPreservesRowPointer( function( qry ) {
					qry.map( function( row ) { return row; } );
				} );
			} );

			it( title='queryMap() function inside cfloop preserves row pointer', body=function( currentSpec ) {
				testClosureFunctionPreservesRowPointer( function( qry ) {
					queryMap( qry, function( row ) { return row; } );
				} );
			} );

		} );

		describe( title="LDEV-2027: QueryReduce inside cfloop preserves row pointer", body=function() {

			it( title='qry.reduce() member function inside cfloop preserves row pointer', body=function( currentSpec ) {
				testClosureFunctionPreservesRowPointer( function( qry ) {
					qry.reduce( function( acc, row ) { return acc & row.key; }, "" );
				} );
			} );

			it( title='queryReduce() function inside cfloop preserves row pointer', body=function( currentSpec ) {
				testClosureFunctionPreservesRowPointer( function( qry ) {
					queryReduce( qry, function( acc, row ) { return acc & row.key; }, "" );
				} );
			} );

		} );

		describe( title="LDEV-2027: QueryEach inside cfloop preserves row pointer", body=function() {

			it( title='qry.each() member function inside cfloop preserves row pointer', body=function( currentSpec ) {
				testClosureFunctionPreservesRowPointer( function( qry ) {
					qry.each( function( row ) {} );
				} );
			} );

			it( title='queryEach() function inside cfloop preserves row pointer', body=function( currentSpec ) {
				testClosureFunctionPreservesRowPointer( function( qry ) {
					queryEach( qry, function( row ) {} );
				} );
			} );

		} );

		describe( title="LDEV-2027: QuerySome inside cfloop preserves row pointer", body=function() {

			it( title='qry.some() member function inside cfloop preserves row pointer', body=function( currentSpec ) {
				testClosureFunctionPreservesRowPointer( function( qry ) {
					qry.some( function( row ) { return row.id == 1; } );
				} );
			} );

			it( title='querySome() function inside cfloop preserves row pointer', body=function( currentSpec ) {
				testClosureFunctionPreservesRowPointer( function( qry ) {
					querySome( qry, function( row ) { return row.id == 1; } );
				} );
			} );

		} );

		describe( title="LDEV-2027: QueryEvery inside cfloop preserves row pointer", body=function() {

			it( title='qry.every() member function inside cfloop preserves row pointer', body=function( currentSpec ) {
				testClosureFunctionPreservesRowPointer( function( qry ) {
					qry.every( function( row ) { return row.id > 0; } );
				} );
			} );

			it( title='queryEvery() function inside cfloop preserves row pointer', body=function( currentSpec ) {
				testClosureFunctionPreservesRowPointer( function( qry ) {
					queryEvery( qry, function( row ) { return row.id > 0; } );
				} );
			} );

		} );

	}

}
