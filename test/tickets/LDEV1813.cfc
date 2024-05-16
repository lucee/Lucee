component extends="org.lucee.cfml.test.LuceeTestCase" skip="true" labels="array" {
	function run( testResults, testBox ) {
		describe( "Test suite for LDEV-1813", function() {
			it( title='Array Slice Implementation', body=function( currentSpec ) {
				var a = [1,2,3,4,5,6,7,8];
				var arr = ["Aa","Bb","Cc","Dd","Ee"];
				assertEquals([1, 2, 3, 4, 5, 6,7, 8], a[:]);//return entire Array
				assertEquals([1, 2, 3, 4, 5, 6], a[1:6]); //starts from 1 to 6 index
				assertEquals([1, 3, 5], a[1:6:2]);// "Increase step by 2"
				assertEquals([5, 6, 7, 8], a[5:]);//form '5:' to the end of the array
				assertEquals([1, 2, 3], a[:3]);//to the end of ':3' exclude that index
				assertEquals([1, 2, 3, 4], a[:-5]);//Return all the elements from array expcept the last 5 elements
				assertEquals([1, 3, 5, 7], a[:-2:2]);//Return all the elements from array expcept the last 2 elements, increased by step 2
				assertEquals(["Aa", "Cc", "Ee"], arr[::2]);//Return all the elements from array elements, increased by step 2
				assertEquals(["Ee", "Dd", "Cc", "Bb", "Aa"], arr[-1:1:-1]);//Return all the elements from array elements in reverse order
				assertEquals(["Aa", "Cc"], arr[1:-2:2]);//Return the elements from array elements, increased by step 2
			});
		});
	}
}
