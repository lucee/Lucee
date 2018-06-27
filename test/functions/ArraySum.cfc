component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArraySum()", body=function() {
			it(title="checking ArraySum() function", body = function( currentSpec ) {
				arr=arrayNew(1);
				arr[1]=111;
				arr[2]=22;
				arr[3]=3.5;
				assertEquals("136.5", arraySum(arr));

				arr[4]="susi";
				try{
					assertEquals("136.5", arraySum(arr));
					fail("must throw:Non-numeric value found.");
				} catch(any e){}
					
				arr=arrayNew(1);
				assertEquals("0",arraySum(arr));

				arr=arrayNew(2);
				try{
					assertEquals("0", arraySum(arr));
					fail("must throw:The array passed cannot contain more than one dimension. ");
				} catch(any e){}
			});
		});
	}
}
