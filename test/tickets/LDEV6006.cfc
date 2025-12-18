component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	variables.testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6006/";

	function run( testResults, testBox ) {

		describe( "LDEV-6006: astFromString mishandles CFML comment syntax in string literals", function() {

			it( "should parse component with <!--- in string literal correctly", function() {
				// astFromPath wraps script components in cfscript tag
				var astFromFile = astFromPath( variables.testDir & "commentInString.cfc" );
				expect( astFromFile.body ).toHaveLength( 1, "astFromPath should return 1 body element" );
				expect( astFromFile.body[1].type ).toBe( "CFMLTag", "astFromPath should return CFMLTag" );
				expect( astFromFile.body[1].name ).toBe( "script", "script-based .cfc is wrapped in cfscript" );

				// Component should be inside the cfscript body
				var scriptBody = astFromFile.body[1].body.body;
				expect( scriptBody ).toBeArray();
				var componentTag = scriptBody.filter( function( item ) {
					return ( item.type ?: "" ) == "CFMLTag" && ( item.name ?: "" ) == "component";
				});
				expect( componentTag ).toHaveLength( 1, "component should be inside cfscript body" );
			});

			it( "handles <!--- inside string in pure script mode", function() {
				// Pure script mode works fine
				var code = 'x = "test <!--- comment";';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				expect( ast.body[1].type ).toBe( "AssignmentExpression" );
				expect( ast.body[1].right.value ).toInclude( "<!---" );
			});

			it( "should handle <!--- inside string in component context", function() {
				// Component code with <!--- in a string - auto-detected as script mode
				var code = 'component { function test() { x = "^<!---.*--->$"; } }';
				var ast = astFromString( code );

				// astFromString auto-detects script mode, component is at root
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

		});

	}

}
