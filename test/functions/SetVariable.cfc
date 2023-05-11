component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, textbox ) {
		describe(title="Testcase for setVariable() function", body=function() {
			it(title="Checking the setVariable() function", body=function( currentSpec ) {
				var inputString = "Welcome to all";
				setVariable(name = "inputString", value = "I love lucee");
				expect(inputString).toBe("I love lucee");

				setVariable("session.testVariable", "Save tree");
				expect(session.testVariable).toBe("Save tree");

				setVariable("cookie.testVariable", "Save tree");
				expect(cookie.testVariable).toBe("Save tree");

				setVariable("form.testVariable", "Save tree");
				expect(form.testVariable).toBe("Save tree");

				setVariable("url.testVariable", "Save tree");
				expect(url.testVariable).toBe("Save tree");
			});
		});
	}
}
