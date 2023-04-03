component extends="org.lucee.cfml.test.LuceeTestCase" skip="true" {
	function run( testResults, testBox ) {
		describe( title="Testcase for LDEV-4439", body=function() {
			it( title = "Checking numberFormat() with decimal value", body=function( currentSpec ) {
				expect(numberFormat(237453.524,'0.00')).toBe("237453.52");
				expect(numberFormat(237453.526,'0.00')).toBe("237453.53");
				expect(numberFormat(237453.525,'0.00')).toBe("237453.53");
			});
		});
	}	
}
