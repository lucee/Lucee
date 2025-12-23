component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	function run( testResults, testBox ) {

		describe( "LDEV-6041: AST should include original source fidelity", function() {

			describe( "2a. Identifier case preservation", function() {

				it( "should have raw property on Identifier with original casing", function() {
					var ast = astFromString( 'myVar = someFunc( argName=value );', "script" );
					var assignment = ast.body[1];

					// Left side - myVar identifier
					var leftId = assignment.left;
					expect( leftId.type ).toBe( "Identifier" );
					expect( leftId.name ).toBe( "MYVAR", "name should be uppercased" );
					expect( leftId ).toHaveKey( "raw", "Identifier should have raw property with original casing" );
					expect( leftId.raw ).toBeWithCase( "myVar", "raw should preserve original case" );
				});

				it( "should preserve case for UDF callee names", function() {
					var ast = astFromString( 'someFunc();', "script" );
					var call = ast.body[1];

					expect( call.type ).toBe( "CallExpression" );
					var callee = call.callee;
					expect( callee.type ).toBe( "Identifier" );
					expect( callee.name ).toBe( "SOMEFUNC", "name should be uppercased" );
					expect( callee ).toHaveKey( "raw", "UDF callee should have raw property" );
					expect( callee.raw ).toBeWithCase( "someFunc", "raw should preserve original case" );
				});

				it( "should preserve case for named argument names", function() {
					var ast = astFromString( 'test( argName=1 );', "script" );
					var call = ast.body[1];
					var namedArg = call.arguments[1];

					expect( namedArg.type ).toBe( "NamedArgument" );
					var nameId = namedArg.name;
					expect( nameId.type ).toBe( "Identifier" );
					expect( nameId.name ).toBe( "ARGNAME", "name should be uppercased" );
					expect( nameId ).toHaveKey( "raw", "named argument identifier should have raw property" );
					expect( nameId.raw ).toBeWithCase( "argName", "raw should preserve original case" );
				});

				it( "should preserve case for BIF callee names", function() {
					var ast = astFromString( 'arrayAppend( arr, val );', "script" );
					var call = ast.body[1];

					expect( call.type ).toBe( "CallExpression" );
					expect( call.isBuiltIn ).toBeTrue( "should be marked as BIF" );
					var callee = call.callee;
					expect( callee.type ).toBe( "Identifier" );
					expect( callee ).toHaveKey( "raw", "BIF callee should have raw property" );
					expect( callee.raw ).toBeWithCase( "arrayAppend", "raw should preserve original case" );
				});

				// TODO: Scope identifiers (request, session, etc) are resolved to scope integers during parsing
			// and the original identifier casing is discarded. Would require parser changes to preserve.
			xit( "should preserve case for scope prefixes", function() {
					var ast = astFromString( 'request.myKey = 1;', "script" );
					var assignment = ast.body[1];
					var memberExpr = assignment.left;

					expect( memberExpr.type ).toBe( "MemberExpression" );
					var scopeId = memberExpr.object;
					expect( scopeId.type ).toBe( "Identifier" );
					expect( scopeId.name ).toBe( "REQUEST", "scope name should be uppercased" );
					expect( scopeId ).toHaveKey( "raw", "scope identifier should have raw property" );
					expect( scopeId.raw ).toBeWithCase( "request", "raw should preserve original lowercase" );
				});

				// TODO: Same as above - scope identifier casing not preserved
				xit( "should preserve case for uppercase scope", function() {
					var ast = astFromString( 'REQUEST.myKey = 1;', "script" );
					var assignment = ast.body[1];
					var memberExpr = assignment.left;
					var scopeId = memberExpr.object;

					expect( scopeId.name ).toBe( "REQUEST" );
					expect( scopeId ).toHaveKey( "raw", "scope identifier should have raw property" );
					expect( scopeId.raw ).toBeWithCase( "REQUEST", "raw should preserve original uppercase" );
				});

			});

			describe( "2b. var keyword preservation", function() {

				it( "should have declaration property when var keyword used", function() {
					var ast = astFromString( 'function test() { var x = 1; }', "script" );
					var func = ast.body[1];
					var assignment = func.body.body[1];

					expect( assignment.type ).toBe( "AssignmentExpression" );
					expect( assignment ).toHaveKey( "declaration", "var declaration should have declaration property" );
					expect( assignment.declaration ).toBe( "var", "declaration should be 'var'" );
				});

				it( "should NOT have declaration property for explicit local scope", function() {
					var ast = astFromString( 'function test() { local.x = 1; }', "script" );
					var func = ast.body[1];
					var assignment = func.body.body[1];

					expect( assignment.type ).toBe( "AssignmentExpression" );
					expect( assignment ).notToHaveKey( "declaration", "explicit local.x should not have declaration property" );
				});

				it( "should distinguish var x from local.x", function() {
					var ast1 = astFromString( 'function test() { var x = 1; }', "script" );
					var ast2 = astFromString( 'function test() { local.x = 1; }', "script" );

					var assignment1 = ast1.body[1].body.body[1];
					var assignment2 = ast2.body[1].body.body[1];

					// Both produce MemberExpression with LOCAL.X, but var version should have declaration
					expect( assignment1.left.type ).toBe( "MemberExpression" );
					expect( assignment2.left.type ).toBe( "MemberExpression" );

					var hasDecl1 = structKeyExists( assignment1, "declaration" ) && assignment1.declaration == "var";
					var hasDecl2 = structKeyExists( assignment2, "declaration" ) && assignment2.declaration == "var";

					expect( hasDecl1 ).toBeTrue( "var x should have declaration='var'" );
					expect( hasDecl2 ).toBeFalse( "local.x should NOT have declaration property" );
				});

				it( "should preserve var keyword in for loop initializer", function() {
					var ast = astFromString( 'for( var i = 1; i <= 10; i++ ) {}', "script" );
					var forStmt = ast.body[1];
					var init = forStmt.init;

					expect( init.type ).toBe( "AssignmentExpression" );
					expect( init ).toHaveKey( "declaration", "var in for loop should have declaration property" );
					expect( init.declaration ).toBe( "var" );
				});

				it( "should preserve var keyword in for-in loop", function() {
					var ast = astFromString( 'for( var item in arr ) {}', "script" );
					var forStmt = ast.body[1];

					expect( forStmt.type ).toBe( "ForOfStatement" );
					expect( forStmt ).toHaveKey( "declaration", "var in for-in should have declaration property" );
					expect( forStmt.declaration ).toBe( "var" );
				});

			});

			describe( "2c. ScopeIdentifier node type", function() {

				it( "should use ScopeIdentifier for scope references", function() {
					var ast = astFromString( 'request.myKey = 1;', "script" );
					var assignment = ast.body[1];
					var memberExpr = assignment.left;

					expect( memberExpr.type ).toBe( "MemberExpression" );
					var scopeId = memberExpr.object;
					expect( scopeId.type ).toBe( "ScopeIdentifier", "scope reference should be ScopeIdentifier not Identifier" );
					expect( scopeId.name ).toBe( "REQUEST" );
					expect( scopeId ).notToHaveKey( "raw", "ScopeIdentifier should not have raw (synthetic node)" );
				});

				it( "should use ScopeIdentifier for LOCAL scope from var keyword", function() {
					var ast = astFromString( 'function test() { var x = 1; }', "script" );
					var func = ast.body[1];
					var assignment = func.body.body[1];
					var memberExpr = assignment.left;

					expect( memberExpr.type ).toBe( "MemberExpression" );
					var scopeId = memberExpr.object;
					expect( scopeId.type ).toBe( "ScopeIdentifier", "LOCAL from var should be ScopeIdentifier" );
					expect( scopeId.name ).toBe( "LOCAL" );
				});

				it( "should use ScopeIdentifier for explicit local scope", function() {
					var ast = astFromString( 'function test() { local.x = 1; }', "script" );
					var func = ast.body[1];
					var assignment = func.body.body[1];
					var memberExpr = assignment.left;

					expect( memberExpr.type ).toBe( "MemberExpression" );
					var scopeId = memberExpr.object;
					expect( scopeId.type ).toBe( "ScopeIdentifier", "explicit local should be ScopeIdentifier" );
					expect( scopeId.name ).toBe( "LOCAL" );
				});

				it( "should use ScopeIdentifier for various scopes", function() {
					var scopes = [ "session", "application", "url", "form", "cgi", "cookie", "server", "variables", "arguments" ];
					for ( var scopeName in scopes ) {
						var ast = astFromString( '#scopeName#.key = 1;', "script" );
						var assignment = ast.body[1];
						var memberExpr = assignment.left;
						var scopeId = memberExpr.object;

						expect( scopeId.type ).toBe( "ScopeIdentifier", "#scopeName# should be ScopeIdentifier" );
						expect( scopeId.name ).toBe( uCase( scopeName ), "#scopeName# should be uppercased" );
					}
				});

			});

			describe( "2d. Implicit type annotations", function() {

				it( "should distinguish implicit any return type from explicit", function() {
					var ast1 = astFromString( 'function test() {}', "script" );
					var ast2 = astFromString( 'any function test() {}', "script" );

					var func1 = ast1.body[1];
					var func2 = ast2.body[1];

					// Both have returnType.value = "any", but only explicit should have it
					var hasExplicit1 = structKeyExists( func1.returnType, "explicit" ) && func1.returnType.explicit;
					var hasExplicit2 = structKeyExists( func2.returnType, "explicit" ) && func2.returnType.explicit;

					expect( hasExplicit1 ).toBeFalse( "implicit any should not have explicit=true" );
					expect( hasExplicit2 ).toBeTrue( "explicit any should have explicit=true" );
				});

				it( "should distinguish implicit any param type from explicit", function() {
					var ast1 = astFromString( 'function test( arg ) {}', "script" );
					var ast2 = astFromString( 'function test( any arg ) {}', "script" );

					var param1 = ast1.body[1].params[1];
					var param2 = ast2.body[1].params[1];

					// Both have type.value = "any", but only explicit should have it
					var hasExplicit1 = structKeyExists( param1.type, "explicit" ) && param1.type.explicit;
					var hasExplicit2 = structKeyExists( param2.type, "explicit" ) && param2.type.explicit;

					expect( hasExplicit1 ).toBeFalse( "implicit any param should not have explicit=true" );
					expect( hasExplicit2 ).toBeTrue( "explicit any param should have explicit=true" );
				});

			});

		});

	}

}
