component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArrayLen()", body=function() {
			it(title="checking ArrayLen() function", body = function( currentSpec ) {
				var arr=arrayNew(1);
				assertEquals(0, arrayLen(arr));
				ArrayAppend( arr, 1 );
				assertEquals(1, arrayLen(arr));
				arr[9]=9;
				assertEquals(9, arrayLen(arr));
				ArrayResize(arr, 20);
				assertEquals(20, arrayLen(arr));

				arr=arrayNew(2);
				arr[1][1]=11;
				arr[1][2]=12;
				arr[1][3]=13;
				arr[2][1]=21;
				arr[2][2]=22;
				arr[2][3]=23;

				assertEquals(2, arrayLen(arr));
			});

			it(title="checking array.len() member function", body = function( currentSpec ) {
				var arr = arrayNew(1);
				assertEquals(0,arr.len());
				ArrayAppend( arr, 1 );
				assertEquals(1,arr.len());
				arr[9] = 9;
				assertEquals(9,arr.len());
				ArrayResize(arr, 20);
				assertEquals(20,arr.len());

				arr=arrayNew(2);
				arr[1][1] = 11;
				arr[1][2] = 12;
				arr[1][3] = 13;
				arr[2][1] = 21;
				arr[2][2] = 22;
				arr[2][3] = 23;

				assertEquals(2,arr.len());
			});
		});
	}
}