component extends="org.lucee.cfml.test.LuceeTestCase" labels="LDEV6815" {

	function run( testResults, testBox ) {
		describe( "LDEV-6815 queryAddRow( query, struct ) from parallel threads", function() {

			it( title="queryAddRow( q, struct ) in parallel keeps every row", body=function( currentSpec ) {
				var q = queryNew( "a" );
				var items = [];
				for ( var i = 1; i <= 20000; i++ ) arrayAppend( items, i );
				arrayEach( items, function( i ) {
					queryAddRow( q, { a: i } );
				}, true, 8 );
				expect( q.recordcount ).toBe( 20000 );
				var sum = 0; var empty = 0;
				loop query=q { if ( !len( q.a ) ) empty++; else sum += q.a; }
				expect( empty ).toBe( 0 );
				expect( sum ).toBe( 20000 * 20001 / 2 );
			});

			it( title="q.addRow( struct ) member in parallel keeps every row", body=function( currentSpec ) {
				var q = queryNew( "a,b" );
				var items = [];
				for ( var i = 1; i <= 20000; i++ ) arrayAppend( items, i );
				arrayEach( items, function( i ) {
					q.addRow( { a: i, b: i } );
				}, true, 8 );
				expect( q.recordcount ).toBe( 20000 );
				var bad = 0;
				loop query=q { if ( q.a != q.b ) bad++; }
				expect( bad ).toBe( 0 );
			});

			it( title="queryAddRow( q, struct ) returns the row it wrote", body=function( currentSpec ) {
				var q = queryNew( "a" );
				expect( queryAddRow( q, { a: 1 } ) ).toBe( 1 );
				expect( queryAddRow( q, { a: 2 } ) ).toBe( 2 );
				expect( q.a[ 2 ] ).toBe( 2 );
			});
		});
	}
}
