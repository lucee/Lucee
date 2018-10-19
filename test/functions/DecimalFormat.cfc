component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for DecimalFormat", function() {
			it(title = "Checking with DecimalFormat", body = function( currentSpec ) {
				assertEquals("x123.00", "x#toString(DecimalFormat (123))#");
				assertEquals("x123.00", "x#toString(DecimalFormat (123.00000000002))#");
				assertEquals("x123,456,789.00", "x#toString(DecimalFormat (123456789.00))#");
				assertEquals("x123,456.00", "x#toString(DecimalFormat (123456.00))#");

				assertEquals("x-123.00", "x#toString(DecimalFormat (-123))#");
				assertEquals("x-1,234.00", "x#toString(DecimalFormat (-1234))#");
				assertEquals("x-123.00", "x#toString(DecimalFormat (-123.00000000002))#");
				assertEquals("x-123,456,789.00", "x#toString(DecimalFormat (-123456789.00))#");
				assertEquals("x-123,456.00", "x#toString(DecimalFormat (-123456.00))#");
			});
		});	
	}
}