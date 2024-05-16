component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run (testResults, textBox ) {
		describe(" Testcase for LDEV-3095 ",function(){
			it(title = "Checked isValid() with mail", body = function ( currentSpec ){
				expect(isValid("email","test@mail.com")).toBe("true");
				expect(isValid("email","test..test@mail.com")).toBe("true");
				expect(isValid("email",'test" "test@gmail.com')).toBe("false");
				expect(isValid("email",'" "test@mail.com')).toBe("false");
				expect(isValid("email","test@example")).toBe("true");
				expect(isValid("email",'test""test@mail.com')).toBe("false");
				expect(isValid("email",'test"test"test@mail.com')).toBe("false");
				expect(isValid("email","test@mail.c")).toBe("true");
				expect(isValid("email",".test@mail.com")).toBe("true");
			});
		})
	}
}