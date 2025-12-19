component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	variables.testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6027/";

	function run( testResults, testBox ) {

		describe( "LDEV-6027: Property attribute order should be preserved in AST", function() {

			it( "property attributes should maintain declaration order", function() {
				var ast = astFromPath( variables.testDir & "PropertyOrder.cfc" );

				// Find first property node
				var prop = findFirstProperty( ast );
				expect( prop ).notToBeNull( "Should find a property in the AST" );

				// Get attribute names in order
				var attrNames = [];
				for ( var attr in ( prop.attributes ?: [] ) ) {
					arrayAppend( attrNames, attr.name );
				}

				// Should be: name, type, default (declaration order)
				// Bug: Currently returns alphabetical order
				expect( attrNames[ 1 ] ).toBe( "name", "First attribute should be 'name', got: " & attrNames.toList() );
				expect( attrNames[ 2 ] ).toBe( "type", "Second attribute should be 'type', got: " & attrNames.toList() );
				expect( attrNames[ 3 ] ).toBe( "default", "Third attribute should be 'default', got: " & attrNames.toList() );
			});

		});

	}

	/**
	 * Recursively find the first property CFMLTag node
	 */
	private function findFirstProperty( required struct node ) {
		if ( ( node.type ?: "" ) == "CFMLTag" && ( node.name ?: "" ) == "property" ) {
			return node;
		}
		for ( var key in node ) {
			var val = node[ key ];
			if ( isStruct( val ) ) {
				var result = findFirstProperty( val );
				if ( !isNull( result ) ) return result;
			} else if ( isArray( val ) ) {
				for ( var item in val ) {
					if ( isStruct( item ) ) {
						var result = findFirstProperty( item );
						if ( !isNull( result ) ) return result;
					}
				}
			}
		}
		return;
	}

}
