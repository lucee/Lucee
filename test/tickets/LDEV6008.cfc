component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	function run( testResults, testBox ) {

		describe( "LDEV-6008: PreserveSingleQuotes evaluated in cfquery AST", function() {

			it( "should preserve PreserveSingleQuotes function call in cfquery body", function() {
				// Use a literal string argument so it doesn't try to resolve a variable
				var code = '<cfquery name="q">##PreserveSingleQuotes( "test" )##</cfquery>';
				var ast = astFromString( code );

				expect( ast.body ).toHaveLength( 1 );
				expect( ast.body[1].type ).toBe( "CFMLTag" );
				expect( ast.body[1].name ).toBe( "query" );

				// The body should contain an ExpressionStatement (interpolation)
				var body = ast.body[1].body;
				expect( body.body ).toBeArray();
				expect( body.body ).toHaveLength( 1, "Should have one expression" );

				var stmt = body.body[1];
				expect( stmt.type ).toBe( "ExpressionStatement" );

				var expr = stmt.expression;
				// Expression is wrapped in CastExpression (string cast), CallExpression is inside argument
				expect( expr.type ).toBe( "CastExpression" );
				expect( expr.argument.type ).toBe( "CallExpression",
					"Expected CallExpression inside CastExpression but got #expr.argument.type# - PreserveSingleQuotes was evaluated during AST generation" );
				expect( expr.argument.callee.name ).toBe( "preservesinglequotes",
					"Function name should be preservesinglequotes" );
			});

			it( "should not evaluate expressions during AST generation", function() {
				// This demonstrates that PreserveSingleQuotes is being EXECUTED during parsing
				// Even though we're just generating an AST, Lucee evaluates the function
				var code = '<cfquery name="q">##PreserveSingleQuotes( "original" )##</cfquery>';
				var ast = astFromString( code );

				var body = ast.body[1].body;
				var stmt = body.body[1];
				var expr = stmt.expression;

				// If the bug is present, expr will be StringLiteral with value="original"
				// If fixed, expr should be CastExpression with argument.type="CallExpression"
				if ( expr.type == "StringLiteral" ) {
					fail( "BUG: PreserveSingleQuotes was evaluated during AST generation. " &
						"Expression reduced to StringLiteral '#expr.value#' instead of CallExpression" );
				}

				expect( expr.type ).toBe( "CastExpression" );
				expect( expr.argument.type ).toBe( "CallExpression" );
				expect( expr.argument.callee.name ).toBe( "preservesinglequotes" );
			});

			it( "preserves nested PreserveSingleQuotes when wrapped in another function", function() {
				// When PreserveSingleQuotes is wrapped in trim(), it's preserved
				// This test verifies that nested calls work
				var code = '<cfquery name="q">##trim( PreserveSingleQuotes( "test" ) )##</cfquery>';
				var ast = astFromString( code );

				var body = ast.body[1].body;
				var stmt = body.body[1];
				var expr = stmt.expression;

				// Expression is wrapped in CastExpression
				expect( expr.type ).toBe( "CastExpression" );

				// The outer trim() should be there as CallExpression inside the cast
				var trimCall = expr.argument;
				expect( trimCall.type ).toBe( "CallExpression" );
				expect( trimCall.callee.name ).toBe( "trim" );

				// The argument to trim() should be CallExpression (PreserveSingleQuotes)
				var innerArg = trimCall.arguments[1];
				expect( innerArg.type ).toBe( "CallExpression",
					"Inner PreserveSingleQuotes should be CallExpression, got #innerArg.type#" );
				expect( innerArg.callee.name ).toBe( "preservesinglequotes" );
			});

		});

	}

}
