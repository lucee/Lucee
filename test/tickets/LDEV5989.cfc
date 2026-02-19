component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	variables.testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV5989/";

	function run( testResults, testBox ) {

		describe( "LDEV-5989: Interpolated attribute values should be parsed as expressions", function() {

			it( "interpolated condition attribute should be parsed as expression not StringLiteral", function() {
				var code = fileRead( variables.testDir & "interpolatedAttr.cfm" );
				var ast = astFromString( code );

				// Find the condition attribute
				var attr = findAttribute( ast, "condition" );
				expect( attr ).notToBeNull( "condition attribute should be present in AST" );

				// The value should be parsed as an expression (CastExpression or CallExpression)
				// NOT as a StringLiteral with hashes in the value
				expect( attr.value.type ).notToBe( "StringLiteral",
					"Interpolated attribute should be parsed as expression, not StringLiteral. Got type: " & attr.value.type );
			});

			it( "condition with call expression should parse the function call", function() {
				var code = fileRead( variables.testDir & "expressionAttr.cfm" );
				var ast = astFromString( code );

				var attr = findAttribute( ast, "condition" );
				expect( attr ).notToBeNull( "condition attribute should be present" );

				// Value should be a CastExpression (toBoolean) wrapping a CallExpression
				// or directly a CallExpression depending on implementation
				var valueType = attr.value.type;
				expect( valueType == "CallExpression" || valueType == "CastExpression" ).toBeTrue(
					"Expected CallExpression or CastExpression, got: " & valueType );
			});

			it( "condition expression should have correct call structure", function() {
				// Parse cfloop with condition containing interpolated call expression
				var code = fileRead( variables.testDir & "interpolatedAttr.cfm" );
				var ast = astFromString( code );

				var attr = findAttribute( ast, "condition" );
				expect( attr ).notToBeNull( "condition attribute should be present" );

				// Navigate to the actual call expression (may be wrapped in CastExpression)
				var expr = attr.value;
				if ( expr.type == "CastExpression" ) {
					expr = expr.argument;
				}

				// Should be a CallExpression for it.hasNext()
				expect( expr.type ).toBe( "CallExpression", "Should be a CallExpression" );
				expect( expr.callee.type ).toBe( "MemberExpression", "Callee should be MemberExpression" );
			});

		});

	}

	/**
	 * Recursively find an Attribute node by name
	 */
	private function findAttribute( required struct node, required string name ) {
		if ( ( node.type ?: "" ) == "Attribute" && ( node.name ?: "" ) == name ) {
			return node;
		}
		for ( var key in node ) {
			var val = node[ key ];
			if ( isStruct( val ) ) {
				var result = findAttribute( val, name );
				if ( !isNull( result ) ) return result;
			} else if ( isArray( val ) ) {
				for ( var item in val ) {
					if ( isStruct( item ) ) {
						var result = findAttribute( item, name );
						if ( !isNull( result ) ) return result;
					}
				}
			}
		}
		return;
	}

}
