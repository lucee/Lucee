component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArrayResize()", body=function() {
			it(title="checking ArrayResize() function", body = function( currentSpec ) {
				arr=arrayNew(1);
				assertEquals(0, arrayLen(arr));
				ArrayResize(arr, 20);
				assertEquals(20, arrayLen(arr));
				ArrayResize(arr, 10);
				assertEquals(20, arrayLen(arr));

				arr=arrayNew(1);
				arr[2]=2;
				arr[4]=4;
				ArrayResize(arr, 10);
				assertEquals(10, arrayLen(arr));
				assertEquals(2, arr[2]);
				assertEquals(4, arr[4]);
			});
		});
	}
}