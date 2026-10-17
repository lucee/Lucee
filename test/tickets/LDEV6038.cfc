component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	function run( testResults, testBox ) {
		describe( "LDEV-6038: Comments should not leave ghost nodes in AST", function() {

			it( "should not create empty StringLiteral placeholder for stripped comments", function() {
				var ast = astFromPath( getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6038/test.cfm" );

				// Source: <cfset x = 1><!--- my comment --->
				// Should have 1 body element (the cfset tag)
				// Bug: Has 2 body elements (cfset + empty StringLiteral ghost node)

				expect( ast.body ).toBeArray();
				expect( ast.body.len() ).toBe( 1, "AST should have exactly 1 body element, not a ghost node for the comment" );
				expect( ast.body[1].type ).toBe( "CFMLTag" );
				expect( ast.body[1].name ).toBe( "set" );
			});

			it( "should not create ghost nodes for comments between tags", function() {
				var code = '<cfset a = 1><!--- comment ---><cfset b = 2>';
				var ast = astFromString( code );

				// Should have 2 body elements (two cfset tags)
				// Bug: Has 3 body elements (cfset + empty ghost + cfset)

				expect( ast.body ).toBeArray();
				expect( ast.body.len() ).toBe( 2, "AST should have exactly 2 body elements" );
				expect( ast.body[1].name ).toBe( "set" );
				expect( ast.body[2].name ).toBe( "set" );
			});

			it( "should not create ghost nodes for trailing comments", function() {
				var code = '<cfmodule template="test.cfm">content</cfmodule><!--- doc_layout !--->';
				var ast = astFromString( code );

				// Should have 1 body element (the cfmodule tag)
				// Bug: Has 2 body elements (cfmodule + empty StringLiteral ghost)

				expect( ast.body ).toBeArray();
				expect( ast.body.len() ).toBe( 1, "AST should have exactly 1 body element for cfmodule" );
				expect( ast.body[1].type ).toBe( "CFMLTag" );
				expect( ast.body[1].name ).toBe( "module" );
			});

		});
	}

}
