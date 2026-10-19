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

		describe( "LDEV-6018: Dynamic struct keys should not be wrapped in CastExpression", function() {

			it( "should preserve MemberExpression in struct key with interpolation", function() {
				var code = 'x = { "##arguments.name##": "value" };';
				var ast = astFromString( code, "script" );

				// Find the struct property key
				var prop = ast.body[ 1 ].right.properties[ 1 ];
				expect( prop.type ).toBe( "Property" );

				// Bug: key is parsed as MemberExpression first time, but after round-trip
				// with "#ARGUMENTS.NAME#" output, it becomes CastExpression
				// The key should remain a MemberExpression (or at minimum, be consistent)
				expect( prop.key.type ).toBe( "MemberExpression", "Struct key should be MemberExpression, not wrapped in CastExpression" );
			});

			it( "should preserve CallExpression in struct key with interpolation", function() {
				var code = 'x = { "##getKey()##": "value" };';
				var ast = astFromString( code, "script" );

				var prop = ast.body[ 1 ].right.properties[ 1 ];
				expect( prop.type ).toBe( "Property" );

				// Bug: key is CallExpression first time, but becomes CastExpression after round-trip
				expect( prop.key.type ).toBe( "CallExpression", "Struct key should be CallExpression, not wrapped in CastExpression" );
			});

		});

		describe( "LDEV-6018: Interpolated expressions should parse consistently", function() {

			it( "should parse simple interpolated function name as Identifier", function() {
				// Simple "#funcName#"() parses callee as Identifier directly
				var code = '"##funcName##"();';
				var ast = astFromString( code, "script" );

				var callExpr = ast.body[ 1 ];
				expect( callExpr.type ).toBe( "CallExpression" );
				expect( callExpr.callee.type ).toBe( "Identifier" );
				expect( callExpr.callee.name ).toBe( "FUNCNAME" );
			});

			it( "should parse complex interpolated new expression as CastExpression", function() {
				// new "#arguments.reporter#"() should have CastExpression callee
				var code = 'new "##arguments.reporter##"();';
				var ast = astFromString( code, "script" );

				var newExpr = ast.body[ 1 ];
				expect( newExpr.type ).toBe( "NewExpression" );

				// The callee is a CastExpression wrapping a MemberExpression
				expect( newExpr.callee.type ).toBe( "CastExpression", "Interpolated new callee should be CastExpression" );
				expect( newExpr.callee.argument.type ).toBe( "MemberExpression" );
			});

			it( "should parse complex interpolated call expression as MemberExpression", function() {
				// "#arguments.reporter#"() (not new) should have MemberExpression callee
				var code = '"##arguments.reporter##"();';
				var ast = astFromString( code, "script" );

				var callExpr = ast.body[ 1 ];
				expect( callExpr.type ).toBe( "CallExpression" );

				// Unlike new expressions, regular calls parse the interpolated expression directly
				expect( callExpr.callee.type ).toBe( "MemberExpression" );
			});

		});

		describe( "LDEV-6018: Unary plus should be UnaryExpression, not CastExpression", function() {

			it( "should parse unary plus as UnaryExpression like unary minus", function() {
				var plusCode = 'x = +num;';
				var minusCode = 'x = -num;';

				var plusAst = astFromString( plusCode, "script" );
				var minusAst = astFromString( minusCode, "script" );

				var plusExpr = plusAst.body[1].right;
				var minusExpr = minusAst.body[1].right;

				// Unary minus correctly uses UnaryExpression
				expect( minusExpr.type ).toBe( "UnaryExpression" );
				expect( minusExpr.operator ).toBe( "NEGATE" );

				// Bug: Unary plus uses CastExpression with typeAnnotation="number"
				// Should be UnaryExpression with operator="PLUS" (or similar)
				expect( plusExpr.type ).toBe( "UnaryExpression",
					"Unary plus should be UnaryExpression, not #plusExpr.type#" );
			});

			it( "should preserve unary plus in function arguments", function() {
				var code = 'DateAdd( "d", +num, now() );';
				var ast = astFromString( code, "script" );

				var arg = ast.body[1].arguments[2];

				// The +num argument should be distinguishable from just num
				// Currently it's CastExpression which loses the + sign
				expect( arg.type ).toBe( "UnaryExpression",
					"Unary plus in argument should be UnaryExpression, not #arg.type#" );
			});

		});

		describe( "LDEV-6018: Integer divide should have distinct operator", function() {

			it( "should distinguish integer divide from regular divide", function() {
				var intDivCode = 'x = a \ b;';
				var divCode = 'x = a / b;';

				var intDivAst = astFromString( intDivCode, "script" );
				var divAst = astFromString( divCode, "script" );

				var intDivExpr = intDivAst.body[1].right;
				var divExpr = divAst.body[1].right;

				expect( intDivExpr.type ).toBe( "BinaryExpression" );
				expect( divExpr.type ).toBe( "BinaryExpression" );

				// Bug: Both have operator "DIVIDE" - should be different
				// Integer divide should be "INTDIV" or "INTEGER_DIVIDE"
				expect( divExpr.operator ).toBe( "DIVIDE" );
				expect( intDivExpr.operator ).toBe( "INTDIV",
					"Integer divide (\\) should have operator 'INTDIV', not '#intDivExpr.operator#'" );
			});

		});

		describe( "LDEV-6018: Safe navigation should have optional flag", function() {

			it( "should distinguish safe navigation from regular member access", function() {
				var safeCode = 'x = obj?.prop;';
				var normalCode = 'x = obj.prop;';

				var safeAst = astFromString( safeCode, "script" );
				var normalAst = astFromString( normalCode, "script" );

				var safeExpr = safeAst.body[1].right;
				var normalExpr = normalAst.body[1].right;

				expect( safeExpr.type ).toBe( "MemberExpression" );
				expect( normalExpr.type ).toBe( "MemberExpression" );

				// Bug: Both look identical - safe navigation should have optional=true
				expect( normalExpr.optional ?: false ).toBe( false );
				expect( safeExpr ).toHaveKey( "optional",
					"Safe navigation should have 'optional' field" );
				expect( safeExpr.optional ).toBe( true,
					"Safe navigation (?.) should have optional=true" );
			});

			it( "should preserve safe navigation in chained access", function() {
				var code = 'x = obj?.nested?.value;';
				var ast = astFromString( code, "script" );

				var expr = ast.body[1].right;

				// Outer member access (?.value)
				expect( expr.type ).toBe( "MemberExpression" );
				expect( expr ).toHaveKey( "optional" );
				expect( expr.optional ).toBe( true );

				// Inner member access (obj?.nested)
				expect( expr.object.type ).toBe( "MemberExpression" );
				expect( expr.object ).toHaveKey( "optional" );
				expect( expr.object.optional ).toBe( true );
			});

			it( "should NOT bleed optional flag to previous member in mixed chain", function() {
				// Bug: a.b.c?.d incorrectly sets optional on BOTH .d AND .c
				// Only .d should have optional=true
				var code = 'x = a.b.c?.d;';
				var ast = astFromString( code, "script" );

				var expr = ast.body[1].right;

				// Structure: MemberExpression(.d) -> MemberExpression(.c) -> MemberExpression(.b) -> Identifier(a)

				// .d - should have optional=true (we used ?.)
				expect( expr.type ).toBe( "MemberExpression" );
				expect( expr.property.name ).toBe( "D" );
				expect( expr ).toHaveKey( "optional" );
				expect( expr.optional ).toBe( true, ".d should have optional=true" );

				// .c - should NOT have optional (we used regular .)
				expect( expr.object.type ).toBe( "MemberExpression" );
				expect( expr.object.property.name ).toBe( "C" );
				expect( expr.object.optional ?: false ).toBe( false,
					".c should NOT have optional=true - the flag is bleeding from ?.d" );

				// .b - should NOT have optional
				expect( expr.object.object.type ).toBe( "MemberExpression" );
				expect( expr.object.object.property.name ).toBe( "B" );
				expect( expr.object.object.optional ?: false ).toBe( false,
					".b should NOT have optional=true" );
			});

			it( "should correctly mark only the safe-navigated member in middle of chain", function() {
				// a.b?.c.d - only .c should have optional
				var code = 'x = a.b?.c.d;';
				var ast = astFromString( code, "script" );

				var expr = ast.body[1].right;

				// .d - should NOT have optional
				expect( expr.optional ?: false ).toBe( false, ".d should NOT have optional" );

				// .c - should have optional=true (we used ?.)
				expect( expr.object ).toHaveKey( "optional" );
				expect( expr.object.optional ).toBe( true, ".c should have optional=true" );

				// .b - should NOT have optional
				expect( expr.object.object.optional ?: false ).toBe( false,
					".b should NOT have optional=true - the flag is bleeding from ?.c" );
			});

		});

		describe( "LDEV-6018: Compound assignment operators should be preserved", function() {

			it( "should preserve modulo compound assignment", function() {
				var code = 'x %= 3;';
				var ast = astFromString( code, "script" );

				var expr = ast.body[1];
				expect( expr.type ).toBe( "AssignmentExpression" );

				// Bug: operator is "ASSIGN" and right is expanded to x % 3
				// Should be operator "MODULUS_ASSIGN" or similar
				expect( expr.operator ).toBe( "MODULUS_ASSIGN",
					"Compound %%== should have operator 'MODULUS_ASSIGN', not '#expr.operator#'" );
			});

			it( "should preserve all compound assignment operators", function() {
				var ops = {
					"x += 1": "PLUS_ASSIGN",
					"x -= 1": "MINUS_ASSIGN",
					"x *= 2": "MULTIPLY_ASSIGN",
					"x /= 2": "DIVIDE_ASSIGN",
					"x %= 2": "MODULUS_ASSIGN",
					"x &= 'a'": "CONCAT_ASSIGN"
				};

				for ( var code in ops ) {
					var expected = ops[ code ];
					var ast = astFromString( code & ";", "script" );
					var expr = ast.body[1];

					expect( expr.type ).toBe( "AssignmentExpression" );
					expect( expr.operator ).toBe( expected,
						"'#code#' should have operator '#expected#', not '#expr.operator#'" );
				}
			});

		});

		describe( "LDEV-6018: Increment/decrement should be UpdateExpression, not AssignmentExpression", function() {

			it( "should parse pre-increment as UpdateExpression", function() {
				var code = 'x = ++i;';
				var ast = astFromString( code, "script" );

				var expr = ast.body[1].right;

				// Bug: ++i is parsed as AssignmentExpression with PLUS_ASSIGN
				// Should be UpdateExpression with prefix=true
				expect( expr.type ).toBe( "UpdateExpression",
					"Pre-increment ++i should be UpdateExpression, not #expr.type#" );
				expect( expr ).toHaveKey( "prefix" );
				expect( expr.prefix ).toBe( true, "Pre-increment should have prefix=true" );
				expect( expr.operator ).toBe( "++" );
			});

			it( "should parse post-increment as UpdateExpression", function() {
				var code = 'x = i++;';
				var ast = astFromString( code, "script" );

				var expr = ast.body[1].right;

				// Bug: i++ is parsed as AssignmentExpression with PLUS_ASSIGN
				// Should be UpdateExpression with prefix=false
				expect( expr.type ).toBe( "UpdateExpression",
					"Post-increment i++ should be UpdateExpression, not #expr.type#" );
				expect( expr ).toHaveKey( "prefix" );
				expect( expr.prefix ).toBe( false, "Post-increment should have prefix=false" );
				expect( expr.operator ).toBe( "++" );
			});

			it( "should parse pre-decrement as UpdateExpression", function() {
				var code = 'x = --i;';
				var ast = astFromString( code, "script" );

				var expr = ast.body[1].right;

				expect( expr.type ).toBe( "UpdateExpression",
					"Pre-decrement --i should be UpdateExpression, not #expr.type#" );
				expect( expr ).toHaveKey( "prefix" );
				expect( expr.prefix ).toBe( true );
				expect( expr.operator ).toBe( "--" );
			});

			it( "should parse post-decrement as UpdateExpression", function() {
				var code = 'x = i--;';
				var ast = astFromString( code, "script" );

				var expr = ast.body[1].right;

				expect( expr.type ).toBe( "UpdateExpression",
					"Post-decrement i-- should be UpdateExpression, not #expr.type#" );
				expect( expr ).toHaveKey( "prefix" );
				expect( expr.prefix ).toBe( false );
				expect( expr.operator ).toBe( "--" );
			});

			it( "should allow increment as inline expression", function() {
				// This is the key issue - ++i must be usable as an expression
				var code = 'x = int( 100 / count * ++i );';
				var ast = astFromString( code, "script" );

				var callExpr = ast.body[1].right;
				expect( callExpr.type ).toBe( "CallExpression" );

				// Find the ++i in the multiply expression
				var multiplyExpr = callExpr.arguments[1];
				expect( multiplyExpr.type ).toBe( "BinaryExpression" );
				expect( multiplyExpr.operator ).toBe( "MULTIPLY" );

				var incExpr = multiplyExpr.right;
				// Bug: This is AssignmentExpression which can't be used inline
				expect( incExpr.type ).toBe( "UpdateExpression",
					"Inline ++i should be UpdateExpression for proper round-trip" );
			});

		});

	}

}
