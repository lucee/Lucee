component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for BitNot()", body=function() {
			it(title="checking BitNot() function", body = function( currentSpec ) {
				assertEquals("-2",BitNot(1));
				assertEquals("-1",BitNot(0));
				assertEquals("-13",BitNot(12));
			});

			it(title="checking BitNot() function with a large number outside int range", body = function(currentSpec) {
                // Large number beyond the int range, e.g., 2^62
                var largeNumber = "4611686018427387904";  // This is treated as BigDecimal in Lucee
                var result = BitNot(largeNumber);
                var expected = "-4611686018427387905";  // Expected result after applying BitNot
                assertEquals(expected, toString(result));
            });
		});
	}
}