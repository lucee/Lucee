component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1836", function() {
			it( title='Negative Index array', body=function( currentSpec ) {
				var arr = ["Aa","Bb","Cc","Dd","Ee"];
				assertequals("Ee", arr[-1]);//return last element of array
				assertequals("Dd", arr[-2]); //return last before element of array
				assertequals("Cc", arr[-3]);
				assertequals("Bb", arr[-4]);
				assertequals("Aa", arr[-5]);
			});
		});
	}
}