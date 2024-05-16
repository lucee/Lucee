component extends="org.lucee.cfml.test.LuceeTestCase" label="query" {

	function run( testResults , testBox ) {
		describe( "test case for LDEV-2924", function() {
			var q = queryNew( 'foo,bar,baz' );
			it(title = "query.ColumnList with list and string functions", body = function( currentSpec ) {
				expect( listFindNoCase( q.columnList, 'bar' ) ).toBe( 2 );
				expect( listFindNoCase( q.columnList, 'foo' ) ).toBe( 1 );
				expect( reverse( q.columnList ) ).toBe( 'ZAB,RAB,OOF' );
				expect( repeatString( q.columnList, 2 ) ).toBe( 'FOO,BAR,BAZFOO,BAR,BAZ' );
				expect( stringLen( q.columnList ) ).toBe( 11 );
				expect( uCase( q.columnList ) ).toBe( 'FOO,BAR,BAZ' );
			});

			it(title = "query.ColumnList with list and string member functions", skip="true", body = function( currentSpec ) {
				expect( q.columnList.uCase() ).toBe( 'FOO,BAR,BAZ' );
				expect( q.columnList.stringLen() ).toBe( 11 );
				expect( q.columnList.reverse() ).toBe( 'ZAB,RAB,OOF' );
				expect( q.columnList.repeatString( 2 ) ).toBe( 'FOO,BAR,BAZFOO,BAR,BAZ' );
				expect( q.columnList.listFindNoCase( 'bar' ) ).toBe( 2 );
				expect( q.columnList.listFindNoCase( 'foo' ) ).toBe( 1 );
			});

			it(title = "query.recordcount with numeric member function", skip="true", body = function( currentSpec ) {
				expect( q.recordcount.numberFormat() ).toBe( 0 ); //  No matching Method/Function for lucee.runtime.type.QueryColumnRef.numberFormat() found
			});

			it(title = "query.currentrow with numeric member function", skip="true", body = function( currentSpec ) {
				expect( q.currentrow.numberFormat() ).toBe( 0 ); //  No matching Method/Function for lucee.runtime.type.QueryColumnRef.numberFormat() found
			});
		});
	}
}