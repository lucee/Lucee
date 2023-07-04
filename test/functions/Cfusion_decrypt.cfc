component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, textbox ) {
		describe(title="Testcase for Cfusion_decrypt() function", body=function() {
			it(title="Checking the Cfusion_decrypt() function", body=function( currentSpec ) {
				var key = "T5JalcfiANtOA+3V+02Ccw==";
				var string = "Lucee Association Switzerland (LAS)";
				var encrypted_string = Cfusion_encrypt(string, key);

				expect(Cfusion_decrypt(encrypted_string, key)).toBe("Lucee Association Switzerland (LAS)");
				expect(Cfusion_decrypt(string = Cfusion_encrypt(string = "Save Tree!", key = "@!!6839"), key = "@!!6839")). toBe("Save Tree!");
			});
		});
	}
}