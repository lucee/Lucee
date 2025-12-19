component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	variables.testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6016/";

	function run( testResults, testBox ) {

		describe( "LDEV-6016: AST static blocks should have static marker", function() {

			it( "should mark static block with static=true", function() {
				var ast = astFromPath( variables.testDir & "staticBlock.cfc" );
				var comp = ast.body[1];
				// Find the static block in the component body
				var staticBlock = comp.body.body.filter( function( s ) {
					return ( s.type == "BlockStatement" || s.type == "CFMLTag" ) && structKeyExists( s, "static" );
				});
				expect( staticBlock ).toHaveLength( 1, "Should have one static block" );
				expect( staticBlock[1].static ).toBe( true, "Static block should have static=true" );
			});

			it( "should preserve final keyword on assignment inside static block", function() {
				var ast = astFromPath( variables.testDir & "staticBlock.cfc" );
				var comp = ast.body[1];
				// Find static block
				var staticBlocks = comp.body.body.filter( function( s ) {
					return structKeyExists( s, "static" ) && s.static == true;
				});
				expect( staticBlocks ).toHaveLength( 1, "Should have one static block" );
				// Find the assignment inside
				var body = staticBlocks[1].body;
				var assignment = body[1];
				expect( assignment.type ).toBe( "AssignmentExpression" );
				expect( assignment ).toHaveKey( "final", "Assignment should have final marker" );
				expect( assignment.final ).toBe( true, "Assignment should be marked final" );
			});

			it( "should NOT merge multiple static blocks", function() {
				var ast = astFromPath( variables.testDir & "multipleStaticBlocks.cfc" );
				var comp = ast.body[1];

				// Find static blocks
				var staticBlocks = comp.body.body.filter( function( s ) {
					return structKeyExists( s, "static" ) && s.static == true;
				});

				// Should have 3 separate static blocks:
				// - static { static1=1; }
				// - static { static2=2; }
				// - static function foo() (wrapped in cfstatic)
				expect( staticBlocks ).toHaveLength( 3, "Should preserve separate static blocks" );

				// Verify each has the expected content
				expect( staticBlocks[1].body ).toHaveLength( 1, "First static block should have 1 statement" );
				expect( staticBlocks[2].body ).toHaveLength( 1, "Second static block should have 1 statement" );
				expect( staticBlocks[3].body ).toHaveLength( 1, "Third static block (function) should have 1 statement" );
				expect( staticBlocks[3].body[1].type ).toBe( "FunctionDeclaration", "Third block should contain the function" );
			});

			it( "should NOT merge script static block with tag cffunction modifier=static", function() {
				var ast = astFromPath( variables.testDir & "mixedStaticTagScript.cfc" );
				var comp = ast.body[1];

				// Find static blocks
				var staticBlocks = comp.body.body.filter( function( s ) {
					return structKeyExists( s, "static" ) && s.static == true;
				});

				// Should have 2 separate static blocks:
				// - static { static1=1; } (script)
				// - cffunction modifier="static" (tag)
				expect( staticBlocks ).toHaveLength( 2, "Should preserve separate static blocks (script and tag)" );

				// First should be the script static block with AssignmentExpression
				expect( staticBlocks[1].body ).toHaveLength( 1, "Script static block should have 1 statement" );
				expect( staticBlocks[1].body[1].type ).toBe( "AssignmentExpression", "Should be assignment" );

				// Second should be the cffunction tag
				expect( staticBlocks[2].body ).toHaveLength( 1, "Tag static block should have 1 statement" );
				expect( staticBlocks[2].body[1].type ).toBe( "CFMLTag", "Should be CFMLTag (cffunction)" );
				expect( staticBlocks[2].body[1].fullname ).toBe( "cffunction", "Should be cffunction tag" );
			});

		});
	}

}
