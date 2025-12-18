component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	function run( testResults, testBox ) {

		var h = chr( 35 ); // hash character

		describe( "LDEV-6014: AST should preserve dynamic property assignment with interpolated string key", function() {

			it( "should produce AssignmentExpression for interpolated string key assignment", function() {
				// Code: "#scope#.#name#" = value;
				var code = '"#h#scope#h#.#h#name#h#" = value;';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				var stmt = ast.body[1];
				expect( stmt.type ).toBe( "AssignmentExpression",
					"Expected AssignmentExpression but got #stmt.type#" );
			});

			it( "should have left side as interpolated string, not MemberExpression", function() {
				var code = '"#h#scope#h#.#h#name#h#" = value;';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				var stmt = ast.body[1];
				expect( stmt.type ).toBe( "AssignmentExpression" );
				// Left side should be the interpolated string expression
				expect( stmt.left.type ).notToBe( "MemberExpression",
					"Left side should be interpolated string, not MemberExpression" );
			});

			it( "should have right side as the assigned value", function() {
				var code = '"#h#scope#h#.#h#name#h#" = value;';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				var stmt = ast.body[1];
				expect( stmt.type ).toBe( "AssignmentExpression" );
				expect( stmt.right.type ).toBe( "Identifier",
					"Expected right side Identifier but got #stmt.right.type#" );
				expect( stmt.right.name ).toBe( "VALUE",
					"Expected right side name 'VALUE' but got '#stmt.right.name ?: 'null'#'" );
			});

			it( "should not have operator on MemberExpression (MockBox pattern)", function() {
				// Code: "#arguments.propertyScope#.#arguments.propertyName#" = arguments.mock;
				var code = '"#h#arguments.propertyScope#h#.#h#arguments.propertyName#h#" = arguments.mock;';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				var stmt = ast.body[1];
				// If it's incorrectly a MemberExpression, it shouldn't have operator
				if ( stmt.type == "MemberExpression" ) {
					expect( structKeyExists( stmt, "operator" ) ).toBe( false,
						"MemberExpression should not have operator key but has operator=#stmt.operator ?: 'null'#" );
				}
			});

			it( "should handle realistic dynamic property pattern from MockBox", function() {
				// Code: "#arguments.propertyScope#.#arguments.propertyName#" = arguments.mock;
				var code = '"#h#arguments.propertyScope#h#.#h#arguments.propertyName#h#" = arguments.mock;';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				var stmt = ast.body[1];
				expect( stmt.type ).toBe( "AssignmentExpression",
					"Expected AssignmentExpression but got #stmt.type#" );
				expect( stmt.right.type ).toBe( "MemberExpression",
					"Expected right side MemberExpression but got #stmt.right.type#" );
			});

		});

	}

}
