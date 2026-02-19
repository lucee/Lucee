component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	function run( testResults, testBox ) {

		describe( "LDEV-6035: AST fullname field should be consistent for shorthand vs cf-prefixed tags", function() {

			it( "should include fullname for cf-prefixed script tag", function() {
				var code = 'cfabort;';
				var ast = astFromString( code, "script" );

				var tag = ast.body[1];
				expect( tag.type ).toBe( "CFMLTag" );
				expect( tag.name ).toBe( "ABORT" );
				expect( tag ).toHaveKey( "fullname", "cf-prefixed tag should have fullname" );
				expect( tag.fullname ).toBe( "cfabort" );
			});

			it( "should include fullname for shorthand script tag", function() {
				var code = 'abort;';
				var ast = astFromString( code, "script" );

				var tag = ast.body[1];
				expect( tag.type ).toBe( "CFMLTag" );
				expect( tag.name ).toBe( "ABORT" );
				// Bug: shorthand tag lacks fullname field
				expect( tag ).toHaveKey( "fullname",
					"Shorthand tag should have fullname for consistency" );
				expect( tag.fullname ).toBe( "abort",
					"Shorthand fullname should be 'abort', not 'cfabort'" );
			});

			it( "should include fullname for cf-prefixed tag with attributes", function() {
				var code = 'cfdump( var=x );';
				var ast = astFromString( code, "script" );

				var tag = ast.body[1];
				expect( tag.type ).toBe( "CFMLTag" );
				expect( tag.name ).toBe( "DUMP" );
				expect( tag ).toHaveKey( "fullname" );
				expect( tag.fullname ).toBe( "cfdump" );
			});

			it( "should include fullname for shorthand tag with named param syntax", function() {
				// Note: dump( var=x ) parses as CallExpression, not CFMLTag
				// Use cfhttp which has no function equivalent
				var code = 'http url="http://example.com" {}';
				var ast = astFromString( code, "script" );

				var tag = ast.body[1];
				expect( tag.type ).toBe( "CFMLTag" );
				expect( tag.name ).toBe( "HTTP" );
				// Bug: shorthand tag lacks fullname field
				expect( tag ).toHaveKey( "fullname",
					"Shorthand tag should have fullname" );
				expect( tag.fullname ).toBe( "http" );
			});

			it( "should include fullname for block tags", function() {
				var codeShorthand = 'loop from=1 to=10 index="i" {}';
				var codePrefixed = 'cfloop( from=1, to=10, index="i" ) {}';

				var astShorthand = astFromString( codeShorthand, "script" );
				var astPrefixed = astFromString( codePrefixed, "script" );

				var tagShorthand = astShorthand.body[1];
				var tagPrefixed = astPrefixed.body[1];

				expect( tagPrefixed ).toHaveKey( "fullname" );
				expect( tagPrefixed.fullname ).toBe( "cfloop" );

				// Bug: shorthand lacks fullname
				expect( tagShorthand ).toHaveKey( "fullname",
					"Shorthand block tag should have fullname" );
				expect( tagShorthand.fullname ).toBe( "loop" );
			});

			it( "should distinguish shorthand from cf-prefixed in round-trip", function() {
				// The key issue: after round-trip, we lose info about original syntax
				var shorthandCode = 'abort;';
				var prefixedCode = 'cfabort;';

				var shorthandAst = astFromString( shorthandCode, "script" );
				var prefixedAst = astFromString( prefixedCode, "script" );

				// Both have same name
				expect( shorthandAst.body[1].name ).toBe( prefixedAst.body[1].name );

				// But fullname should differ to preserve original syntax
				if ( shorthandAst.body[1].keyExists( "fullname" ) &&
				     prefixedAst.body[1].keyExists( "fullname" ) ) {
					expect( shorthandAst.body[1].fullname ).notToBe( prefixedAst.body[1].fullname,
						"fullname should differ between shorthand and cf-prefixed" );
				}
			});

		});

	}

}
