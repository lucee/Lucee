component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "check that str.toBase64() returns the same value as toBase64(str)", function() {

			it(title = "Checking with toBase64", body = function( currentSpec ) {

				testString = "MEANINGLESS_VALUE"
                assertEquals(toBase64(testString), testString.toBase64())
			});		
		});	
	}
}