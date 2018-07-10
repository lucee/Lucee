component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1582", body=function() {
			it(title="Checking mask value '99.00' with negative number ", body = function( currentSpec ) {
				var result = numberformat(-2.5, "99.00");
				expect(result).toBe('-2.50');
			});
			it(title="Checking mask value '99.99' with negative number ", body = function( currentSpec ) {
				var result = numberformat(-2.5, "99.99");
				expect(result).toBe('-2.50');
			});
			it(title="Checking mask value '9.00' with negative number ", body = function( currentSpec ) {
				var result = numberformat(-2.5, "9.00");
				expect(result).toBe('-2.50');
			});
			it(title="Checking mask value '00.00' with negative number ", body = function( currentSpec ) {
				var result = numberformat(-2.5, "00.00");
				expect(result).toBe('-02.50');
			});
			it(title="Checking mask value '__.00' with negative number ", body = function( currentSpec ) {
				var result = numberformat(-2.5, "__.00");
				expect(result).toBe('-2.50');
			});
		});
	}
}
