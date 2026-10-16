component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for ListFindNoCase", function() {
			it(title = "Checking with ListFindNoCase", body = function( currentSpec ) {
				assertEquals("2", "#ListFindNoCase('abba,bb','bb')#");
				assertEquals("2", "#ListFindNoCase('abba,bb,AABBCC,BB','BB')#");
				assertEquals("0", "#ListFindNoCase('abba,bb,AABBCC','ZZ')#");
				assertEquals("2", "#ListFindNoCase('abba,,,,,,,bb,AABBCC,BB','BB')#");
				assertEquals("2", "#ListFindNoCase(',,,abba,,,,,,,bb,AABBCC,BB','BB')#");
				assertEquals("2", "#ListFindNoCase(',,,abba,,,,,,,bb,AABBCC,BB','BB','.,;')#");
				assertEquals("2", "#ListFindNoCase('a,,c','c',',',false)#");
				assertEquals("3", "#ListFindNoCase('a,,c','c',',',true)#");
				assertEquals("2", "#ListFindNoCase('a,,c','C',',',false)#");
			});

			it(title = "prefix must not match a longer element", body = function( currentSpec ) {
				// 'ab' is a prefix of 'abba' — must not match
				assertEquals(0, ListFindNoCase('abba,bb', 'ab'));
				// trailing element: 'abba' is last, searching for prefix 'ab'
				assertEquals(0, ListFindNoCase('bb,abba', 'ab'));
				// multi-char delimiter: same prefix rule applies
				assertEquals(0, ListFindNoCase('abba.bb', 'ab', '.;'));
				assertEquals(0, ListFindNoCase('bb.abba', 'ab', '.;'));
			});

			it(title = "suffix must not match a longer element", body = function( currentSpec ) {
				// 'bba' is a suffix of 'abba' — must not match
				assertEquals(0, ListFindNoCase('abba,bb', 'bba'));
				// trailing element
				assertEquals(0, ListFindNoCase('bb,abba', 'bba'));
			});

			it(title = "value longer than any element must not match", body = function( currentSpec ) {
				assertEquals(0, ListFindNoCase('ab,cd', 'abcd'));
				// trailing element
				assertEquals(0, ListFindNoCase('ab', 'abcd'));
			});

			it(title = "exact match still works — case insensitive", body = function( currentSpec ) {
				assertEquals(1, ListFindNoCase('abba,bb', 'ABBA'));
				// trailing element exact match
				assertEquals(2, ListFindNoCase('bb,abba', 'ABBA'));
				// multi-char delimiter
				assertEquals(1, ListFindNoCase('abba.bb', 'ABBA', '.;'));
				assertEquals(2, ListFindNoCase('bb.abba', 'ABBA', '.;'));
			});

			it(title = "single-element list", body = function( currentSpec ) {
				assertEquals(1, ListFindNoCase('hello', 'HELLO'));
				assertEquals(0, ListFindNoCase('hello', 'hell'));
				assertEquals(0, ListFindNoCase('hello', 'hellox'));
			});
		});
	}
}