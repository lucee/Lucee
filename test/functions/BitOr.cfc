component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for BitOr()", body=function() {
			it(title="Checking BitOr() function", body = function( currentSpec ) {
				assertEquals("1", BitOr(1, 0));
			});

			it("should correctly perform bitwise OR between two positive numbers", function() {
				expect( BitOr(15, 9) ).toBe(15);
			});
			
			it("should correctly perform bitwise OR between a positive and a zero number", function() {
				expect( BitOr(15, 0) ).toBe(15);
			});
			
			it("should correctly perform bitwise OR between two negative numbers", function() {
				expect( BitOr(-15, -9) ).toBe(-9);
			});
			
			it("should correctly perform bitwise OR between a positive and a negative number", function() {
				expect( BitOr(15, -9) ).toBe(-1);
			});
			
			it("should handle bitwise OR where one number is the maximum integer value", function() {
				expect( BitOr(2147483647, 1) ).toBe(2147483647);
			});
			
			it("should return the non-zero value when one number is zero", function() {
				expect( BitOr(0, 0) ).toBe(0);
			});
			
			it("should correctly perform bitwise OR between two large BigInteger values", function() {
				expect( BitOr(9223372036854775808, 9223372036854775807) ).toBe(9223372036854775807);
			});
			
			it("should correctly perform bitwise OR between a BigInteger and a smaller integer", function() {
				// Expect the large number because all bits from the smaller number (255) are part of the larger number
				expect( BitOr(9223372036854775808, 255) ).toBe(9223372036854775808 + 255);
			});
			
			it("should correctly perform bitwise OR between a BigInteger and a smaller integer", function() {
				// Expect a value with low 8 bits set to 1, and all higher bits to 1 (same as the larger number)
				expect( BitOr(9223372036854775807, 255) ).toBe(9223372036854775807);
			});
			

		});
	}
}