component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArrayIndexExists()", body=function() {
			it(title="checking ArrayIndexExists() function", body = function( currentSpec ) {
				var a=array(1);
				var a[3]=1;
				assertEquals(true, ArrayIndexExists(array("a","b","c","d"),2));
				assertEquals(false, ArrayIndexExists(array("a","b","c","d"),5));
				assertEquals(false, ArrayIndexExists(a,2));
				assertEquals(true, ArrayIndexExists(a,3));
			});
		});
	}
}