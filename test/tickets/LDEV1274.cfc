component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1274", function() {
			it( title='Checking LSParseCurrency() method', body=function( currentSpec ) {
				result = lsParseCurrency("$1,234.56", "en_us");
				expect(result).toBe("1234.56");
			});

			it( title='Checking LSParseCurrency() method with a native value', body=function( currentSpec ) {
				result = lsParseCurrency("$-1,234.56", "en_us");
				expect(result).toBe("-1234.56");
			});
		});
	}
}