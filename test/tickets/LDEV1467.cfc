component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1467", body=function() {
			it(title="Checking REMatch() is not support with line breaks ", body = function( currentSpec ) {
				REMatchTest = ArrayLen( REMatch( '.', Chr( 10 ) ) );
				expect(REMatchTest).toBe(1);
			});
			it(title="Checking REFind() is support with line breaks", body = function( currentSpec ) {
				REFindTest = REFind( '.',Chr( 10 ) ) ;
				expect(REFindTest).toBe(1);
			});
		});
	}
}
