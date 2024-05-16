component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		
		describe( title = "Test suite for arrayPop", body = function() {

			it( title = 'Checking script and tag syntax',body = function( currentSpec ) {
				var arr=[1,2,3,4];
				assertEquals(4,arrayPop(arr));
				assertEquals(3,arr.pop());
				assertEquals("1,2",arrayToList(arr));
			});

			it( title = 'Checking with empty array',body = function( currentSpec ) {
				var failed=false;
				try {
					arrayPop([]);// must fail with an exception
				}
				catch(e) {
					failed=true;
				}
				assertEquals(true,failed);
				
			});


			it( title = 'Checking with empty value and default value',body = function( currentSpec ) {
				assertEquals("none",arrayPop([],"none"));
			});
		});

	}
}