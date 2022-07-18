component extends = "org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults, testbox ){
		describe("Test case for LDEV2642", function(){
			it(title = "Check isvalid function with boolean'", body = function( currentSpec ){
				expect(isvalid("integer",'true')).tobe(false);
				expect(isvalid("integer",'false')).tobe(false);
				expect(isvalid("integer",'YES')).tobe(false);
				expect(isvalid("integer",'NO')).tobe(false);
			});
			it(title = "Check isvalid function with string and numbers'", body = function( currentSpec ){
				expect(isvalid("integer",'lucee')).tobe(false);
				expect(isvalid("integer",11)).tobe(true);
				expect(isvalid("integer",'31')).tobe(true);
				expect(isValid("integer",235.)).tobe(true);
				expect(isValid("integer",235.1)).tobe(false);
				expect(isValid("integer",235.0)).tobe(true);
				expect(isValid("integer",-2.67)).tobe(false);
				expect(isValid("integer","235.1")).tobe(false);
				expect(isValid("integer","235.")).tobe(true);
				expect(isValid("integer","235.0")).tobe(true);
			});
		});
	}
}