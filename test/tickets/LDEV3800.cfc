component extends = "org.lucee.cfml.test.LuceeTestCase" skip="true"{
	function run( testResults, testBox ){
		describe( "Test case for LDEV-2382",function() {
			it( title = "Checking isvalid(email , email with german characters)",body = function( currentSpec ){
				expect(isValid("email","test@müller.de")).toBeTrue(); 
				expect(isValid("email","test@müller.çöm")).toBeTrue();
				expect(isValid("email","somthingçöm@gmail.com")).toBeTrue();
				expect(isValid("email","somthingçöm@çöm.com")).toBeTrue();
				expect(isValid("email","somthingçöm@gmail..com")).toBeFalse();
				expect(isValid("email","somthing@gmail..çöm")).toBeFalse();
				expect(isValid("email", "error@domain.com 😄")).toBeFalse(); // LDEV-2461
				expect(isValid('email','foo@bar'&chr(8207) )).toBeFalse(); // LDEV-3677
			});
		});
	}
}