component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for URLDecode()", body=function() {
			it(title="checking URLDecode() function", body = function( currentSpec ) {
				assertEquals("123", "#URLDecode("123")#");
				assertEquals(" ", "#URLDecode("+")#");
				assertEquals(" ", "#URLDecode("%20")#");

				assertEquals("%25", "#URLEncodedFormat('%')#");
				assertEquals("%", "#URLDecode(URLEncodedFormat('%'))#");

				assertEquals("%25%26%2F", "#URLEncodedFormat('%&/')#");
				assertEquals(" ", "#"+".URLDecode()#");

				// umlauts in input (invalid but supported)
				assertEquals('äöüßÄÜÖ€', URLDecode('äöüßÄÜÖ€', 'utf-8'));


				/* Windows31-J or Shift_JIS %8e%71 -> Unicode \u5b50 = 23376*/
				assertEquals(Chr(23376), URLDecode('%8e%71', 'windows-31j'));
				assertEquals(Chr(23376), URLDecode('%8eq', 'windows-31j'));
			});
		});
	}
}