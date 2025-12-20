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

		describe( "LDEV-6016: Static block position in AST", function() {

			it( "should preserve tag-style static function position relative to other members", function() {
				// Source: cfproperty (line 2), static cffunction (line 4-6), cffunction init (line 8)
				var ast = astFromPath( variables.testDir & "staticTagFunctionPosition.cfc" );
				var comp = ast.body[1];

				// Filter to meaningful nodes (skip whitespace/expression statements)
				var members = comp.body.body.filter( function( s ) {
					return s.type == "CFMLTag" || ( s.type == "BlockStatement" && structKeyExists( s, "static" ) );
				});

				// Should be: cfproperty, static function wrapper, cffunction init (in source order)
				expect( members ).toHaveLength( 3, "Should have 3 members (property, static function, init function)" );

				// First should be cfproperty
				expect( members[1].type ).toBe( "CFMLTag" );
				expect( members[1].name ).toBe( "property" );

				// Second should be the static function (wrapped in BlockStatement)
				// OR the cffunction directly if not wrapped
				if ( members[2].type == "BlockStatement" ) {
					expect( members[2].static ).toBe( true, "Static function wrapper should be at original position (after property)" );
					// The function inside should be getVersion
					var innerFunc = members[2].body.filter( function( n ) { return n.type == "CFMLTag"; })[1];
					var nameAttr = innerFunc.attributes.filter( function( a ) { return a.name == "name"; })[1];
					expect( nameAttr.value.value ).toBe( "getVersion", "Static function getVersion should be first" );
				} else {
					// If cffunction with static marker
					expect( members[2].name ).toBe( "function" );
					var nameAttr = members[2].attributes.filter( function( a ) { return a.name == "name"; })[1];
					expect( nameAttr.value.value ).toBe( "getVersion", "Static function getVersion should be first" );
				}

				// Third should be cffunction init
				if ( members[3].type == "BlockStatement" ) {
					// Bug: init was hoisted, static is at end - this is WRONG
					fail( "Bug: init function should be third, not hoisted to second position" );
				}
				expect( members[3].type ).toBe( "CFMLTag" );
				expect( members[3].name ).toBe( "function" );
				var nameAttr3 = members[3].attributes.filter( function( a ) { return a.name == "name"; })[1];
				expect( nameAttr3.value.value ).toBe( "init", "init function should be last" );
			});

			it( "should preserve static block position relative to other members", function() {
				// Source: cfproperty (line 2), cfstatic (line 4-6), cffunction (line 8)
				var ast = astFromPath( variables.testDir & "staticBlockPosition.cfc" );
				var comp = ast.body[1];

				// Filter to meaningful nodes (skip whitespace/expression statements)
				var members = comp.body.body.filter( function( s ) {
					return s.type == "CFMLTag" || ( s.type == "BlockStatement" && structKeyExists( s, "static" ) );
				});

				// Should be: cfproperty, static BlockStatement, cffunction (in source order)
				expect( members ).toHaveLength( 3, "Should have 3 members (property, static, function)" );

				// First should be cfproperty
				expect( members[1].type ).toBe( "CFMLTag" );
				expect( members[1].name ).toBe( "property" );

				// Second should be static block (at original position)
				expect( members[2].type ).toBe( "BlockStatement" );
				expect( members[2].static ).toBe( true, "Static block should be at original position" );

				// Third should be cffunction
				expect( members[3].type ).toBe( "CFMLTag" );
				expect( members[3].name ).toBe( "function" );
			});

			it( "static block should have start position info", function() {
				var ast = astFromPath( variables.testDir & "staticBlockPosition.cfc" );
				var comp = ast.body[1];

				var staticBlocks = comp.body.body.filter( function( s ) {
					return s.type == "BlockStatement" && structKeyExists( s, "static" ) && s.static == true;
				});

				expect( staticBlocks ).toHaveLength( 1 );
				expect( staticBlocks[1] ).toHaveKey( "start", "Static block should have start position" );
				expect( staticBlocks[1].start.line ).toBe( 4, "Static block should start at line 4" );
			});

		});
	}

}
