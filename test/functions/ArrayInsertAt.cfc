component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArraInsertAt()", body=function() {
			it(title="checking ArraInsertAt() function", body = function( currentSpec ) {
				var arr=arrayNew(1);
				ArrayAppend( arr, 1 );
				ArrayAppend( arr, 2 );
				ArrayAppend( arr, 3 );
				ArrayInsertAt( arr, 1 ,"new1");
				assertEquals("4", arrayLen(arr));
				assertEquals("new1", arr[1]);
				assertEquals("1", arr[2]);
				assertEquals("2", arr[3]);
				assertEquals("3", arr[4]);

				ArrayInsertAt( arr, 3 ,"new2");
				assertEquals("5", arrayLen(arr));
				assertEquals("new1", arr[1]);
				assertEquals("1", arr[2]);
				assertEquals("new2", arr[3]);
				assertEquals("2", arr[4]);

				try{
					ArrayInsertAt( arr, 10 ,"new3");
					fail("must throw:Cannot insert/delete at position 10.");
				} catch ( any e ){

				}

				arr=arrayNew(1);
				arr[1]=1;
				arr[2]=2;
				arr[3]=3;
				arr[7]=7;
				ArrayInsertAt( arr, 3 ,"new");
				assertEquals(8, arrayLen(arr));

				try {
					assertEquals(7, arr[5]);
					fail("must throw:Element 5 is undefined in a Java object of type class coldfusion.runtime.Array referenced as ");
				} catch ( any e ){

				}

				try {
					assertEquals(7, arr[6]);
					fail("must throw:Element 6 is undefined in a Java object of type class coldfusion.runtime.Array referenced as ");
				} catch ( any e ){

				}

				try {
					assertEquals(7, arr[7]);
					fail("must throw:Element 7 is undefined in a Java object of type class coldfusion.runtime.Array referenced as ");
				} catch ( any e ){

				}

				assertEquals(1, "#arr[1]#");
				assertEquals(2, "#arr[2]#");
				assertEquals("new","#arr[3]#");
				assertEquals(3, "#arr[4]#");
				assertEquals(7, "#arr[8]#");
			});
		});
	}
}