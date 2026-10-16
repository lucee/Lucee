component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for ListFind", function() {
			it(title = "Checking with ListFind", body = function( currentSpec ) {
				assertEquals("2", "#ListFind('abba,bb','bb')#");
				assertEquals("4", "#ListFind('abba,bb,AABBCC,BB','BB')#");
				assertEquals("0", "#ListFind('abba,bb,AABBCC','ZZ')#");
				assertEquals("2", "#ListFind(',,,,abba,bb,AABBCC','bb')#");
				assertEquals("2", "#ListFind(',,,,abba,,,,bb,AABBCC','bb')#");
				assertEquals("2", "#ListFind(',,,,abba,,,,bb,AABBCC','bb','.,;')#");
				assertEquals("2", "#ListFind('a,,c','c',',',false)#");
				assertEquals("3", "#ListFind('a,,c','c',',',true)#");
			});

			it(title = "prefix must not match a longer element", body = function( currentSpec ) {
				// 'ab' is a prefix of 'abba' — must not match
				assertEquals(0, ListFind('abba,bb', 'ab'));
				// trailing element
				assertEquals(0, ListFind('bb,abba', 'ab'));
				// multi-char delimiter
				assertEquals(0, ListFind('abba.bb', 'ab', '.;'));
				assertEquals(0, ListFind('bb.abba', 'ab', '.;'));
			});

			it(title = "suffix must not match a longer element", body = function( currentSpec ) {
				assertEquals(0, ListFind('abba,bb', 'bba'));
				assertEquals(0, ListFind('bb,abba', 'bba'));
			});

			it(title = "value longer than any element must not match", body = function( currentSpec ) {
				assertEquals(0, ListFind('ab,cd', 'abcd'));
				assertEquals(0, ListFind('ab', 'abcd'));
			});

			it(title = "exact match still works — case sensitive", body = function( currentSpec ) {
				assertEquals(1, ListFind('abba,bb', 'abba'));
				// case sensitive: uppercase should NOT match
				assertEquals(0, ListFind('abba,bb', 'ABBA'));
				// trailing element exact match
				assertEquals(2, ListFind('bb,abba', 'abba'));
				// multi-char delimiter
				assertEquals(1, ListFind('abba.bb', 'abba', '.;'));
				assertEquals(2, ListFind('bb.abba', 'abba', '.;'));
			});

			it(title = "single-element list", body = function( currentSpec ) {
				assertEquals(1, ListFind('hello', 'hello'));
				assertEquals(0, ListFind('hello', 'hell'));
				assertEquals(0, ListFind('hello', 'hellox'));
			});
		});
	}
}
