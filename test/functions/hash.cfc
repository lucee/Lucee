component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "check that str.hash() returns the same value as hash(str)", function() {

			it(title = "Checking with hash()", body = function( currentSpec ) {
			
				testString = "MEANINGLESS_VALUE"
                assertEquals(hash(testString), testString.hash())
			});		
		});	
	}
}