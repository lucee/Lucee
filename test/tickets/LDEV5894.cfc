component extends="org.lucee.cfml.test.LuceeTestCase" javaSettings='{
		"maven": [
			"com.icegreen:greenmail:2.1.7"
		]
	}' {

	import "com.icegreen.greenmail.util.ServerSetup";
	import "org.lucee.extension.mail.SMTPVerifier";
 
	private function priv() {
		return true;
	}
	

	function run( testResults , testBox ) {
		describe( "test suite for LDEV5894", function() {
			it(title = "test closure in closure in  in closure", body = function( currentSpec ) {

				var x= function() {
					return function() {
						return com.icegreen.greenmail.util.ServerSetup::PROTOCOL_SMTP;
					}
				};
				expect(x()()).toBe("smtp");
			});

			it(title = "call private function", body = function( currentSpec ) {

				var x= function() {
					return function() {
						return priv();
					}
				};
				expect(x()()).toBeTrue();
			});

		});
	}
}