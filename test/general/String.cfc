component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){}

	function afterAll(){}

	function run( testResults , testBox ) {
		describe( "tests for the type string", function() {

			it(title="use the underlaying method", body=function(){
				var originalString = "My phone number is 123-456-7890.";
				var regex = "\d";  
				var replacedString = originalString.replaceAll(regex, "*");
				expect(replacedString).toBe("My phone number is ***-***-****.");
			});
		});
	}
}