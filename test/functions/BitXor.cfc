component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for BitXOr()", body=function() {
			it(title="Checking BitXOr() function", body = function( currentSpec ) {
				assertEquals("2",BitXOr(1, 3));
			});

			it(title="Checking BitXOr() function with small integers", body = function(currentSpec) {
                assertEquals("2", BitXOr(1, 3));  // 01 XOR 11 = 10
            });

            it(title="Checking BitXOr() function with zero", body = function(currentSpec) {
                assertEquals("1", BitXOr(1, 0));  // 01 XOR 00 = 01
                assertEquals("0", BitXOr(0, 0));  // 00 XOR 00 = 00
            });

            it(title="Checking BitXOr() function with negative numbers", body = function(currentSpec) {
                assertEquals("-2", BitXOr(-1, 1));  // 111...11111111 XOR 000...00000001 = 111...11111110
            });

            it(title="Checking BitXOr() function with a large number outside int range", body = function(currentSpec) {
                // Using large numbers, like powers of two that would typically be beyond 32-bit integer range
                assertEquals("9223372036854775809", BitXOr("9223372036854775808", "1")); // 100...00000000 XOR 000...00000001 = 100...00000001
            });

            it(title="Checking BitXOr() function between two large numbers", body = function(currentSpec) {
                // Both numbers are large and outside the standard int range
                assertEquals("0", BitXOr("18446744073709551615", "18446744073709551615"));  // All bits are the same, so XOR results in 0
            });

            it(title="Checking BitXOr() function with different large numbers", body = function(currentSpec) {
                assertEquals("18446744073709551614", BitXOr("18446744073709551615", "1"));  // 111...11111111 XOR 000...00000001 = 111...11111110
            });

		});
	}
}