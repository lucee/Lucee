component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	variables.testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV5986/";

	function run( testResults, testBox ) {

		describe( "LDEV-5986: Tag islands should have dedicated node type in AST", function() {

			it( "should have a TagIsland node type wrapping the tag content", function() {
				var code = fileRead( variables.testDir & "tagIsland.cfm" );
				var ast = astFromString( code, "cfml" );

				// Tag islands should have their own node type like TagIsland or similar
				// Currently the backticks become StringLiteral with "\r\n" and the
				// tag content is parsed inline without being wrapped in a TagIsland node

				// Check for a TagIsland node (or similar dedicated type)
				var tagIsland = findNodeByType( ast, "TagIsland" );
				expect( isNull( tagIsland ) ).toBeFalse( "Tag island should have a dedicated TagIsland node type" );
			});

			it( "tag island should not produce spurious StringLiteral from backticks", function() {
				var code = fileRead( variables.testDir & "tagIsland.cfm" );
				var ast = astFromString( code, "cfml" );

				// Currently the backtick delimiters ``` become StringLiteral nodes
				// with value "\r\n" - these shouldn't exist
				var scriptBody = findScriptBody( ast );
				expect( scriptBody ).notToBeNull( "Should find cfscript body" );

				// Count StringLiterals that are just whitespace - these come from the backticks
				var spuriousStrings = 0;
				for ( var stmt in scriptBody ) {
					if ( ( stmt.type ?: "" ) == "ExpressionStatement" ) {
						var expr = stmt.expression ?: {};
						if ( ( expr.type ?: "" ) == "StringLiteral" ) {
							var val = expr.value ?: "";
							if ( reFind( "^[\r\n\s]*$", val ) ) {
								spuriousStrings++;
							}
						}
					}
				}
				expect( spuriousStrings ).toBe( 0, "Backtick delimiters should not become StringLiteral nodes" );
			});

		});

	}

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

	private function findScriptBody( required struct node ) {
		if ( ( node.type ?: "" ) == "CFMLTag" && ( node.name ?: "" ) == "script" ) {
			if ( structKeyExists( node, "body" ) && structKeyExists( node.body, "body" ) ) {
				return node.body.body;
			}
		}
		for ( var key in node ) {
			var val = node[ key ];
			if ( isStruct( val ) ) {
				var result = findScriptBody( val );
				if ( !isNull( result ) ) return result;
			} else if ( isArray( val ) ) {
				for ( var item in val ) {
					if ( isStruct( item ) ) {
						var result = findScriptBody( item );
						if ( !isNull( result ) ) return result;
					}
				}
			}
		}
		return;
	}

}
