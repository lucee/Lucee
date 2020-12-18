component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run() {
		describe( title="Test suite for reMatchnocase", body=function() {
			it( title='Test case for reMatchnocase function  ',body=function( currentSpec ) {
				assertEquals('["1","45","38"]',serializeJSON( reMatchnocase("[0-9]+", "1 way to extract any number like 45, 38") ));
				assertEquals('["lucee","Lucee"]',serializeJSON( reMatchnocase("(lucee)+", "I love lucee Lucee") ));
				assertEquals('["l","e","lucee","Lucee"]',serializeJSON( reMatchnocase("[lucee]+", "I love lucee Lucee") ));

			});

			it( title='Test case for reMatchnocase member function',body=function( currentSpec ) {
				assertEquals('["1","45","38"]',serializeJSON( "1 way to extract any number like 45, 38".reMatchnocase("[0-9]+") ));
				assertEquals('["lucee","Lucee"]',serializeJSON( "I love lucee Lucee".reMatchnocase("(lucee)+") ));
				assertEquals('["l","e","lucee","Lucee"]',serializeJSON( "I love lucee Lucee".reMatchnocase("[lucee]+") ));

			});
		});
	}
}