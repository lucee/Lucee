component extends="org.lucee.cfml.test.LuceeTestCase" labels="booleanFormat" {
	function run( testResults, testBox ) {
		describe( "LDEV-4589 booleanFormat() member function", function() {
			it( "numeric variable", function() {
				var value = 1;
				expect( value.booleanFormat() ).toBe( "true" );
				var zero = 0;
				expect( zero.booleanFormat() ).toBe( "false" );
			});
			it( "string variable", function() {
				expect( "false".booleanFormat() ).toBe( "false" );
				expect( "123".booleanFormat() ).toBe( "true" );
			});
			it( "yesNoFormat() member function on numbers and strings", function() {
				var value = 1;
				expect( value.yesNoFormat() ).toBe( "Yes" );
				expect( "no".yesNoFormat() ).toBe( "No" );
			});
			it( "string that is not a boolean still has no booleanFormat() member", function() {
				expect( function() { "abc".booleanFormat(); } ).toThrow();
			});
			it( "boolean variable", function() {
				var b = true;
				expect( b.booleanFormat() ).toBe( "true" );
			});
		});
	}
}
