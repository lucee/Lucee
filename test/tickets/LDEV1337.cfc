component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1337", function() {
			it( title='Checking lsNumberformat for 0', body=function( currentSpec ) {
				local.result = lsNumberformat(0,'+999');
				expect(local.result.trim()).toBe('+  0');
			});

			it( title='Checking lsNumberformat for 0.1', body=function( currentSpec ) {
				local.result = lsNumberformat(0.1,'+999');
				expect(local.result.trim()).toBe('+  0');
			});
		});
	}
}