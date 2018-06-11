component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArrayFindAllNocase()", body=function() {
			it(title="checking ArrayFindAllNocase() function", body = function( currentSpec ) {
				var arr=["aaa","bb","aaa","ccc","AAA"];
				var res=[];
				var res=arrayFindAllNoCase(arr,"aaa");
				assertEquals('1,3,5', arraytoList(res));
				var res=[];
				var res=arrayFindAllNoCase(arr,"a");
				assertEquals('', arraytoList(res));
			});
		});
	}
}