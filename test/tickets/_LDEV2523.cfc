component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run() {
		describe( title = "Test suite for LDEV2523", body = function() {
			it( title = 'Test case for Array map member function skips null values',body = function( currentSpec ) {
				a = [JavaCast("null","")];
				b = arraymap(a,function(item) {
					return "lucee";
				});
				assertEquals("1",arraylen(a));
				assertEquals("false",arrayisempty(b));
				assertEquals("lucee",b[1]);
			});

			it( title = 'Test case for Array map member function skips null values for member function',body = function( currentSpec ) {
				a = [JavaCast("null","")];
				b = a.map(function(item) {
					return "lucee_core_dev";
				});
				assertEquals("1",arraylen(a));
				assertEquals("false",arrayisempty(b));
				assertEquals("lucee_core_dev",b[1]);
			});
		});
	}
}