component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	variables.testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV5989/";

	function run( testResults, testBox ) {

		describe( "LDEV-5989: Interpolated attribute values have escaped hashes in raw field", function() {

			it( "raw should contain original single hashes for interpolated attributes", function() {
				var code = fileRead( variables.testDir & "interpolatedAttr.cfm" );
				var ast = astFromString( code );

				// Find the condition attribute
				var attr = findAttribute( ast, "condition" );
				expect( attr ).notToBeNull( "condition attribute should be present in AST" );

				// The raw field should contain single hashes like the original source
				// NOT escaped hashes like "##it.hasNext()##"
				var rawValue = attr.value.raw;

				// Check for doubled hashes (##) in raw - use chr(35) to avoid CFML escaping issues
				// If raw has ##, find() will locate chr(35)&chr(35) sequence
				var doubleHash = chr( 35 ) & chr( 35 );
				var hasDoubleHash = find( doubleHash, rawValue ) > 0;

				// If hasDoubleHash is true, raw contains ## which is the bug
				expect( hasDoubleHash ).toBeFalse( "Raw should contain single hashes, not escaped ## - got: " & rawValue );
			});

			it( "round-trip should preserve interpolated attribute values", function() {
				// Parse file with interpolated attribute
				var code1 = fileRead( variables.testDir & "roundtrip1.cfm" );
				var ast1 = astFromString( code1 );

				var attr1 = findAttribute( ast1, "condition" );
				expect( attr1 ).notToBeNull( "First parse should find condition attribute" );
				var raw1 = attr1.value.raw;

				// Build round-trip file using template
				var template = fileRead( variables.testDir & "loopTemplate.txt" );
				var code2 = replace( template, "%%CONDITION%%", raw1 );
				fileWrite( variables.testDir & "roundtrip2.cfm", code2 );

				// Parse the round-trip file
				var ast2 = astFromString( code2 );
				var attr2 = findAttribute( ast2, "condition" );
				expect( attr2 ).notToBeNull( "Second parse should find condition attribute" );

				// Raw should be stable - not doubling hashes each round
				expect( attr2.value.raw ).toBe( raw1, "Raw should be stable after round-trip, not doubling hashes" );
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
