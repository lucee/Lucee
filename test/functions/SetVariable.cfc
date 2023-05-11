component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, textbox ) {
		describe(title="Testcase for setVariable() function", body=function() {
			it(title="Checking the setVariable() function", body=function( currentSpec ) {
				var inputString = "Welcome to all";
				setVariable(name = "inputString", value = "I love lucee");
				expect(inputString).toBe("I love lucee");

				setVariable("session.test", "Save tree");
				expect(session.test).toBe("Save tree");

				setVariable("cookie.test", "Save tree");
				expect(cookie.test).toBe("Save tree");

				setVariable("form.test", "Save tree");
				expect(form.test).toBe("Save tree");

				setVariable("url.test", "Save tree");
				expect(url.test).toBe("Save tree");
			});
		});
	}
}