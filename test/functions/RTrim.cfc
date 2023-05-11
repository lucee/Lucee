component extends="org.lucee.cfml.test.LuceeTestCase" {

	public function run( testResults , testBox ) {
		describe( title="Testcase for rTrim", body=function() {
			it(title="Checking with rTrim  function", body=function( currentSpec ) {
				assertEquals("   welcome", rTrim("   welcome "));
			});

			it(title="Checking with string.rTrim() member function", body=function( currentSpec ) {
				assertEquals("   welcome", "   welcome   ".rTrim());
			});
		});
	}
}