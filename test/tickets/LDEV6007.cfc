component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	variables.testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6007/";

	function run( testResults, testBox ) {

		describe( "LDEV-6007: Component inside cfscript becomes sibling instead of child", function() {

			it( "should return component at root for script-based .cfc files", function() {
				// After fixing Quirk #22, astFromPath on .cfc files should return
				// the component directly at root, without cfscript wrapper
				var ast = astFromPath( variables.testDir & "scriptComponent.cfc" );

				expect( ast.type ).toBe( "Program" );
				expect( ast.body ).toBeArray();
				expect( ast.body ).toHaveLength( 1, "Should have single body element" );

				// Component should be directly at root, not wrapped in cfscript
				expect( ast.body[1].type ).toBe( "CFMLTag" );
				expect( ast.body[1].name ).toBe( "component",
					"Component should be at root, got: #ast.body[1].name#" );
			});

			it( "should have function inside the component body", function() {
				var ast = astFromPath( variables.testDir & "scriptComponent.cfc" );

				// The component should contain the function
				var component = ast.body[1];
				expect( component.name ).toBe( "component" );

				// Find function in component body
				var funcDecl = component.body.body.filter( function( item ) {
					return ( item.type ?: "" ) == "FunctionDeclaration";
				});
				expect( funcDecl ).toHaveLength( 1, "Component should contain bar function" );
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
