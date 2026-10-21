component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	function run( testResults, testBox ) {
		describe( "LDEV-6024: Computed member expression inside scope access", function() {

			it( "should preserve nested computed member expression in AST", function() {
				var code = 'return variables[local.functionName[1]]();';
				var ast = astFromString( code, "script" );

				// The return statement
				var returnStmt = ast.body[1];
				expect( returnStmt.type ).toBe( "ReturnStatement" );

				// The call expression: variables[...]()`
				var callExpr = returnStmt.argument;
				expect( callExpr.type ).toBe( "CallExpression" );

				// The callee is a MemberExpression: variables[local.functionName[1]]
				var callee = callExpr.callee;
				expect( callee.type ).toBe( "MemberExpression" );
				expect( callee.computed ).toBe( true );

				// Object should be VARIABLES identifier
				expect( callee.object.type ).toBe( "Identifier" );
				expect( callee.object.name ).toBe( "VARIABLES" );

				// Property is wrapped in CastExpression (cast to string for bracket access)
				var prop = callee.property;
				expect( prop.type ).toBe( "CastExpression" );
				expect( prop.typeAnnotation ).toBe( "string" );

				// The argument inside CastExpression should be the MemberExpression
				var innerExpr = prop.argument;
				expect( innerExpr.type ).toBe( "MemberExpression" );
				expect( innerExpr.computed ).toBe( true );

				// The inner expression is local.functionName[1]
				expect( innerExpr.object.type ).toBe( "MemberExpression" );
				expect( innerExpr.object.property.name ).toBe( "FUNCTIONNAME" );
			});

			it( "should handle simple computed scope access", function() {
				var code = 'return variables[key];';
				var ast = astFromString( code, "script" );

				var returnStmt = ast.body[1];
				var memberExpr = returnStmt.argument;

				expect( memberExpr.type ).toBe( "MemberExpression" );
				expect( memberExpr.computed ).toBe( true );
				expect( memberExpr.object.name ).toBe( "VARIABLES" );

				// Property is wrapped in CastExpression (cast to string for bracket access)
				expect( memberExpr.property.type ).toBe( "CastExpression" );
				expect( memberExpr.property.argument.type ).toBe( "Identifier" );
				expect( memberExpr.property.argument.name ).toBe( "KEY" );
			});

		});
	}

}
