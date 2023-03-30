component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( "testcase for decodeFromURL()", function() {
			it(title = "Checking with decodeFromURL()", body = function( currentSpec ) {
				expect(function() {
					expect( decodeFromURL(EncodeForURL('https://test.example/api/v1/index.html?uname=tester&lang=en&param1=test')) ).toBe('https://test.example/api/v1/index.html?uname=tester&l=en&p1=test');
				}).notToThrow();
			});
		});
	}
}
