component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	function run( testResults, testBox ) {

		describe( "LDEV-6013: AST MemberExpression should have computed=true for bracket notation", function() {

			it( "should have computed=true for bracket notation with string key", function() {
				var code = 'x = obj[ "key" ];';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				var right = ast.body[1].right;
				expect( right.type ).toBe( "MemberExpression" );
				expect( right.computed ).toBe( true,
					"Expected computed=true for bracket notation but got computed=#right.computed#" );
			});

			it( "should have computed=true for bracket notation with special characters in key", function() {
				var code = 'x = this.mappings[ "/tests" ];';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				var right = ast.body[1].right;
				expect( right.type ).toBe( "MemberExpression" );
				expect( right.computed ).toBe( true,
					"Expected computed=true for bracket notation with special chars but got computed=#right.computed#" );
				// Property should be StringLiteral, not Identifier
				expect( right.property.type ).toBe( "StringLiteral",
					"Expected property to be StringLiteral but got #right.property.type#" );
			});

			it( "should have computed=false for dot notation", function() {
				var code = 'x = obj.key;';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				var right = ast.body[1].right;
				expect( right.type ).toBe( "MemberExpression" );
				expect( right.computed ).toBe( false,
					"Expected computed=false for dot notation but got computed=#right.computed#" );
				// Property should be Identifier for dot notation
				expect( right.property.type ).toBe( "Identifier",
					"Expected property to be Identifier but got #right.property.type#" );
			});

			it( "should have computed=true for bracket notation with variable key", function() {
				var code = 'x = obj[ idx ];';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				var right = ast.body[1].right;
				expect( right.type ).toBe( "MemberExpression" );
				expect( right.computed ).toBe( true,
					"Expected computed=true for bracket notation with variable but got computed=#right.computed#" );
			});

			it( "should have computed=true for bracket notation with numeric key", function() {
				var code = 'x = arr[ 1 ];';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				var right = ast.body[1].right;
				expect( right.type ).toBe( "MemberExpression" );
				expect( right.computed ).toBe( true,
					"Expected computed=true for numeric index but got computed=#right.computed#" );
			});

			it( "should preserve bracket notation on assignment left side", function() {
				var code = 'this.mappings[ "/tests" ] = "foo";';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				var left = ast.body[1].left;
				expect( left.type ).toBe( "MemberExpression" );
				expect( left.computed ).toBe( true,
					"Expected computed=true on left side of assignment but got computed=#left.computed#" );
			});

		});

	}

}
