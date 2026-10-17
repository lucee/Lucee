component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	function run( testResults, testBox ) {

		describe( "LDEV-6034: AST should not expose auto-generated closure/lambda names", function() {

			it( "should not include internal name for anonymous closure", function() {
				var ast = astFromString( 'arr.each( function( item ) { return item; } );', "script" );
				// body[1] is CallExpression directly (no ExpressionStatement wrapper)
				var callExpr = ast.body[1];
				expect( callExpr.type ).toBe( "CallExpression" );
				var closureArg = callExpr.arguments[1];
				expect( closureArg.type ).toBe( "ClosureDeclaration" );
				// Anonymous closure should NOT have a name field - it's internal implementation detail
				expect( closureArg ).notToHaveKey( "name", "Anonymous closure should not expose auto-generated internal name" );
			});

			it( "should not include internal name for lambda expression", function() {
				var ast = astFromString( 'x = () => 1;', "script" );
				var assignment = ast.body[1];
				expect( assignment.type ).toBe( "AssignmentExpression" );
				var lambda = assignment.right;
				expect( lambda.type ).toBe( "LambdaDeclaration" );
				// Lambda should NOT have a name field - it's internal implementation detail
				expect( lambda ).notToHaveKey( "name", "Lambda should not expose auto-generated internal name" );
			});

			it( "should not include internal name for inline lambda in array method", function() {
				var ast = astFromString( '[1,2,3].map( ( x ) => x * 2 );', "script" );
				// body[1] is CallExpression directly
				var callExpr = ast.body[1];
				expect( callExpr.type ).toBe( "CallExpression" );
				var lambdaArg = callExpr.arguments[1];
				expect( lambdaArg.type ).toBe( "LambdaDeclaration" );
				// Lambda should NOT have a name field
				expect( lambdaArg ).notToHaveKey( "name", "Inline lambda should not expose auto-generated internal name" );
			});

		});
	}

}
