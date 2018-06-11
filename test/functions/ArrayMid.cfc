component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArrayMid()", body=function() {
			it(title="checking ArrayMid() function", body = function( currentSpec ) {
				var text="abcdef";
				var arr=['a','b','c','d','e','f'];

				assertEquals("abcdef", mid(text,1));
				assertEquals("bcdef", mid(text,2));
				assertEquals("abc", mid(text,1,3));
				assertEquals("bcd", mid(text,2,3));
				assertEquals("bcdef", mid(text,2,100));
				assertEquals("", mid(text,200,100));

				assertEquals("a,b,c,d,e,f", arrayToList(arrayMid(arr,1)));
				assertEquals("b,c,d,e,f", arrayToList(arrayMid(arr,2)));
				assertEquals("a,b,c", arrayToList(arrayMid(arr,1,3)));
				assertEquals("b,c,d", arrayToList(arrayMid(arr,2,3)));
				assertEquals("b,c,d,e,f", arrayToList(arrayMid(arr,2,100)));
				assertEquals("", arrayToList(arrayMid(arr,200,100)));

				arr=['a','b'];
				arr[4]='d';
				arr[5]='e';
				arr[6]='f';
				assertEquals("a,b,", arrayToList(arrayMid(arr,1,3)));
				assertEquals("b,,d", arrayToList(arrayMid(arr,2,3)));
			});
		});
	}
}