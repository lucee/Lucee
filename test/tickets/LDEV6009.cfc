component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	function run( testResults, testBox ) {

		describe( "LDEV-6009: AST should include compiler metadata", function() {

			it( "should include sourceType='script' when parsed as script", function() {
				var code = 'x = 1;';
				var ast = astFromString( code, "script" );

				expect( ast.type ).toBe( "Program" );
				expect( ast ).toHaveKey( "sourceType" );
				expect( ast.sourceType ).toBe( "script" );
			});

			it( "should include sourceType='tag' when parsed as tag", function() {
				var code = '<cfset x = 1>';
				var ast = astFromString( code );

				expect( ast.type ).toBe( "Program" );
				expect( ast ).toHaveKey( "sourceType" );
				expect( ast.sourceType ).toBe( "tag" );
			});

			it( "should include dotNotationUpperCase metadata", function() {
				var code = 'x = 1;';
				var ast = astFromString( code, "script" );

				expect( ast.type ).toBe( "Program" );
				expect( ast ).toHaveKey( "dotNotationUpperCase" );
				expect( ast.dotNotationUpperCase ).toBeBoolean();
			});

			it( "should include handleUnquotedAttrValueAsString metadata", function() {
				var code = '<cfset x = 1>';
				var ast = astFromString( code );

				expect( ast.type ).toBe( "Program" );
				expect( ast ).toHaveKey( "handleUnquotedAttrValueAsString" );
				expect( ast.handleUnquotedAttrValueAsString ).toBeBoolean();
			});

			it( "should include all metadata in astFromPath results", function() {
				var testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6009/";
				var ast = astFromPath( testDir & "test.cfc" );

				expect( ast.type ).toBe( "Program" );
				expect( ast ).toHaveKey( "sourceType" );
				expect( ast ).toHaveKey( "dotNotationUpperCase" );
				expect( ast ).toHaveKey( "handleUnquotedAttrValueAsString" );
			});

		});

	}

}
