component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	variables.testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6019/";

	function run( testResults, testBox ) {

		describe( "LDEV-6019: Unquoted function attributes after parenthesis should parse", function() {

			it( "should parse function with unquoted access=remote attribute", function() {
				// This should not throw an error
				var ast = astFromPath( variables.testDir & "unquotedAccess.cfc" );

				expect( ast.type ).toBe( "Program" );
				// Find the function and verify it has access attribute
				var func = ast.body[ 1 ].body.body[ 1 ];
				expect( func.type ).toBe( "FunctionDeclaration" );
				expect( func.access ).toBe( "remote" );
			});

			it( "should parse function with colon syntax access:remote attribute", function() {
				var ast = astFromPath( variables.testDir & "colonAccess.cfc" );

				expect( ast.type ).toBe( "Program" );
				var func = ast.body[ 1 ].body.body[ 1 ];
				expect( func.type ).toBe( "FunctionDeclaration" );
				expect( func.access ).toBe( "remote" );
			});

			it( "should parse function with unquoted boolean attribute", function() {
				var ast = astFromPath( variables.testDir & "unquotedBoolean.cfc" );

				expect( ast.type ).toBe( "Program" );
				var func = ast.body[ 1 ].body.body[ 1 ];
				expect( func.type ).toBe( "FunctionDeclaration" );
			});

		});

	}

}
