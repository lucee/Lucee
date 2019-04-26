component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for arrayDeleteAt()", body=function() {
			it(title="checking arrayDeleteAt() function", body = function( currentSpec ) {
				var arr=arrayNew(1);
				ArrayAppend( arr, 1 );
				ArrayAppend( arr, 2 );
				ArrayAppend( arr, 3 );
				ArrayDeleteAt( arr, 1 );

				assertEquals("2", arrayLen(arr));
				assertEquals("2", arr[1]);
				assertEquals("3", arr[2]);

				ArrayDeleteAt( arr, 1 );
				assertEquals("1", arrayLen(arr));
				assertEquals("3", arr[1]);

				try{
					ArrayDeleteAt( arr, 10);
					fail("must throw:Cannot insert/delete at position 10.");
				} catch ( any e){

				}

				var arr=arrayNew(1);
				arr[1]=1;
				arr[2]=1;
				arr[3]=1;
				arr[7]=7;
				ArrayDeleteAt( arr, 3 );
				assertEquals("1", arr[1]);
				assertEquals("1", arr[2]);
				assertEquals("7", arr[6]);
				assertEquals("6", arrayLen(arr));

				try{
					test=arr[3];
					fail("must throw:Element 3 is undefined in a Java object of type class coldfusion.runtime.Array referenced as ");
				} catch ( any e ){

				}
				assertEquals("7", arr[6]);

				var arr=arrayNew(1);
				arr[1]=1;
				arr[2]=1;
				arr[3]=1;
				arr[7]=7;
				ArrayDeleteAt( arr, 4 );
				assertEquals("1", arr[1]);
				assertEquals("1", arr[2]);
				assertEquals("1", arr[3]);
				assertEquals("7", arr[6]);
				assertEquals("6", arrayLen(arr));
			});
		});
	}
}