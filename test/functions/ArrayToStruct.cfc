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

				arr = ["a","b","c"];
				sct = arrayToStruct(array=arr, valueAsKey=false);
				assertEquals("true", IsStruct(sct));
				assertEquals("1,2,3", structKeyList(sct) ); 
				assertEquals("a", sct["1"]);
				
				sct = arrayToStruct(array=arr, valueAsKey=true);
				assertEquals("true", IsStruct(sct));
				assertEquals("a,b,c", structKeyList(sct) ); 
				assertEquals(1, sct.a);
			});
		});
	}
}
	