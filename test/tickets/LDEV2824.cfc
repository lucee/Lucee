component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-2824", function() {
			it(title = "test case for ArrayEach with parallel=true, maxthreads=1", body = function( currentSpec ) {
				var arr = [];
				arraySet(arr,1,5,"");
				ArrayEach (arr, function(){
					var doNothing=1;
				}, true, 1);
			});

			it(title = "test case for ArrayEvery with parallel=true, maxthreads=1", body = function( currentSpec ) {
				var arr = [];
				arraySet(arr,1,5,"");
				ArrayEvery (arr, function(){
					return false;
				}, true, 1);
			});

			it(title = "test case for ArrayFilter with parallel=true, maxthreads=1", body = function( currentSpec ) {
				var arr = [];
				arraySet(arr,1,5,"");
				ArrayMap (arr, function(){
					return false;
				}, true, 1);
			});

			it(title = "test case for ArrayMap with parallel=true, maxthreads=1", body = function( currentSpec ) {
				var arr = [];
				arraySet(arr,1,5,"Lucee");
				ArrayMap (arr, function(item){
					return { name: "item" };
				}, true, 1);
			});

			it(title = "test case for ArraySome with parallel=true, maxthreads=1", body = function( currentSpec ) {
				var arr = [];
				arraySet(arr,1,5,"Lucee");
				ArraySome (arr, function(carry, value){
					return true;
				}, true, 1);
			});
		});
	}
}