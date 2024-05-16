component extends="org.lucee.cfml.test.LuceeTestCase" labels="binary" {
	public function run( testResults, testBox ) {
		describe( title="Testcase for isBinary() function", body=function() {
			it(title="Checking the isBinary() function", body=function( currentSpec ) {
				expect(isBinary(ToBinary(toBase64("I am a string.")))).toBeTrue();
				expect(isBinary(arrayNew(1))).toBeFalse();
				expect(isBinary(true)).toBeFalse();
				expect(isBinary(1010)).toBeFalse();
				expect(isBinary("binary")).toBeFalse();
			});
		});
	}
}