component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-2824", function() {
			it(title = "test case for each with parallel=true, maxthreads=1", body = function( currentSpec ) {
				var arr = [];
				arraySet(arr,1,5,"");
				ArrayEach (arr, function(){
					var doNothing=1;
				}, true, 1);
			});
		});
	}
}