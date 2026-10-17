component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("Testcase for LDEV-2899", function() {
			it(title="Checking booleanFormat() with nested structure", body=function( currentSpec ) {
				var a = { b: { c: true } };
				try {
					var result = booleanFormat(a?.c?.d);
				}
				catch(any e) {
					var result = e.message;
				}
				expect(result).toBe("false");
			});
			it(title="Checking booleanFormat() and yesNoFormat() with null", body=function( currentSpec ) {
				expect( booleanFormat( nullValue() ) ).toBe( "false" );
				expect( yesNoFormat( nullValue() ) ).toBe( "No" );
				// unchanged
				expect( booleanFormat( "" ) ).toBe( "false" );
				expect( booleanFormat( 1 ) ).toBe( "true" );
				expect( yesNoFormat( "" ) ).toBe( "No" );
				expect( yesNoFormat( true ) ).toBe( "Yes" );
			});
		});
	}
}
