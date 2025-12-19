component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	function run( testResults, testBox ) {

		describe( "LDEV-6018: String concatenation should not wrap operands in CastExpression", function() {

			it( "should not add typeAnnotation CastExpression wrapper to concat operands", function() {
				var code = 'x = foo & "bar";';
				var ast = astFromString( code, "script" );

				// Find the BinaryExpression
				var binExpr = ast.body[ 1 ].right;
				expect( binExpr.type ).toBe( "BinaryExpression" );
				expect( binExpr.operator ).toBe( "CONCAT" );

				// Bug: left operand is wrapped in CastExpression with typeAnnotation: "string"
				// This is internal type coercion info that shouldn't be in the AST
				expect( binExpr.left.type ).toBe( "Identifier", "Left operand should be Identifier, not CastExpression" );
				expect( binExpr.left.name ).toBe( "FOO" );
			});

			it( "should preserve simple identifier in string concatenation", function() {
				var code = 'result = path & "/file.txt";';
				var ast = astFromString( code, "script" );

				var binExpr = ast.body[ 1 ].right;
				expect( binExpr.type ).toBe( "BinaryExpression" );

				// The left operand should be a simple Identifier
				// Not wrapped in CastExpression with typeAnnotation
				expect( binExpr.left ).notToHaveKey( "typeAnnotation", "Operand should not have typeAnnotation" );
				expect( binExpr.left.type ).toBe( "Identifier" );
			});

		});

	}

}
