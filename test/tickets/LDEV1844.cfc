component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1844", function() {
			it( title='Checking chaning process in member function with array', body=function( currentSpec ) {
				var arr = [12,0,1,2,3,4,5,6];
				arr.deleteAt(4).insertAt(3,'Added Successfully');
				assertEquals('Added Successfully', arr[3]);

				var myarray=ArrayNew(1);
				var result= myarray.clear().Resize(10).set(1,10,"blah");
				assertEquals('blah', result[10]);
			});
		});
	}
}