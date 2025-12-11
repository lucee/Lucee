component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	variables.testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV5975/";

	function run( testResults, testBox ) {

		describe( "LDEV-5975: Chained method calls lose method name in AST", function() {

			it( "simple method call should include method name in AST", function() {
				var code = fileRead( variables.testDir & "simpleMethodCall.cfm" );
				var ast = astFromString( code, "cfml" );

				// Find the CallExpression
				var callExpr = findNodeByType( ast, "CallExpression" );
				expect( callExpr ).notToBeNull( "CallExpression should be present" );

				// The callee should be a MemberExpression with object and property
				var callee = callExpr.callee;
				expect( callee.type ).toBe( "MemberExpression", "Callee should be a MemberExpression" );
				expect( callee.object.type ).toBe( "Identifier" );
				expect( callee.object.name ).toBe( "OBJ" );
				expect( callee.property.type ).toBe( "Identifier" );
				expect( callee.property.name ).toBe( "MYMETHOD", "Method name should be preserved" );
			});

			it( "method call with arguments should include method name", function() {
				var code = fileRead( variables.testDir & "methodCall.cfm" );
				var ast = astFromString( code, "tag" );

				// Find the CallExpression for ensureCapacity
				var callExpr = findNodeByType( ast, "CallExpression" );
				expect( callExpr ).notToBeNull( "CallExpression should be present" );

				// The callee should be a MemberExpression
				var callee = callExpr.callee;
				expect( callee.type ).toBe( "MemberExpression", "Callee should be a MemberExpression" );

				// Should have method name "ensureCapacity"
				expect( callee.property.name ).toBe( "ENSURECAPACITY", "Method name 'ensureCapacity' should be preserved" );
			});

			it( "chained method calls should preserve all method names", function() {
				var code = fileRead( variables.testDir & "chainedCalls.cfm" );
				var ast = astFromString( code, "cfml" );

				// Find all method names in the AST
				var methodNames = findAllMethodNames( ast );

				// Should contain first, second, third
				expect( methodNames ).toInclude( "FIRST", "Method 'first' should be preserved" );
				expect( methodNames ).toInclude( "SECOND", "Method 'second' should be preserved" );
				expect( methodNames ).toInclude( "THIRD", "Method 'third' should be preserved" );
			});

		});
	}

	/**
	 * Recursively find a node by type in the AST
	 */
	private function findNodeByType( required struct node, required string nodeType ) {
		if ( ( node.type ?: "" ) == arguments.nodeType ) {
			return node;
		}
		for ( var key in node ) {
			var val = node[ key ];
			if ( isStruct( val ) ) {
				var result = findNodeByType( val, arguments.nodeType );
				if ( !isNull( result ) ) return result;
			} else if ( isArray( val ) ) {
				for ( var item in val ) {
					if ( isStruct( item ) ) {
						var result = findNodeByType( item, arguments.nodeType );
						if ( !isNull( result ) ) return result;
					}
				}
			}
		}
		return;
	}

	/**
	 * Find all method names from MemberExpression.property in CallExpressions
	 */
	private function findAllMethodNames( required struct node, array results = [] ) {
		// If this is a CallExpression with MemberExpression callee, grab the method name
		if ( ( node.type ?: "" ) == "CallExpression" && structKeyExists( node, "callee" ) && isStruct( node.callee ) ) {
			if ( ( node.callee.type ?: "" ) == "MemberExpression" && structKeyExists( node.callee, "property" ) ) {
				var prop = node.callee.property;
				if ( isStruct( prop ) && structKeyExists( prop, "name" ) ) {
					arrayAppend( results, prop.name );
				}
			}
		}

		// Recurse
		for ( var key in node ) {
			var val = node[ key ];
			if ( isStruct( val ) ) {
				findAllMethodNames( val, results );
			} else if ( isArray( val ) ) {
				for ( var item in val ) {
					if ( isStruct( item ) ) {
						findAllMethodNames( item, results );
					}
				}
			}
		}
		return results;
	}

}
