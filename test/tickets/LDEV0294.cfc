component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-294", function() {
			it("checking param value, when the varaible unavailable", function() {
				count = 0;
				param URL.test = defaulter();
				expect(count).toBe(1);
			});

			it("checking param value, when the varaible available", function() {
				count = 0;
				URL.test = "test";
				param URL.test = defaulter();
				expect(count).toBe(0);
			});
		});
	}
	
	// Private function //
	private function defaulter(){
		count++;
		return count;
	}
}