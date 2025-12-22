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

			it( "should distinguish colon separator from equals separator in struct properties", function() {
				var colonCode = 'x = { a : 1 };';
				var equalsCode = 'x = { a = 1 };';

				var colonAst = astFromString( colonCode, "script" );
				var equalsAst = astFromString( equalsCode, "script" );

				var colonProp = colonAst.body[1].right.properties[1];
				var equalsProp = equalsAst.body[1].right.properties[1];

				// Both should be Property type
				expect( colonProp.type ).toBe( "Property" );
				expect( equalsProp.type ).toBe( "Property" );

				// Should have a separator field to distinguish them
				expect( colonProp ).toHaveKey( "separator",
					"Property should have 'separator' field to indicate colon vs equals" );
				expect( equalsProp ).toHaveKey( "separator",
					"Property should have 'separator' field to indicate colon vs equals" );

				// Colon syntax should have separator=":"
				expect( colonProp.separator ).toBe( ":",
					"Colon syntax { a : 1 } should have separator=':' but got '#colonProp.separator ?: 'null'#'" );

				// Equals syntax should have separator="="
				expect( equalsProp.separator ).toBe( "=",
					"Equals syntax { a = 1 } should have separator='=' but got '#equalsProp.separator ?: 'null'#'" );
			});

			it( "should preserve mixed separators in same struct", function() {
				// CFML allows mixing colon and equals in the same struct literal
				var code = 'x = { a : 1, b = 2, c : 3 };';
				var ast = astFromString( code, "script" );

				var props = ast.body[1].right.properties;
				expect( props ).toHaveLength( 3 );

				// First property uses colon
				expect( props[1] ).toHaveKey( "separator" );
				expect( props[1].separator ).toBe( ":",
					"First property 'a : 1' should have separator=':' but got '#props[1].separator ?: 'null'#'" );

				// Second property uses equals
				expect( props[2] ).toHaveKey( "separator" );
				expect( props[2].separator ).toBe( "=",
					"Second property 'b = 2' should have separator='=' but got '#props[2].separator ?: 'null'#'" );

				// Third property uses colon
				expect( props[3] ).toHaveKey( "separator" );
				expect( props[3].separator ).toBe( ":",
					"Third property 'c : 3' should have separator=':' but got '#props[3].separator ?: 'null'#'" );
			});

			it( "should distinguish colon separator from equals separator in named function arguments", function() {
				var colonCode = 'myFunc( zac: 1, micha: 2 );';
				var equalsCode = 'myFunc( zac=1, micha=2 );';

				var colonAst = astFromString( colonCode, "script" );
				var equalsAst = astFromString( equalsCode, "script" );

				var colonArgs = colonAst.body[1].arguments;
				var equalsArgs = equalsAst.body[1].arguments;

				// Both should be NamedArgument type
				expect( colonArgs[1].type ).toBe( "NamedArgument" );
				expect( equalsArgs[1].type ).toBe( "NamedArgument" );

				// Should have a separator field to distinguish them
				expect( colonArgs[1] ).toHaveKey( "separator",
					"NamedArgument should have 'separator' field to indicate colon vs equals" );
				expect( equalsArgs[1] ).toHaveKey( "separator",
					"NamedArgument should have 'separator' field to indicate colon vs equals" );

				// Colon syntax should have separator=":"
				expect( colonArgs[1].separator ).toBe( ":",
					"Colon syntax myFunc( zac: 1 ) should have separator=':' but got '#colonArgs[1].separator ?: 'null'#'" );

				// Equals syntax should have separator="="
				expect( equalsArgs[1].separator ).toBe( "=",
					"Equals syntax myFunc( zac=1 ) should have separator='=' but got '#equalsArgs[1].separator ?: 'null'#'" );
			});

			it( "should preserve mixed separators in same function call", function() {
				// CFML allows mixing colon and equals in the same function call
				var code = 'myFunc( a: 1, b=2, c: 3 );';
				var ast = astFromString( code, "script" );

				var args = ast.body[1].arguments;
				expect( args ).toHaveLength( 3 );

				// First arg uses colon
				expect( args[1] ).toHaveKey( "separator" );
				expect( args[1].separator ).toBe( ":",
					"First arg 'a: 1' should have separator=':' but got '#args[1].separator ?: 'null'#'" );

				// Second arg uses equals
				expect( args[2] ).toHaveKey( "separator" );
				expect( args[2].separator ).toBe( "=",
					"Second arg 'b=2' should have separator='=' but got '#args[2].separator ?: 'null'#'" );

				// Third arg uses colon
				expect( args[3] ).toHaveKey( "separator" );
				expect( args[3].separator ).toBe( ":",
					"Third arg 'c: 3' should have separator=':' but got '#args[3].separator ?: 'null'#'" );
			});

			it( "should represent Class::method() as static MemberExpression not _getstaticscope", function() {
				var code = 'x = MyClass::staticMethod( arg );';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				var right = ast.body[1].right;
				expect( right.type ).toBe( "CallExpression" );

				// The callee should be a MemberExpression with static=true
				var callee = right.callee;
				expect( callee.type ).toBe( "MemberExpression",
					"Expected MemberExpression but got #callee.type#" );
				expect( callee ).toHaveKey( "static",
					"MemberExpression should have 'static' field for :: syntax" );
				expect( callee.static ).toBe( true,
					"Expected static=true for :: syntax but got #callee.static ?: 'null'#" );

				// The object should be the class name, not _getstaticscope call
				expect( callee.object.type ).toBe( "Identifier",
					"Expected object to be Identifier but got #callee.object.type#" &
					( callee.object.type == "CallExpression" ? " with callee #callee.object.callee.name ?: 'unknown'#" : "" ) );
				expect( callee.object.name ).toBe( "MyClass",
					"Expected object name 'MyClass' but got '#callee.object.name ?: 'null'#'" );

				// The property should be the method name
				expect( callee.property.name ).toBe( "staticMethod",
					"Expected property name 'staticMethod' but got '#callee.property.name ?: 'null'#'" );
			});

			it( "should represent super::method() as static MemberExpression not _getsuperstaticscope", function() {
				var code = 'x = super::parentMethod();';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				var right = ast.body[1].right;
				expect( right.type ).toBe( "CallExpression" );

				// The callee should be a MemberExpression with static=true
				var callee = right.callee;
				expect( callee.type ).toBe( "MemberExpression" );
				expect( callee ).toHaveKey( "static" );
				expect( callee.static ).toBe( true );

				// The object should be "super" identifier, not _getsuperstaticscope call
				expect( callee.object.type ).toBe( "Identifier",
					"Expected object to be Identifier but got #callee.object.type#" &
					( callee.object.type == "CallExpression" ? " with callee #callee.object.callee.name ?: 'unknown'#" : "" ) );
				expect( callee.object.name ).toBe( "super",
					"Expected object name 'super' but got '#callee.object.name ?: 'null'#'" );

				// The property should be the method name
				expect( callee.property.name ).toBe( "parentMethod" );
			});

			it( "should represent Class::CONSTANT as static MemberExpression", function() {
				var code = 'x = MyClass::CONSTANT;';
				var ast = astFromString( code, "script" );

				expect( ast.body ).toHaveLength( 1 );
				var right = ast.body[1].right;

				// Direct static property access (not a CallExpression)
				expect( right.type ).toBe( "MemberExpression",
					"Expected MemberExpression but got #right.type#" );
				expect( right ).toHaveKey( "static" );
				expect( right.static ).toBe( true );

				// The object should be the class name
				expect( right.object.type ).toBe( "Identifier",
					"Expected object to be Identifier but got #right.object.type#" &
					( right.object.type == "CallExpression" ? " with callee #right.object.callee.name ?: 'unknown'#" : "" ) );
				expect( right.object.name ).toBe( "MyClass" );

				// The property should be the constant name
				expect( right.property.name ).toBe( "CONSTANT" );
			});

		});

	}

}
