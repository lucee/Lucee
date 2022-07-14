component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArrayLast()", body=function() {
			it(title="checking ArrayLast() function", body = function( currentSpec ) {
				var x=array(1,2,3,4,5,6,7,8);
				var arr = ["aaa", "bbb", "ccc", "ddd"];
				var arr2 = ["aaa", "bbb", "ccc", "ddd", ""];
				assertEquals(8, ArrayLast(x));
				assertEquals("ddd", ArrayLast(arr));
				assertEquals("", ArrayLast(arr2));
			});
			it(title="checking array.Last() member function", body = function( currentSpec ) {
				var x = array(1,2,3,4,5,6,7,8);
				var arr = ["aaa", "bbb", "ccc", "ddd"];
				var arr2 = ["aaa", "bbb", "ccc", "ddd", ""];
				assertEquals(8, x.last());
				assertEquals("ddd", arr.last());
				assertEquals("", arr2.last());
			});
		});
	}
}