component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1813", function() {
			it( title='Array Slice Implementation', body=function( currentSpec ) {
				var a = [1,2,3,4,5,6,7,8];
				var arr = ["Aa","Bb","Cc","Dd","Ee"]
				assertequals([1,2,3,4,5,6,7,8], a[:]);//return entire Array
				assertequals([1, 2, 3, 4, 5], a[1:6]); //starts from 1 to 6 index
				assertequals([1, 3, 5], a[1:6:2]);// "Increase step by 2"
				assertequals([5,6,7,8], a[5:]);//form '5:' to the end of the array
				assertequals([1, 2], a[:3]);//to the end of ':3' exclude that index
				assertequals([1,2,3], a[:-5]);//Return all the elements from array expcept the last 5 elemenmts
				assertequals([1,3,5], a[:-2:2]);//Return all the elements from array expcept the last 2 elemenmts, increased by step 2
				assertequals([1,3,5], arr[::2]);//Return all the elements from array elemenmts, increased by step 2
			});
		});
	}
}