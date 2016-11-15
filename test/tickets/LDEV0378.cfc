component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-378", function() {
			it("Adding two decimal numbers, it gives Unexpected decimal numbers", function( currentSpec ){
				num1 = 50816.319;
				num2 = 1802.720;
				sum = num1 + num2;
				expect(sum).toBe(52619.039);
			});
		});
	}
}