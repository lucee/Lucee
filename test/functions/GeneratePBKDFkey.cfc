component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="Testcase for generatePBKDFkey() function", body=function() {
			it(title="Checking the generatePBKDFkey() function with positional arguments", body=function( currentSpec ) {
				expect(generatePBKDFKey("PBKDF2WithHmacSHA1", "secret", "salty", 5000, 128)).toBe("Y0MCpCe3zb0CNJvyXNUWEQ==");
			});
			it(title="Checking the generatePBKDFkey() function with named arguments", body=function( currentSpec ) {
				expect(generatePBKDFKey(algorithm="PBKDF2WithHmacSHA1", passphrase="secret", salt="salty", iterations=5000, keySize=128)).toBe("Y0MCpCe3zb0CNJvyXNUWEQ==");
			});
		});
	}
}
