component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test case for non UTF-8 characters in LDEV-4648", body=function() {
			it(title="checking URLDecode() function", body = function( currentSpec ) {
				expect( Chr(23376) ).toBe( URLDecode('%8e%71', 'windows-31j') );
				expect( Chr(23376) ).toBe( URLDecode('%8eq', 'windows-31j') );
				expect( Chr(23376) ).toBe( URLDecode('%8e%71', 'Shift_JIS') );
				expect( Chr(23376) ).toBe( URLDecode('%8eq', 'Shift_JIS') );
			});
		});
	}
}