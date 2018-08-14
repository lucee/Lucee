component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArrayPrepend()", body=function() {
			it(title="checking ArrayPrepend() function", body = function( currentSpec ) {
				arr=arrayNew(1);
				arr[1]=1;
				arr[2]=2;
				ArrayPrepend( arr, 'a' );
				ArrayPrepend( arr, 'b' );
				assertEquals("b", arr[1]);
				assertEquals("a", arr[2]);
				assertEquals(1, arr[3]);
				assertEquals(2, arr[4]);
				assertEquals(4, arrayLen(arr));

				arr=arrayNew(1);
				arr[20]=20;
				ArrayPrepend( arr, 'a' );
				ArrayPrepend( arr, 'b' );
				assertEquals("b", arr[1]);
				assertEquals("a", arr[2]);
				assertEquals(20, arr[22]);
				assertEquals(22, arrayLen(arr));
				 
				arr=arrayNew(1);
				arr[2]=2;
				arr[4]=4;
				ArrayPrepend( arr, 'a' );
				ArrayPrepend( arr, 'b' );
				assertEquals("b", arr[1]);
				assertEquals("a", arr[2]);
				assertEquals(2, arr[4]);
				assertEquals(4, arr[6]);
				try{
					assertEquals(null, arr[5]);
					fail("must throw:Array at position 5 is empty");
				} catch (any e){}
				assertEquals(6, arrayLen(arr));
			});
		});
	}
}