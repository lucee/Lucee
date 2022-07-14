component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArrayMax()", body=function() {
			it(title="checking ArrayMax() function", body = function( currentSpec ) {
				var arr=arrayNew(1);
				ArrayAppend( arr, 1 );
				ArrayAppend( arr, 2 );
				ArrayAppend( arr, 3 );
				assertEquals(3, ArrayMax(arr));

				arr=arrayNew(1);
				ArrayAppend( arr, 1 );
				ArrayAppend( arr, 2.5 );
				ArrayAppend( arr, 33.5 );
				assertEquals(33.5, ArrayMax(arr));

				arr=arrayNew(1);
				ArrayAppend( arr, 1 );
				ArrayAppend( arr, true );
				ArrayAppend( arr, 2 );
				assertEquals(2, ArrayMax(arr));

				arr=arrayNew(1);
				ArrayAppend( arr, 1 );
				ArrayAppend( arr, "hans" );
				ArrayAppend( arr, 2 );
				try {
					assertEquals(2, ArrayMax(arr));
					fail("must throw:Non-numeric value found");
				} catch ( any e ){

				}

				arr=arrayNew(1);
				arr[3]=0;
				ArrayAppend( arr, 1 );
				ArrayAppend( arr, 2 );
				try {
					assertEquals(2, ArrayMax(arr));
					fail("must throw:Non-numeric value found");
				} catch ( any e ){
				}

				var arr=arrayNew(2);
				arr[1][1]=1;
				arr[1][2]=2;
				arr[1][3]=3;
				try{
					assertEquals(3, ArrayMax(arr));
					fail("must throw:The array passed cannot contain more than one dimension.");
				} catch ( any e){
				}
			});

			it(title="checking array.Max() member function", body = function( currentSpec ) {
				var arr = arrayNew(1);
				ArrayAppend( arr, 1 );
				ArrayAppend( arr, 2 );
				ArrayAppend( arr, 3 );
				assertEquals(3,arr.max());

				arr = arrayNew(1);
				ArrayAppend( arr, 1 );
				ArrayAppend( arr, 2.5 );
				ArrayAppend( arr, 33.5 );
				assertEquals(33.5,arr.max());

				arr = arrayNew(1);
				ArrayAppend( arr, 1 );
				ArrayAppend( arr, true );
				ArrayAppend( arr, 2 );
				assertEquals(2,arr.max());

				arr = arrayNew(1);
				ArrayAppend( arr, 1 );
				ArrayAppend( arr, "hans" );
				ArrayAppend( arr, 2 );
				try {
					assertEquals(2,arr.max());
					fail("must throw:Non-numeric value found");
				} catch ( any e ){

				}

				arr = arrayNew(1);
				arr[3] = 0;
				ArrayAppend( arr, 1 );
				ArrayAppend( arr, 2 );
				try {
					assertEquals(2,arr.max());
					fail("must throw:Non-numeric value found");
				} catch ( any e ){
				}

				var arr = arrayNew(2);
				arr[1][1] = 1;
				arr[1][2] = 2;
				arr[1][3] = 3;
				try{
					assertEquals(3,arr.max());
					fail("must throw:The array passed cannot contain more than one dimension.");
				} catch ( any e){
				}
			});
		});
	}
}