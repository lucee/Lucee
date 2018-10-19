component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-2019", body=function() {
			it( title='Checking rereplace() with backslash before a numeric value',body=function( currentSpec ) {
				var result = rereplace("xabc","(a)","\\1");
				expect(result).toBe('x\1bc');
			});
			it( title='Checking rereplace() with backslash before some numeric values',body=function( currentSpec ) {
				var result = rereplace("xabc","(a)","\\123");
				expect(result).toBe('x\123bc');
			});
			it( title='Checking rereplace() with multiple backslash before a numeric value',body=function( currentSpec ) {
				var result = rereplace("xabc","(a)","\\\\4");
				expect(result).toBe('x\\4bc');
			});
		});
	}
}