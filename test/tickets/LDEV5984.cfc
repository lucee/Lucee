component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	variables.testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV5984/";

	function run( testResults, testBox ) {

		describe( "LDEV-5984: Escaped hashes lose escaping in AST", function() {

			it( "raw should contain escaped hashes to allow round-tripping", function() {
				var code = fileRead( variables.testDir & "escapedHashes.cfm" );
				var ast = astFromString( code );

				// Find the StringLiteral in the AST
				var stringLiteral = findStringLiteral( ast );
				expect( stringLiteral ).notToBeNull( "StringLiteral should be present in AST" );

				// The value correctly contains ##hello## (the runtime value)
				// But the raw should contain #### so we can regenerate the source
				// Currently raw is "##hello##" which loses the escaping info
				var rawValue = stringLiteral.raw;

				// Count the hashes - should have 4 before and 4 after "hello"
				// i.e. raw should be something like '####hello####' or "####hello####"
				var hashCount = len( rawValue ) - len( replace( rawValue, "##", "", "all" ) );
				// Each ## = 2 chars removed, so hashCount / 2 = number of ## sequences
				// We expect 4 ## sequences (#### before hello, #### after hello)
				expect( hashCount / 2 ).toBeGTE( 4, "Raw should contain #### (escaped ##) not just ## - got: " & rawValue );
			});

			it( "round-trip should preserve escaped hashes", function() {
				// Input has #### which means literal ## in the string value
				// Need ######## in test code to produce #### in parsed string
				// ######## -> #### (in test) -> ## (in parsed value) = 2 chars per side
				var code = "<cfscript>x = '########test########';</cfscript>";
				var ast1 = astFromString( code );

				var str1 = findStringLiteral( ast1 );
				expect( isNull( str1 ) ).toBeFalse( "First parse should find StringLiteral" );

				// Value should be ##test## (the runtime value) = 8 chars
				var val1 = str1.value;
				expect( len( val1 ) ).toBe( 8, "Value should be 8 chars but got len=" & len( val1 ) );

				// If we use raw to regenerate and reparse, value should be stable
				var newCode = "<cfscript>x = " & str1.raw & ";</cfscript>";
				var ast2 = astFromString( newCode );
				var str2 = findStringLiteral( ast2 );

				expect( isNull( str2 ) ).toBeFalse( "Second parse should find StringLiteral" );
				expect( str2.value ).toBe( val1, "Value should be stable after round-trip" );
			});

		});

	}

	/**
	 * Recursively find a StringLiteral node in the AST that contains hashes
	 */
	private function findStringLiteral( required struct node ) {
		if ( ( node.type ?: "" ) == "StringLiteral" && structKeyExists( node, "value" ) ) {
			if ( find( "test", node.value ) || find( "hello", node.value ) ) {
				return node;
			}
		}
		for ( var key in node ) {
			var val = node[ key ];
			if ( isStruct( val ) ) {
				var result = findStringLiteral( val );
				if ( !isNull( result ) ) return result;
			} else if ( isArray( val ) ) {
				for ( var item in val ) {
					if ( isStruct( item ) ) {
						var result = findStringLiteral( item );
						if ( !isNull( result ) ) return result;
					}
				}
			}
		}
		return;
	}

}
