component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	function run( testResults, testBox ) {
		describe( "LDEV-6022: String literal concatenation should not be merged in AST", function() {

			it( "preserves three-part string concatenation", function() {
				var code = 'x = "<c" & "fsc" & "ript>";';
				var ast = astFromString( code, "script" );

				// The assignment expression
				var assign = ast.body[1];
				expect( assign.type ).toBe( "AssignmentExpression" );

				// Right side should be a BinaryExpression
				var concat1 = assign.right;
				expect( concat1.type ).toBe( "BinaryExpression" );
				expect( concat1.operator ).toBe( "CONCAT" );

				// Left side of outer concat should also be a BinaryExpression (the first two parts)
				var concat2 = concat1.left;
				expect( concat2.type ).toBe( "BinaryExpression" );
				expect( concat2.operator ).toBe( "CONCAT" );

				// First string literal should be "<c"
				expect( concat2.left.type ).toBe( "StringLiteral" );
				expect( concat2.left.value ).toBe( "<c" );

				// Second string literal should be "fsc"
				expect( concat2.right.type ).toBe( "StringLiteral" );
				expect( concat2.right.value ).toBe( "fsc" );

				// Third string literal should be "ript>"
				expect( concat1.right.type ).toBe( "StringLiteral" );
				expect( concat1.right.value ).toBe( "ript>" );
			});

			it( "preserves two-part string concatenation with cf tag pattern", function() {
				var code = 'x = "<c" & "fcomponent>";';
				var ast = astFromString( code, "script" );

				var assign = ast.body[1];
				var concat = assign.right;

				expect( concat.type ).toBe( "BinaryExpression" );
				expect( concat.left.value ).toBe( "<c" );
				expect( concat.right.value ).toBe( "fcomponent>" );
			});

		});
	}

}
