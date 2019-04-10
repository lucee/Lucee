component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArrayFindAll()", body=function() {
			it(title="checking arrayFindAll() function", body = function( currentSpec ) {
				var arr=["aaa","bb","aaa","ccc","AAA"];
				var res=[];
				res=arrayFindAll(arr,"aaa");
				assertEquals('1,3', arraytoList(res));
				res=arrayFindAll(arr,"a");
				assertEquals('', arraytoList(res));


				var arr=["hello","world","susi","world"];

				// UDF
				var res=[];
				res=arrayFindAll(arr,doFind);
				assertEquals("2,4", arrayToList(res));

				// Closure
				doFind=function (value){
					return value EQ "world";
				};
				var res=[];
				res=arrayFindAll(arr,doFind);
				assertEquals("2,4", arrayToList(res));
			});
		});
	}

	private function doFind(value){
		return value EQ "world";
	}
}