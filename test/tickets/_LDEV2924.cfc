component extends="org.lucee.cfml.test.LuceeTestCase"{

	function run( testResults , testBox ) {
		describe( "test case for LDEV-2924", function() {
			q = queryNew( 'foo,bar,baz' );
			it(title = "querycolumnlist with list and string function", body = function( currentSpec ) {
				expect(listFindNoCase( q.columnList, 'bar' )).toBe('2');
				expect(listFindNoCase( q.columnList, 'foo' )).toBe('1');
				expect(reverse(q.columnList)).toBe('ZAB,RAB,OOF');
				expect(repeatString(q.columnList, 2)).toBe('FOO,BAR,BAZFOO,BAR,BAZ');
				expect(stringLen(q.columnList)).toBe('11');
				expect(ucase(q.columnList)).toBe('FOO,BAR,BAZ');
			});

			it(title = "querycolumnlist with list and string member function", body = function( currentSpec ) {
				expect(q.columnList.ucase()).toBe('FOO,BAR,BAZ');
				expect(q.columnList.stringLen()).toBe('11');
				expect(q.columnList.reverse()).toBe('ZAB,RAB,OOF');
				expect(q.columnList.repeatString(2)).toBe('FOO,BAR,BAZFOO,BAR,BAZ');
				expect(q.columnList.listFindNoCase( 'bar' )).toBe('2');
				expect(q.columnList.listFindNoCase( 'foo' )).toBe('1');
			});
		});
	}
}