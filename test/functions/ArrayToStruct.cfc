component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArrayToStruct()", body=function() {
			it(title="Checking ArrayToStruct() function", body = function( currentSpec ) {
				arr=arrayNew(1);
				arr[1]=1;
				arr[100]=100;
				sct=arrayToStruct(arr);
				assertEquals("true",IsStruct(sct));
				assertEquals("1,100",ListSort(arrayToList(StructKeyArray(sct)),"numeric"));
			});
		});
	}
}
	