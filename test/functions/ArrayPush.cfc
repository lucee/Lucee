component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		
		describe( title = "Test suite for arrayPush", body = function() {

			it( title = 'Checking with arrayPush()',body = function( currentSpec ) {

				var arr=[1,2];
				assertEquals(3,arrayPush(arr,3));
				assertEquals(4,arr.push(4));
				assertEquals("1,2,3,4",arrayToList(arr));
				
			});
		});

	}
}