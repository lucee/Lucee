component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for BitAnd()", body=function() {
			it(title="Checking BitAnd() function integers", body = function( currentSpec ) {
				assertEquals("0",BitAnd(1, 0));
				assertEquals("0",BitAnd(0, 0));
				assertEquals("0",BitAnd(1, 2));
				assertEquals("1",BitAnd(1, 3));
				assertEquals("1",BitAnd(3, 5));
			});

			it(title="Checking BitAnd() function float like integers", body = function( currentSpec ) {
				assertEquals("1",BitAnd(1, 1.0));
				assertEquals("0",BitAnd(1, 0.0));
			});

			it(title="Checking BitAnd() function float edge case ", body = function( currentSpec ) {
				// they can be converted because they are below the threshold
				assertEquals("1",BitAnd(1, 0.9999999999999));
				assertEquals("0",BitAnd(1, 0.00000000000001));
			});

		});
	}
}