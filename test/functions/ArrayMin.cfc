component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArrayMin()", body=function() {
			it(title="checking ArrayMin() function", body = function( currentSpec ) {
				var arr=arrayNew(1);
				ArrayAppend( arr, 1 );
				ArrayAppend( arr, 2 );
				ArrayAppend( arr, 3 );
				assertEquals(1, ArrayMin(arr));

				arr=arrayNew(1);
				ArrayAppend( arr, 1 );
				ArrayAppend( arr, 2.5 );
				ArrayAppend( arr, 33.5 );
				assertEquals(1, ArrayMin(arr));

				arr=arrayNew(1);
				ArrayAppend( arr, 1 );
				ArrayAppend( arr, true );
				ArrayAppend( arr, 2 );
				assertEquals(1, ArrayMin(arr));

				arr=arrayNew(1);
				ArrayAppend( arr, 1 );
				ArrayAppend( arr, "hans" );
				ArrayAppend( arr, 2 );
				try{
					assertEquals(1, ArrayMin(arr));
					fail("must throw:Non-numeric value found");
				} catch (any e){}

				arr=arrayNew(1);
				arr[3]=0;
				ArrayAppend( arr, 1 );
				ArrayAppend( arr, 2 );
				try {
					assertEquals(1, ArrayMin(arr));
					fail("must throw:Non-numeric value found");
				} catch (any e){}

				arr=arrayNew(2);
				arr[1][1]=1;
				arr[1][2]=2;
				arr[1][3]=3;
				try {
					assertEquals(1, ArrayMin(arr));
					fail("must throw:The array passed cannot contain more than one dimension.");
				} catch (any e){}
				myNumberArray = [];
				myNumberArray[1]	= -1;
				myNumberArray[2]	= -100;
				myNumberArray[3]	= -200;

				assertEquals(-200, ArrayMin(myNumberArray));
				assertEquals(0, ArrayMin(arrayNew(1)));
			});

			it(title="checking array.Min() Member function", body = function( currentSpec ) {
				var arr = arrayNew(1);
				ArrayAppend( arr, 1 );
				ArrayAppend( arr, 2 );
				ArrayAppend( arr, 3 );
				assertEquals(1,arr.min());

				arr = arrayNew(1);
				ArrayAppend( arr, 1 );
				ArrayAppend( arr, 2.5 );
				ArrayAppend( arr, 33.5 );
				assertEquals(1,arr.min());

				arr = arrayNew(1);
				ArrayAppend( arr, 1 );
				ArrayAppend( arr, true );
				ArrayAppend( arr, 2 );
				assertEquals(1,arr.min());

				arr = arrayNew(1);
				ArrayAppend( arr, 1 );
				ArrayAppend( arr, "hans" );
				ArrayAppend( arr, 2 );
				try{
					assertEquals(1,arr.min());
					fail("must throw:Non-numeric value found");
				} catch (any e){}

				arr = arrayNew(1);
				arr[3] = 0;
				ArrayAppend( arr, 1 );
				ArrayAppend( arr, 2 );
				try {
					assertEquals(1,arr.min());
					fail("must throw:Non-numeric value found");
				} catch (any e){}

				arr = arrayNew(2);
				arr[1][1] = 1;
				arr[1][2] = 2;
				arr[1][3] = 3;
				try {
					assertEquals(1,arr.min());
					fail("must throw:The array passed cannot contain more than one dimension.");
				} catch (any e){}
				myNumberArray = [];
				myNumberArray[1] = -1;
				myNumberArray[2] = -100;
				myNumberArray[3] = -200;

				assertEquals(-200, myNumberArray.min());
				assertEquals(0, arrayNew(1).min());
			});
		});
	}
}