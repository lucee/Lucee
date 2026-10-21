component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	variables.testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6036/";

	function run( testResults, testBox ) {
		describe( "LDEV-6036: AST should not add default attribute values", function() {

			describe( "Script-style properties", function() {

				it( "property without type should NOT have type attribute in AST", function() {
					var ast = astFromPath( variables.testDir & "propertyNoType.cfc" );

					// Find the first property (no type in source)
					var prop = findProperty( ast, "Mixins" );
					expect( prop ).notToBeNull( "Mixins property should be found in AST" );

					// Convert to struct for easier checking
					var attrs = attrsToStruct( prop );

					// Should have name and inject only
					expect( attrs ).toHaveKey( "name", "property should have name attribute" );
					expect( attrs.name.value ).toBe( "Mixins" );

					expect( attrs ).toHaveKey( "inject", "property should have inject attribute" );
					expect( attrs.inject.value ).toBe( "id:Plugins" );

					// Check attributes - should NOT contain type="any"
					expect( attrs ).notToHaveKey( "type", "property without type in source should NOT have type attribute in AST" );
				});

				it( "property WITH explicit type should have type attribute in AST", function() {
					var ast = astFromPath( variables.testDir & "propertyNoType.cfc" );

					// Find the second property (has type="string" in source)
					var prop = findProperty( ast, "WithType" );
					expect( prop ).notToBeNull( "WithType property should be found in AST" );

					// Convert to struct for easier checking
					var attrs = attrsToStruct( prop );

					// Should have type attribute since it was in source
					expect( attrs ).toHaveKey( "type", "property with type in source should have type attribute in AST" );
					expect( attrs.type.value ).toBe( "string" );
				});

				it( "property WITH explicit type='any' should preserve type attribute with quoteChar", function() {
					var ast = astFromPath( variables.testDir & "propertyNoType.cfc" );

					// Find the property with explicit type="any"
					var prop = findProperty( ast, "ExplicitAny" );
					expect( prop ).notToBeNull( "ExplicitAny property should be found in AST" );

					// Convert to struct for easier checking
					var attrs = attrsToStruct( prop );

					// Should have type attribute since it was explicitly in source
					expect( attrs ).toHaveKey( "type", "property with explicit type='any' should have type attribute" );
					expect( attrs.type.value ).toBe( "any" );
					// CRITICAL: explicit type="any" should have quoteChar (proving it came from source)
					expect( attrs.type ).toHaveKey( "quoteChar", "explicit type='any' should have quoteChar (from source)" );
				});

				it( "only source attributes should have quoteChar", function() {
					var ast = astFromPath( variables.testDir & "propertyNoType.cfc" );

					var prop = findProperty( ast, "Mixins" );
					expect( prop ).notToBeNull( "Mixins property should be found in AST" );

					// Convert to struct for easier checking
					var attrs = attrsToStruct( prop );

					// Source attributes should have quoteChar
					expect( attrs ).toHaveKey( "name" );
					expect( attrs.name ).toHaveKey( "quoteChar", "source attribute should have quoteChar" );

					// If type exists (bug), it should NOT have quoteChar (proving it's a default)
					if ( structKeyExists( attrs, "type" ) ) {
						// If we get here, the bug exists - the default was added
						// Check that at least it lacks quoteChar (confirming it's not from source)
						expect( attrs.type ).notToHaveKey( "quoteChar",
							"default type attribute should NOT have quoteChar (proving it was added, not from source)"
						);
					}
				});

			});

			describe( "Tag-style properties", function() {

				it( "cfproperty without type should NOT have type attribute in AST", function() {
					var ast = astFromPath( variables.testDir & "propertyNoTypeTag.cfc" );

					// Find the first property (no type in source)
					var prop = findProperty( ast, "TagMixins" );
					expect( prop ).notToBeNull( "TagMixins property should be found in AST" );

					// Convert to struct for easier checking
					var attrs = attrsToStruct( prop );

					// Should have name and inject only
					expect( attrs ).toHaveKey( "name", "property should have name attribute" );
					expect( attrs.name.value ).toBe( "TagMixins" );

					expect( attrs ).toHaveKey( "inject", "property should have inject attribute" );
					expect( attrs.inject.value ).toBe( "id:Plugins" );

					// Check attributes - should NOT contain type="any"
					expect( attrs ).notToHaveKey( "type", "cfproperty without type in source should NOT have type attribute in AST" );
				});

				it( "cfproperty WITH explicit type should have type attribute in AST", function() {
					var ast = astFromPath( variables.testDir & "propertyNoTypeTag.cfc" );

					// Find the second property (has type="string" in source)
					var prop = findProperty( ast, "TagWithType" );
					expect( prop ).notToBeNull( "TagWithType property should be found in AST" );

					// Convert to struct for easier checking
					var attrs = attrsToStruct( prop );

					// Should have type attribute since it was in source
					expect( attrs ).toHaveKey( "type", "cfproperty with type in source should have type attribute in AST" );
					expect( attrs.type.value ).toBe( "string" );
				});

				it( "cfproperty WITH explicit type='any' should preserve type attribute with quoteChar", function() {
					var ast = astFromPath( variables.testDir & "propertyNoTypeTag.cfc" );

					// Find the property with explicit type="any"
					var prop = findProperty( ast, "TagExplicitAny" );
					expect( prop ).notToBeNull( "TagExplicitAny property should be found in AST" );

					// Convert to struct for easier checking
					var attrs = attrsToStruct( prop );

					// Should have type attribute since it was explicitly in source
					expect( attrs ).toHaveKey( "type", "cfproperty with explicit type='any' should have type attribute" );
					expect( attrs.type.value ).toBe( "any" );
					// CRITICAL: explicit type="any" should have quoteChar (proving it came from source)
					expect( attrs.type ).toHaveKey( "quoteChar", "explicit type='any' should have quoteChar (from source)" );
				});

			});

			describe( "Script-style functions", function() {

				it( "function without access should NOT have accessExplicit=true in AST", function() {
					var ast = astFromPath( variables.testDir & "functionNoModifiers.cfc" );

					// Find function with no modifiers
					var fn = findFunction( ast, "noModifiers" );
					expect( fn ).notToBeNull( "noModifiers function should be found in AST" );

					// access should exist (default public), but accessExplicit should NOT exist
					expect( fn ).toHaveKey( "access", "function should have access (default)" );
					expect( fn ).notToHaveKey( "accessExplicit", "function without access in source should NOT have accessExplicit" );
				});

				it( "function without returnType should have returnType.explicit=false in AST", function() {
					var ast = astFromPath( variables.testDir & "functionNoModifiers.cfc" );

					// Find function with no modifiers
					var fn = findFunction( ast, "noModifiers" );
					expect( fn ).notToBeNull( "noModifiers function should be found in AST" );

					// returnType should exist (default any), but explicit should be false/missing
					expect( fn ).toHaveKey( "returnType", "function should have returnType (default)" );
					expect( fn.returnType ).notToHaveKey( "explicit", "function without returnType in source should NOT have returnType.explicit" );
				});

				it( "function WITH explicit public should have accessExplicit=true in AST", function() {
					var ast = astFromPath( variables.testDir & "functionNoModifiers.cfc" );

					var fn = findFunction( ast, "explicitPublic" );
					expect( fn ).notToBeNull( "explicitPublic function should be found in AST" );

					// Should have access and accessExplicit since it was explicitly in source
					expect( fn ).toHaveKey( "access", "function with explicit public should have access in AST" );
					expect( fn.access ).toBe( "public" );
					expect( fn ).toHaveKey( "accessExplicit", "function with explicit public should have accessExplicit" );
					expect( fn.accessExplicit ).toBeTrue( "accessExplicit should be true for explicit public" );
				});

				it( "function WITH explicit returnType should have returnType.explicit=true in AST", function() {
					var ast = astFromPath( variables.testDir & "functionNoModifiers.cfc" );

					var fn = findFunction( ast, "explicitAnyReturn" );
					expect( fn ).notToBeNull( "explicitAnyReturn function should be found in AST" );

					// Should have returnType with explicit=true since it was explicitly in source
					expect( fn ).toHaveKey( "returnType", "function with explicit any return should have returnType in AST" );
					expect( fn.returnType.value ).toBe( "any" );
					expect( fn.returnType ).toHaveKey( "explicit", "function with explicit any should have returnType.explicit" );
					expect( fn.returnType.explicit ).toBeTrue( "returnType.explicit should be true for explicit any" );
				});

				it( "function WITH explicit public any should have both explicit flags in AST", function() {
					var ast = astFromPath( variables.testDir & "functionNoModifiers.cfc" );

					var fn = findFunction( ast, "explicitBoth" );
					expect( fn ).notToBeNull( "explicitBoth function should be found in AST" );

					expect( fn ).toHaveKey( "access", "function with explicit public should have access in AST" );
					expect( fn.access ).toBe( "public" );
					expect( fn ).toHaveKey( "accessExplicit", "function with explicit public should have accessExplicit" );
					expect( fn.accessExplicit ).toBeTrue( "accessExplicit should be true" );

					expect( fn ).toHaveKey( "returnType", "function with explicit any return should have returnType in AST" );
					expect( fn.returnType.value ).toBe( "any" );
					expect( fn.returnType ).toHaveKey( "explicit", "function with explicit any should have returnType.explicit" );
					expect( fn.returnType.explicit ).toBeTrue( "returnType.explicit should be true" );
				});

				it( "function with only returnType should NOT have accessExplicit in AST", function() {
					var ast = astFromPath( variables.testDir & "functionNoModifiers.cfc" );

					var fn = findFunction( ast, "onlyReturnType" );
					expect( fn ).notToBeNull( "onlyReturnType function should be found in AST" );

					// access should exist (default), but accessExplicit should NOT exist
					expect( fn ).toHaveKey( "access", "function should have access (default)" );
					expect( fn ).notToHaveKey( "accessExplicit", "function without access in source should NOT have accessExplicit" );

					// Should have returnType with explicit=true since it was explicitly in source
					expect( fn ).toHaveKey( "returnType", "function with explicit string return should have returnType in AST" );
					expect( fn.returnType.value ).toBe( "string" );
					expect( fn.returnType ).toHaveKey( "explicit", "function with explicit string should have returnType.explicit" );
					expect( fn.returnType.explicit ).toBeTrue( "returnType.explicit should be true" );
				});

				it( "function with only access should NOT have returnType.explicit in AST", function() {
					var ast = astFromPath( variables.testDir & "functionNoModifiers.cfc" );

					var fn = findFunction( ast, "onlyAccess" );
					expect( fn ).notToBeNull( "onlyAccess function should be found in AST" );

					// Should have access and accessExplicit since it was explicitly in source
					expect( fn ).toHaveKey( "access", "function with explicit private should have access in AST" );
					expect( fn.access ).toBe( "private" );
					expect( fn ).toHaveKey( "accessExplicit", "function with explicit private should have accessExplicit" );
					expect( fn.accessExplicit ).toBeTrue( "accessExplicit should be true for explicit private" );

					// returnType should exist (default), but explicit should NOT exist
					expect( fn ).toHaveKey( "returnType", "function should have returnType (default)" );
					expect( fn.returnType ).notToHaveKey( "explicit", "function without returnType in source should NOT have returnType.explicit" );
				});

			});

		});
	}

	/**
	 * Find a function by name in the AST
	 */
	private function findFunction( required struct node, required string name ) {
		var nodeType = node.type ?: "";

		// Check if this is a FunctionDeclaration with matching name
		if ( nodeType == "FunctionDeclaration" ) {
			var fnName = "";
			// AST uses "name" with StringLiteral containing function name
			if ( structKeyExists( node, "name" ) && isStruct( node.name ) ) {
				fnName = node.name.value ?: "";
			}
			if ( uCase( fnName ) == uCase( name ) ) {
				return node;
			}
		}

		// Recurse into body
		if ( structKeyExists( node, "body" ) ) {
			if ( isStruct( node.body ) ) {
				var result = findFunction( node.body, name );
				if ( !isNull( result ) ) return result;
			} else if ( isArray( node.body ) ) {
				for ( var child in node.body ) {
					if ( isStruct( child ) ) {
						var result = findFunction( child, name );
						if ( !isNull( result ) ) return result;
					}
				}
			}
		}

		// Recurse into body.body (for components)
		if ( structKeyExists( node, "body" ) && isStruct( node.body ) && structKeyExists( node.body, "body" ) ) {
			if ( isArray( node.body.body ) ) {
				for ( var child in node.body.body ) {
					if ( isStruct( child ) ) {
						var result = findFunction( child, name );
						if ( !isNull( result ) ) return result;
					}
				}
			}
		}

		return;
	}

	/**
	 * Find a property by name in the AST
	 */
	private function findProperty( required struct node, required string name ) {
		var nodeType = node.type ?: "";

		// Check if this is a property with matching name
		if ( nodeType == "CFMLTag" && ( node.name ?: "" ) == "property" ) {
			var nameAttr = getAttr( node, "name" );
			if ( !isNull( nameAttr ) && uCase( nameAttr.value.value ?: "" ) == uCase( name ) ) {
				return node;
			}
		}

		// Recurse into body
		if ( structKeyExists( node, "body" ) ) {
			if ( isStruct( node.body ) ) {
				var result = findProperty( node.body, name );
				if ( !isNull( result ) ) return result;
			} else if ( isArray( node.body ) ) {
				for ( var child in node.body ) {
					if ( isStruct( child ) ) {
						var result = findProperty( child, name );
						if ( !isNull( result ) ) return result;
					}
				}
			}
		}

		// Recurse into body.body (for components)
		if ( structKeyExists( node, "body" ) && isStruct( node.body ) && structKeyExists( node.body, "body" ) ) {
			if ( isArray( node.body.body ) ) {
				for ( var child in node.body.body ) {
					if ( isStruct( child ) ) {
						var result = findProperty( child, name );
						if ( !isNull( result ) ) return result;
					}
				}
			}
		}

		return;
	}

	/**
	 * Get an attribute from a tag node by name
	 */
	private function getAttr( required struct node, required string name ) {
		if ( !structKeyExists( node, "attributes" ) || !isArray( node.attributes ) ) {
			return;
		}
		for ( var attr in node.attributes ) {
			if ( uCase( attr.name ?: "" ) == uCase( name ) ) {
				return attr;
			}
		}
		return;
	}

	/**
	 * Convert attributes array to struct keyed by attribute name
	 */
	private struct function attrsToStruct( required struct node ) {
		var result = {};
		if ( !structKeyExists( node, "attributes" ) || !isArray( node.attributes ) ) {
			return result;
		}
		for ( var attr in node.attributes ) {
			result[ attr.name ] = attr.value;
		}
		return result;
	}

}
