component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	function run( testResults, testBox ) {

		describe( "LDEV-6011: AST should use proper node types for literals, not internal functions", function() {

			it( "should represent empty struct literal as ObjectExpression not _literalstruct", function() {
				var code = 'x = {};';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				var assignment = ast.body[1];
				expect( assignment.type ).toBe( "AssignmentExpression" );

				// The right side should be ObjectExpression, not a CallExpression to _literalstruct
				var right = assignment.right;
				expect( right.type ).toBe( "ObjectExpression",
					"Expected ObjectExpression but got #right.type#" &
					( right.type == "CallExpression" ? " with callee #right.callee.name ?: 'unknown'#" : "" ) );
			});

			it( "should represent empty array literal as ArrayExpression not _literalarray", function() {
				var code = 'x = [];';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				var assignment = ast.body[1];
				expect( assignment.type ).toBe( "AssignmentExpression" );

				// The right side should be ArrayExpression, not a CallExpression to _literalarray
				var right = assignment.right;
				expect( right.type ).toBe( "ArrayExpression",
					"Expected ArrayExpression but got #right.type#" &
					( right.type == "CallExpression" ? " with callee #right.callee.name ?: 'unknown'#" : "" ) );
			});

			it( "should represent struct literal with values as ObjectExpression", function() {
				var code = 'x = { foo: "bar", num: 123 };';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				var right = ast.body[1].right;
				expect( right.type ).toBe( "ObjectExpression",
					"Expected ObjectExpression but got #right.type#" );
			});

			it( "should represent array literal with values as ArrayExpression", function() {
				var code = 'x = [ 1, 2, 3 ];';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				var right = ast.body[1].right;
				expect( right.type ).toBe( "ArrayExpression",
					"Expected ArrayExpression but got #right.type#" );
			});

			it( "should represent new Component() as NewExpression not _createcomponent", function() {
				var code = 'x = new MyComponent();';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				var right = ast.body[1].right;
				expect( right.type ).toBe( "NewExpression",
					"Expected NewExpression but got #right.type#" &
					( right.type == "CallExpression" ? " with callee #right.callee.name ?: 'unknown'#" : "" ) );
			});

			it( "should represent ordered struct literal as ObjectExpression with ordered flag", function() {
				var code = 'x = [ foo=1, bar=2 ];';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				var right = ast.body[1].right;
				expect( right.type ).toBe( "ObjectExpression",
					"Expected ObjectExpression but got #right.type#" &
					( right.type == "CallExpression" ? " with callee #right.callee.name ?: 'unknown'#" : "" ) );
				expect( right.ordered ).toBe( true, "Expected ordered=true for ordered struct literal" );
			});

			it( "should preserve all properties in struct with 3+ keys", function() {
				var code = 'x = { a: 1, b: 2, c: 3 };';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				var right = ast.body[1].right;
				expect( right.type ).toBe( "ObjectExpression" );
				expect( right.properties ).toHaveLength( 3,
					"Expected 3 properties but got #arrayLen( right.properties )#" );
			});

			it( "should preserve component name in NewExpression with positional args", function() {
				var code = 'x = new MyComponent( arg1 );';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				var right = ast.body[1].right;
				expect( right.type ).toBe( "NewExpression" );
				// Callee should be the component name, not the first argument
				expect( right.callee.type ).toBe( "StringLiteral",
					"Expected callee to be StringLiteral but got #right.callee.type#" );
				expect( right.callee.value ).toBe( "MyComponent",
					"Expected callee value 'MyComponent' but got '#right.callee.value ?: 'null'#'" );
				// Arguments should contain the positional arg
				expect( right.arguments ).toHaveLength( 1,
					"Expected 1 argument but got #arrayLen( right.arguments )#" );
			});

			it( "should preserve component name in NewExpression with named args", function() {
				var code = 'x = new MyComponent( name="test" );';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				var right = ast.body[1].right;
				expect( right.type ).toBe( "NewExpression" );
				// Callee should be the component name, not a NamedArgument
				expect( right.callee.type ).toBe( "StringLiteral",
					"Expected callee to be StringLiteral but got #right.callee.type#" );
				expect( right.callee.value ).toBe( "MyComponent",
					"Expected callee value 'MyComponent' but got '#right.callee.value ?: 'null'#'" );
				// Arguments should contain the named arg
				expect( right.arguments ).toHaveLength( 1,
					"Expected 1 argument but got #arrayLen( right.arguments )#" );
			});

			it( "should not include phantom type:undefined argument in NewExpression", function() {
				var code = 'x = new MyComponent();';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				var right = ast.body[1].right;
				expect( right.type ).toBe( "NewExpression" );
				// No constructor args means empty arguments array
				expect( right.arguments ).toHaveLength( 0,
					"Expected 0 arguments but got #arrayLen( right.arguments )#" &
					( arrayLen( right.arguments ) > 0 ? " - first arg: #right.arguments[1].value ?: right.arguments[1].type#" : "" ) );
			});

		});

	}

}
