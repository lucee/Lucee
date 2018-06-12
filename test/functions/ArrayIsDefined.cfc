component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArrayIsDefined()", body=function() {
			it(title="checking ArrayIsDefined() function", body = function( currentSpec ) {
				var arr = arrayNew(1);
				arr[2] = 5;
				arr[4] = 6;
				assertEquals(false, arrayIsDefined(arr,0));
				assertEquals(false, arrayIsDefined(arr,1));
				assertEquals(true,  arrayIsDefined(arr,2));
				assertEquals(false, arrayIsDefined(arr,3));
				assertEquals(true,  arrayIsDefined(arr,4));
				assertEquals(false, arrayIsDefined(arr,5));
			});
		});
	}
}