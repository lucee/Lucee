component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArrayEach()", body=function() {
			it(title="checking ArrayEach() function", body = function( currentSpec ) {
				var arr=["hello","world"];

				request.test=[];

				ArrayEach(arr,eachFilter);
				assertEquals('hello,world', arrayToList(request.test));

				// Closure
				var arr=["hello","world"];
				request.test=[];
				sseachFilter=function (arg1){
					arrayAppend(request.test,arg1);
				};
				ArrayEach(arr,eachFilter);
				assertEquals('hello,world', arrayToList(request.test));
			});
		});
	}

	private function eachFilter(arg1){
		arrayAppend(request.test,arg1);
	}
}