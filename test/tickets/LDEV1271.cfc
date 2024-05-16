component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1271", function() {
			it( title='Checking arrayEvery() method with null values', body=function( currentSpec ) {
				testArray = [ javacast( "null", "" ), javacast( "null", "" ) ];
				testArrayEvery = arrayEvery( testArray, function( n ) {
    				return false;
				} );
				expect(testArrayEvery).toBeFalse();
			});
		});
	}
}