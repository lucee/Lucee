component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1262", function() {
			it(title="Checking the ListRemoveDuplicates()", body = function( currentSpec ) {
				var result = listRemoveDuplicates('1,2,3,4,3');
				assertEquals("1,2,3,4", result );
			});
		});
	}
}