component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe(title="Testcase for jsStringFormat()", body=function() {
			it(title="Checking jsStringFormat() function", body=function( currentSpec ) {
				var str = "Bob's comment was CFML rocks!";
				expect( jsStringFormat(str) ).toBe("Bob\'s comment was CFML rocks!");
				expect( jsStringFormat("I Love Lucee''") ).toBe("I Love Lucee\'\'");
				expect( jsStringFormat('I Love "Lucee"') ).toBe('I Love \"Lucee\"');
				expect( jsStringFormat("Lucee@@@") ).toBe("Lucee@@@");
			});
		});
	}
}