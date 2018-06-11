component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArrayFind()", body=function() {
			it(title="checking arrayFind() function", body = function( currentSpec ) {
				assertEquals(2, ArrayFind(listToArray('abba,bb'),'bb'));
				assertEquals(4, ArrayFind(listToArray('abba,bb,AABBCC,BB'),'BB'));
				assertEquals(0, ArrayFind(listToArray('abba,bb,AABBCC'),'ZZ'));

				var arr=["hello","world"];
				// UDF
				var res=ArrayFind(arr,doFind);
				assertEquals(2, res);
				//closure
				doFind=function (value){
					return value EQ "world";
				};

				res=ArrayFind(arr,doFind);
				assertEquals(2, res);
			});
		});
	}

	private function doFind(value){
		return value EQ "world";
	}
}