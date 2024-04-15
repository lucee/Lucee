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
				assertEquals("1",BitAnd(1, 1.9999999999999));
				assertEquals("0",BitAnd(1, 0.00000000000001));
			});

			it("should correctly perform bitwise AND between two positive numbers", function() {
                expect( BitAnd(15, 9) ).toBe(9);
            });

            it("should correctly perform bitwise AND between a positive and a zero number", function() {
                expect( BitAnd(15, 0) ).toBe(0);
            });

            it("should correctly perform bitwise AND between two negative numbers", function() {
                expect( BitAnd(-15, -9) ).toBe(-15);
            });

            it("should correctly perform bitwise AND between a positive and a negative number", function() {
                expect( BitAnd(15, -9) ).toBe(7);
            });

            it("should handle bitwise AND where one number is the maximum integer value", function() {
                expect( BitAnd(2147483647, 1) ).toBe(1);
            });

            it("should return 0 when both numbers are zero", function() {
                expect( BitAnd(0, 0) ).toBe(0);
            });

			it("should correctly perform bitwise AND between two large BigInteger values", function() {
                expect( BitAnd(9223372036854775808, 9223372036854775807) ).toBe(0);
            });

            it("should correctly perform bitwise AND between a BigInteger and a smaller integer", function() {
                // Expect zero because 255 does not overlap with high bits of largeNumber
				expect( BitAnd(9223372036854775808, 255) ).toBe(0);
            });
            it("should correctly perform bitwise AND between a BigInteger and a smaller integer", function() {
                // Expect 255 because the left number is all 1 in bit representaion
				expect( BitAnd(9223372036854775807, 255) ).toBe(255);
            });
		});
	}
}


