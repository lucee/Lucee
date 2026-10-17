component extends="org.lucee.cfml.test.LuceeTestCase" labels="array"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArraySlice()", body=function() {
			it(title="checking ArraySlice() function", body = function( currentSpec ) {
				var arr=listToArray('aaa,bbb,ccc,ddd,eee');

				assertEquals( "bbb,ccc,ddd,eee", ListCompact(ArrayToList(arraySlice(arr,2))));
				// LDEV-2970: a negative offset counts from the end of the array, -1 is the last element
				assertEquals( "eee", ListCompact(ArrayToList(arraySlice(arr,-1))));
				assertEquals( "ccc,ddd,eee", ListCompact(ArrayToList(arraySlice(arr,-3))));
				assertEquals( "aaa,bbb,ccc,ddd,eee", ListCompact(ArrayToList(arraySlice(arr,-5))));
				expect( function(){ arraySlice(arr,0); } ).toThrow();

				assertEquals( "bbb", ListCompact(ArrayToList(arraySlice(arr,2,1))));
				assertEquals( "bbb,ccc,ddd", ListCompact(ArrayToList(arraySlice(arr,2,3))));

				assertEquals( "bbb,ccc,ddd,eee", ListCompact(ArrayToList(arraySlice(arr,2,0))));
				assertEquals( "bbb,ccc,ddd", ListCompact(ArrayToList(arraySlice(arr,2,-1))));
				assertEquals( "ccc", ListCompact(ArrayToList(arraySlice(arr,-3,-2))));
			});
		});
	}
}