component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArraySet()", body=function() {
			it(title="checking ArraySet() function", body = function( currentSpec ) {
				arr=arrayNew(1);
				ArraySet(arr, 3, 5, "val");
				assertEquals(5, arrayLen(arr));
				assertEquals("val", arr[3]);
				assertEquals("val", arr[4]);
				assertEquals("val", arr[5]);
				try {
					assertEquals("val", arr[2]);
					fail("must throw:Array at position 2 is empty");
				} catch (any e){}

				arr=arrayNew(1);
				sub=arrayNew(1);
				ArraySet(arr, 3, 5, sub);
				sub[1]=1;
				<!---
				@todo clone dont work --->
				assertEquals("0", arrayLen(arr[3]));
				assertEquals("0", arrayLen(arr[4]));
				assertEquals("0", arrayLen(arr[5]));
				arr[3][1]=1;
				arr[4][2]=1;
				arr[5][3]=1;
				<!---
				@todo clone dont work --->
				assertEquals("1", arrayLen(arr[3]));
				assertEquals("2", arrayLen(arr[4]));
				assertEquals("3", arrayLen(arr[5]));

				arr=arrayNew(2);
				try {
				ArraySet(arr, 3, 5, "");
					fail("must throw:Array dimension error. ");
				} catch (any e){}

				arr=arrayNew(1);
				arr[3]=3;
				ArraySet(arr, 1, 3, "");
				try {
					ArraySet(arr, 3, 1, "");
					fail("must throw:3 is not greater than zero or less than or equal to 1 The range passed to ArraySet must begin with a number greater than zero and less than or equal to the second number ");
				} catch (any e){}
				try {
					ArraySet(arr, -3, 1, "");
					fail("must throw:3 is not greater than zero or less than or equal to 1 The range passed to ArraySet must begin with a number greater than zero and less than or equal to the second number ");
				} catch (any e){}

				ax=arrayNew(1);
				ax[1]=1;
				ax[2]=2;
				ax[4]=4;
				inner=arrayNew(1);
				inner[1]=1;
				ArraySet(ax,1,3,inner);
				inner[1]=2;
				assertEquals("1", ax[1][1]);
				ax[2][1]=3;
				assertEquals("1", ax[1][1]);
			});
		});
	}
}