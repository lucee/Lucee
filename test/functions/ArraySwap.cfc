component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArraySwap()", body=function() {
			it(title="Checking ArraySwap() function", body = function( currentSpec ) {
				arr=arrayNew(1);
				arr[1]=111;
				arr[2]=22;
				arr[3]=3.5;
				 
				ArraySwap(arr, 1,3);
				assertEquals("3.5",arr[1]);
				assertEquals("22",arr[2]);
				assertEquals("111",arr[3]);

				try{
					ArraySwap(arr, 1,4);
					fail("must throw:4 is an invalid swap index of the array. ");
				} catch(any e){}
			});

			it(title="Checking array.Swap() member function", body = function( currentSpec ) {
				var arr = arrayNew(1);
				arr[1] = 111;
				arr[2] = 22;
				arr[3] = 3.5;
				 
				arr.swap(1,3);
				assertEquals("3.5",arr[1]);
				assertEquals("22",arr[2]);
				assertEquals("111",arr[3]);

				try{
					arr.swap(1,4);
					fail("must throw:4 is an invalid swap index of the array. ");
				} catch(any e){}
			});
		});
	}
}
	
