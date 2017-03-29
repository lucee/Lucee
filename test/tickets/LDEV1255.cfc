component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1255", function() {
			it( title='Checking array with invalid variable name notation', body=function( currentSpec ) {
			 	var arr = ["A","B","C","D"];
				var hasError = {};
				try {
					var result = arr.3;
				}
				catch ( any e ) {
					hasError = e;
				}
				expect( structKeyExists( hasError, "Message" ) ).toBe(True);
			});
		});
	}
}