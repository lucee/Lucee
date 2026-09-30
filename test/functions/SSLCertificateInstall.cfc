component extends="org.lucee.cfml.test.LuceeTestCase" labels="ssl" {

	function run() {
		describe("SSLCertificateInstall()", function() {

			it("should install SSL certificates for google.com without error", function() {
				expect(function() {
					SSLCertificateInstall("google.com");
				}).notToThrow(); // LDEV-5571 - now uses custom-cacerts store
			});

			it("should install SSL certificates for google.com into custom caerts path, bad password to error", function() {
				expect(function() {
					SSLCertificateInstall("google.com", 443, getPageContext().getConfig().getSecurityDirectory(), "changeme");
				}).toThrow(); // bad password
			});

			it("should install SSL certificates for google.com into custom caerts path without error", function() {
				expect(function() {
					SSLCertificateInstall("google.com", 443, getPageContext().getConfig().getSecurityDirectory(), "changeit");
				}).notToThrow(); // disabled since LDEV-917 - use jvm cacerts
			});

			it("should install SSL certificates for google.com into custom caerts with error", function() {
				expect(function() {
					SSLCertificateInstall("google.com", 443, getTempFile("","cacerts","tmp"), "changeme");
				}).toThrow(); // bad cacerts file, it's empty
			});

		});
	}

}
