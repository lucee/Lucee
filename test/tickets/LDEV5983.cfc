component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	variables.testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV5983/";

	function run( testResults, testBox ) {

		describe( "LDEV-5983: cffunction body duplicates comments between cfargument tags", function() {

			it( "comments between cfargument tags should not be duplicated in AST", function() {
				var code = fileRead( variables.testDir & "commentBetweenArgs.cfm" );
				var ast = astFromString( code, "tag" );

				var commentCount = countStringInAST( ast, "<!--comment-->" );
				expect( commentCount ).toBe( 1, "Comment should appear exactly once in AST, found #commentCount# times" );
			});

			it( "cffunction with comment after last cfargument should be stable", function() {
				var code = fileRead( variables.testDir & "commentAfterLastArg.cfm" );
				var ast = astFromString( code, "tag" );

				var commentCount = countStringInAST( ast, "<!--comment-->" );
				expect( commentCount ).toBe( 1, "Comment should appear exactly once in AST, found #commentCount# times" );
			});

		});

	}

	/**
	 * Count occurrences of a string in StringLiteral values within the AST
	 */
	private numeric function countStringInAST( required struct ast, required string searchFor ) {
		var matches = structFindKey( ast, "value", "all" );
		var count = 0;
		for ( var match in matches ) {
			if ( isSimpleValue( match.value ) && find( arguments.searchFor, match.value ) ) {
				count++;
			}
		}
		return count;
	}

}
