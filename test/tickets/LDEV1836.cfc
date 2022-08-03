component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1836", function() {
			it( title='Negative Index array', body=function( currentSpec ) {
				var arr = ["Aa","Bb","Cc","Dd","Ee","Fe"];
				assertequals("Fe", arr[-1]);//return last element of array
				assertequals("Ee", arr[-2]); //return last before element of array
				assertequals("Dd", arr[-3]);
				assertequals("Cc", arr[-4]);
				assertequals("Bb", arr[-5]);
				assertequals("Aa", arr[-6]);
				expect( function() { arr[-7] } ).toThrow(); // should throw error for negative index greater than array size
				expect( function() { arr[0] } ).toThrow(); // should throw error for 0 position
			});
		});
	}
} 