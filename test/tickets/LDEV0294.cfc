component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe(title="Test suite for LDEV-294", body=function() {
			it(title="checking param value, when the variable unavailable", body=function() {
				count = 0;
				param URL.test = defaulter();
				expect(count).toBe(1);
			});

			it(title="checking param value, when the variable available", body=function() {
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