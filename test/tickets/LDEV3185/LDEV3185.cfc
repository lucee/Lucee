component extends="org.lucee.cfml.test.LuceeTestCase"{
	public function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-3185", body=function() {
			it(title="CSRFGenerateToken with and without key", body=function( currentSpec ) {
				token = CSRFGenerateToken("lucee",true);
				expect(token).toHaveLength(40, token);
				token = CSRFGenerateToken("lucee");
				expect(token).toHaveLength(40, token);
				token = CSRFGenerateToken();
				expect(token).toHaveLength(40, token);
			});
		});
	}
}
