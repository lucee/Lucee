component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for arrayFilter()", body=function() {
			it(title="checking arrayFilter() function", body = function( currentSpec ) {
				// UDF
				var arr=["hello","world"];
				var arr2=ArrayFilter(arr,helloFilter);

				assertEquals('hello,world', arrayToList(arr));
				assertEquals('hello', arrayToList(arr2));


				// closure
				var clo=function (arg1){
					return FindNoCase("hello",arg1);
				};
				arr2=ArrayFilter(arr,clo);
				assertEquals('hello,world', arrayToList(arr));
				assertEquals('hello', arrayToList(arr2));

			});
		});
	}

	private boolean function helloFilter(arg1){
		return FindNoCase("hello",arg1);
	}
}