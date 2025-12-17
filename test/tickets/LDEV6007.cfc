component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	variables.testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6007/";

	function run( testResults, testBox ) {

		describe( "LDEV-6007: Component inside cfscript becomes sibling instead of child", function() {

			it( "should nest component inside cfscript body", function() {
				var ast = astFromPath( variables.testDir & "scriptComponent.cfc" );

				// The AST should have a single top-level element: cfscript
				expect( ast.type ).toBe( "Program" );
				expect( ast.body ).toBeArray();

				// Find the cfscript tag
				var cfscriptTag = findTagByName( ast, "script" );
				expect( cfscriptTag ).notToBeNull( "cfscript tag should be present in AST" );

				// The component should be INSIDE the cfscript body, not as a sibling
				var componentInBody = findTagByName( cfscriptTag, "component" );

				// Currently fails: component is a sibling, not a child
				expect( isNull( componentInBody ) ).toBeFalse(
					"component should be nested inside cfscript body, not as a sibling" );
			});

			it( "should not have component as sibling to cfscript", function() {
				var ast = astFromPath( variables.testDir & "scriptComponent.cfc" );

				// Count top-level CFMLTag nodes
				var topLevelTags = [];
				for ( var node in ast.body ) {
					if ( ( node.type ?: "" ) == "CFMLTag" ) {
						arrayAppend( topLevelTags, node.name ?: "unknown" );
					}
				}

				// There should only be one top-level tag (cfscript), not two (cfscript + component)
				expect( arrayLen( topLevelTags ) ).toBe( 1,
					"Expected 1 top-level tag (cfscript) but found: " & arrayToList( topLevelTags ) );
			});

		});

	}

	/**
	 * Recursively find a tag by name
	 */
	private function findTagByName( required struct node, required string tagName ) {
		if ( ( node.type ?: "" ) == "CFMLTag" && ( node.name ?: "" ) == arguments.tagName ) {
			return node;
		}
		for ( var key in node ) {
			var val = node[ key ];
			if ( isStruct( val ) ) {
				var result = findTagByName( val, arguments.tagName );
				if ( !isNull( result ) ) return result;
			} else if ( isArray( val ) ) {
				for ( var item in val ) {
					if ( isStruct( item ) ) {
						var result = findTagByName( item, arguments.tagName );
						if ( !isNull( result ) ) return result;
					}
				}
			}
		}
		return;
	}

}
