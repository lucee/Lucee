component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	variables.testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6006/";

	function run( testResults, testBox ) {

		describe( "LDEV-6006: astFromString mishandles CFML comment syntax in string literals", function() {

			it( "handles <!--- inside string in pure script mode", function() {
				// Pure script mode works fine
				var code = 'x = "test <!--- comment";';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				expect( ast.body[1].type ).toBe( "AssignmentExpression" );
				expect( ast.body[1].right.value ).toInclude( "<!---" );
			});

			it( "should handle <!--- inside string in component context", function() {
				// Component code with <!--- in a string - must use "script" mode
				// astFromString defaults to tag mode, which would strip <!--- as comment
				var code = 'component { function test() { x = "^<!---.*--->$"; } }';
				var ast = astFromString( code, "script" );

				// Component is at root when using script mode
				expect( ast.body ).toHaveLength( 1,
					"Should have single element, got #arrayLen( ast.body )# elements" );
				expect( ast.body[1].type ).toBe( "CFMLTag",
					"Should be CFMLTag, got #ast.body[1].type#" );
				expect( ast.body[1].name ).toBe( "component",
					"Should be component tag, got #ast.body[1].name#" );

				// Check the string literal preserves the <!--- content
				var funcBody = ast.body[1].body.body[1].body.body;
				var assignment = funcBody[1];
				expect( assignment.right.value ).toInclude( "<!---",
					"String should contain <!--- comment syntax" );
			});

			it( "astFromPath should not wrap script component in cfscript", function() {
				// This is the fix for Quirk #22 - astFromPath should return
				// the component directly, not wrapped in cfscript
				var ast = astFromPath( variables.testDir & "commentInString.cfc" );

				expect( ast.body ).toHaveLength( 1, "Should have single body element" );
				expect( ast.body[1].type ).toBe( "CFMLTag", "Should be CFMLTag" );
				expect( ast.body[1].name ).toBe( "component",
					"Should be component directly, not wrapped in cfscript. Got: #ast.body[1].name#" );

				// Verify the function is inside the component
				var componentBody = ast.body[1].body.body;
				expect( componentBody ).toBeArray();
				var funcDecl = componentBody.filter( function( item ) {
					return ( item.type ?: "" ) == "FunctionDeclaration";
				});
				expect( funcDecl ).toHaveLength( 1, "Component should contain test function" );
			});

		});

	}

}
