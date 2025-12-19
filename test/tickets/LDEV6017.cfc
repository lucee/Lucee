component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	function run( testResults, testBox ) {

		describe( "LDEV-6017: Interpolated struct keys should be TemplateLiteral not BinaryExpression", function() {

			it( "should represent interpolated struct key as TemplateLiteral", function() {
				var code = 'x = { "foo.##bar##.baz": "value" };';
				var ast = astFromString( code, "script" );

				// Find the ObjectExpression
				var objExpr = ast.body[ 1 ].right;
				expect( objExpr.type ).toBe( "ObjectExpression" );

				// Get the first property's key
				var key = objExpr.properties[ 1 ].key;

				// Interpolated strings should be TemplateLiteral (like JS template literals)
				// with quasis (string parts) and expressions (interpolated values)
				expect( key.type ).toBe( "TemplateLiteral", "Interpolated struct key should be TemplateLiteral, not BinaryExpression" );

				// Should have quasis and expressions arrays
				expect( key ).toHaveKey( "quasis" );
				expect( key ).toHaveKey( "expressions" );

				// quasis should contain the string parts: "foo.", ".baz"
				expect( arrayLen( key.quasis ) ).toBe( 2 );
				expect( key.quasis[ 1 ].value ).toBe( "foo." );
				expect( key.quasis[ 2 ].value ).toBe( ".baz" );

				// expressions should contain the interpolated identifier: bar
				expect( arrayLen( key.expressions ) ).toBe( 1 );
				expect( key.expressions[ 1 ].type ).toBe( "Identifier" );
				expect( key.expressions[ 1 ].name ).toBe( "BAR" );
			});

			it( "should allow round-trip of struct with interpolated keys", function() {
				var code = 'x = { "prefix.##name##.suffix": "value" };';
				var ast = astFromString( code, "script" );

				// The key should be representable in a way that can round-trip
				var key = ast.body[ 1 ].right.properties[ 1 ].key;

				// Should be TemplateLiteral, not BinaryExpression
				expect( key.type ).toBe( "TemplateLiteral", "Key should be TemplateLiteral for round-trip support" );
				expect( key.type ).notToBe( "BinaryExpression", "Key should not be broken into concatenation parts" );
			});

			it( "should handle simple interpolation with no prefix/suffix", function() {
				var code = 'x = { "##name##": "value" };';
				var ast = astFromString( code, "script" );

				var key = ast.body[ 1 ].right.properties[ 1 ].key;

				// A simple "#name#" with no string parts becomes just the expression
				// This is expected - it's effectively just the variable
				// The key point is it's NOT a BinaryExpression CONCAT
				expect( key.type ).notToBe( "BinaryExpression", "Should not be broken into concatenation parts" );
				// It should be an Identifier (the interpolated variable)
				expect( key.type ).toBe( "Identifier" );
				expect( key.name ).toBe( "NAME" );
			});

		});

	}

}
