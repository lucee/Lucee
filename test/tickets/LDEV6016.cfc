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

		});
	}

}
