component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run() {
		describe( title="Test suite for removeChars", body=function() {
			it( title='Test case for removeChars function  ',body=function( currentSpec ) {
				assertEquals('Hello World',removeChars('Hello CFML World',6,5));
				assertEquals('HelloCFML World',removeChars('Hello CFML World',6,1));

			});

			it( title='Test case for removeChars member function',body=function( currentSpec ) {
				assertEquals('Hello World','Hello CFML World'.removeChars(6,5));
				assertEquals('HelloCFML World','Hello CFML World'.removeChars(6,1));

			});
		});
	}
}