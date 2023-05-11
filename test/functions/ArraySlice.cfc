component extends="org.lucee.cfml.test.LuceeTestCase" labels="array"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArraySlice()", body=function() {
			it(title="checking ArraySlice() function", body = function( currentSpec ) {
				arr=listToArray('aaa,bbb,ccc,ddd,eee');

				assertEquals( "bbb,ccc,ddd,eee", ListCompact(ArrayToList(arraySlice(arr,2))));
				assertEquals( "eee", ListCompact(ArrayToList(arraySlice(arr,0))));
				assertEquals( "ddd,eee", ListCompact(ArrayToList(arraySlice(arr,-1))));
				assertEquals( "bbb,ccc,ddd,eee", ListCompact(ArrayToList(arraySlice(arr,-3))));

				assertEquals( "bbb", ListCompact(ArrayToList(arraySlice(arr,2,1))));
				assertEquals( "bbb,ccc,ddd", ListCompact(ArrayToList(arraySlice(arr,2,3))));

				assertEquals( "bbb,ccc,ddd,eee", ListCompact(ArrayToList(arraySlice(arr,2,0))));
				assertEquals( "bbb,ccc,ddd", ListCompact(ArrayToList(arraySlice(arr,2,-1))));
				assertEquals( "", ListCompact(ArrayToList(arraySlice(arr,0,-2))));
			});
		});
	}
}