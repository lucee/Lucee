component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArrayMerge()", body=function() {
			it(title="checking ArrayMerge() function", body = function( currentSpec ) {
				var arr1=arrayNew(1);
				ArrayAppend( arr1, 1 );
				ArrayAppend( arr1, 2 );
				ArrayAppend( arr1, 3 );

				var arr2=arrayNew(1);
				ArrayAppend( arr2, 4 );
				ArrayAppend( arr2, 5 );
				ArrayAppend( arr2, 6 );

				var arr=arrayMerge(arr1,arr2);
				assertEquals(6, arrayLen(arr));
				assertEquals(1, arr[1]);
				assertEquals(2, arr[2]);
				assertEquals(3, arr[3]);
				assertEquals(4, arr[4]);
				assertEquals(5, arr[5]);
				assertEquals(6, arr[6]);


				arr=arrayMerge(arr1,arr2,true);
				assertEquals(3, arrayLen(arr));
				assertEquals(1, arr[1]);
				assertEquals(2, arr[2]);
				assertEquals(3, arr[3]);
			});

			it(title="checking array.Merge() member function", body = function( currentSpec ) {
				var arr1 = arrayNew(1);
				ArrayAppend( arr1, 1 );
				ArrayAppend( arr1, 2 );
				ArrayAppend( arr1, 3 );

				var arr2 = arrayNew(1);
				ArrayAppend( arr2, 4 );
				ArrayAppend( arr2, 5 );
				ArrayAppend( arr2, 6 );

				var arr = arr1.merge(arr2);
				assertEquals(6, arrayLen(arr));
				assertEquals(1, arr[1]);
				assertEquals(2, arr[2]);
				assertEquals(3, arr[3]);
				assertEquals(4, arr[4]);
				assertEquals(5, arr[5]);
				assertEquals(6, arr[6]);

				arr = arr1.merge(arr2,true);
				assertEquals(3, arrayLen(arr));
				assertEquals(1, arr[1]);
				assertEquals(2, arr[2]);
				assertEquals(3, arr[3]); 
			});
		});
	}
}