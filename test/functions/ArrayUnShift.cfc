component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		
		describe( title = "Test suite for arrayUnShift", body = function() {

			it( title = 'Checking with arrayUnShift()',body = function( currentSpec ) {

				var arr=[3,4];
				assertEquals(3,arrayUnShift(arr,3));
				assertEquals(4,arr.unshift(4));
				assertEquals("1,2,3,4",arrayToList(arr));
			});
		});
	}
}