component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="Testcase for hmac() function", body=function() {
			var message = 'this is a test';
			var key = 'ABC123'
			it(title="Checking the hmac() with key argument", body=function( currentSpec ) {
				expect(hmac( message, key )).toBe("776770430C93778AD6F91B43A4A30B69");
			});
			it(title="Checking the hmac() with key & algorithm arguments", body=function( currentSpec ) {
				expect(hmac( message, key, "HmacMD5" )).toBe("776770430C93778AD6F91B43A4A30B69");
				expect(hmac( message, key, "HmacSHA1" )).toBe("049E53BAE339C4A05587D7BBBA2857548E8FC327");
				expect(hmac( message, key, "HmacSHA256" )).toBe("0503949602EDE3FF61C84F4CE51C99EEA2961CAA144AEE552F7D120AD6A60D7D");
				expect(hmac( message, key, "HMACSHA384" )).toBe("6FE95751F3C829B80C21B041700DFF5F8A512277F76C7C8C8C2AEE622561E2AE8C7852AB7270B88B5E2AA9D7841FF324");
				expect(hmac( message, key, "HMACSHA512" )).toBe("D8F6CCD1710633FA0A102A9CB4D9E52C66B838854889C34A04C0DB8A26C4A1EC996BB9A627C4C5C14FBACCD419E309F1FA7E356D6948D9773D9BD1D6645E2ECE");
			});
			it(title="Checking the hmac() with key, algorithm & encoding arguments", body=function( currentSpec ) {
				expect(hmac( message, key, "HmacMD5", "utf-8")).toBe("776770430C93778AD6F91B43A4A30B69");
				expect(hmac( message, key, "HmacSHA1", "iso-8859-1")).toBe("049E53BAE339C4A05587D7BBBA2857548E8FC327");
				expect(hmac( message, key, "HmacSHA256", "euc-cn")).toBe("0503949602EDE3FF61C84F4CE51C99EEA2961CAA144AEE552F7D120AD6A60D7D");
				expect(hmac( message, key, "HMACSHA384", "us-ascii")).toBe("6FE95751F3C829B80C21B041700DFF5F8A512277F76C7C8C8C2AEE622561E2AE8C7852AB7270B88B5E2AA9D7841FF324");
				expect(hmac( message, key, "HMACSHA512", "utf-16")).toBe("3D625C3F887D3D02FF4A3EBCD66312524BD5FFD59B00293818B7D925431B78C790E32C0D8D4FB9C11C2D43AFEF6E9B154AAA0F434C7356AAE848C7FAE2495689");
			});
		});
	}
}