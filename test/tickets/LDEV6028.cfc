component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	function run( testResults, testBox ) {
		describe( "LDEV-6028: Struct key AST type changes based on separator (: vs =)", function() {

			it( "numeric key with colon is NumberLiteral", function() {
				var ast = astFromString( 'x = { 1: "a" };', "script" );
				var key = ast.body[1].right.properties[1].key;

				expect( key.type ).toBe( "NumberLiteral" );
				expect( key.value ).toBe( 1 );
			});

			it( "numeric key with equals should be NumberLiteral not StringLiteral", function() {
				var ast = astFromString( 'x = { 1 = "a" };', "script" );
				var key = ast.body[1].right.properties[1].key;

				// BUG: Currently returns StringLiteral with raw="1" instead of NumberLiteral
				expect( key.type ).toBe( "NumberLiteral" );
				expect( key.value ).toBe( 1 );
			});

			it( "float key with colon is NumberLiteral", function() {
				var ast = astFromString( 'x = { 1.5: "a" };', "script" );
				var key = ast.body[1].right.properties[1].key;

				expect( key.type ).toBe( "NumberLiteral" );
				expect( key.value ).toBe( 1.5 );
			});

			it( "float key with equals should be NumberLiteral not StringLiteral", function() {
				var ast = astFromString( 'x = { 1.5 = "a" };', "script" );
				var key = ast.body[1].right.properties[1].key;

				// BUG: Currently returns StringLiteral with raw="1.5" instead of NumberLiteral
				expect( key.type ).toBe( "NumberLiteral" );
				expect( key.value ).toBe( 1.5 );
			});

			it( "negative numeric key with colon is NumberLiteral", function() {
				var ast = astFromString( 'x = { -1: "a" };', "script" );
				var key = ast.body[1].right.properties[1].key;

				expect( key.type ).toBe( "NumberLiteral" );
				expect( key.value ).toBe( -1 );
			});

			it( "negative numeric key with equals should be NumberLiteral not StringLiteral", function() {
				var ast = astFromString( 'x = { -1 = "a" };', "script" );
				var key = ast.body[1].right.properties[1].key;

				// BUG: Currently returns StringLiteral with raw="-1" instead of NumberLiteral
				expect( key.type ).toBe( "NumberLiteral" );
				expect( key.value ).toBe( -1 );
			});

			it( "boolean key with colon is BooleanLiteral", function() {
				var ast = astFromString( 'x = { true: "a" };', "script" );
				var key = ast.body[1].right.properties[1].key;

				expect( key.type ).toBe( "BooleanLiteral" );
				expect( key.value ).toBe( true );
			});

			// Skip: true/false are reserved words in Lucee, so { true = "a" } is parsed as
				// an invalid assignment to the literal 'true', not as a struct key.
				// This is expected parser behavior, not an AST bug.
				// See: https://docs.lucee.org/guides/developing-with-lucee-server/reserved-word.html
			xit( "boolean key with equals should be BooleanLiteral not parse error", function() {
				var ast = astFromString( 'x = { true = "a" };', "script" );
				var key = ast.body[1].right.properties[1].key;

				expect( key.type ).toBe( "BooleanLiteral" );
				expect( key.value ).toBe( true );
			});

			it( "quoted string key is consistent with both separators", function() {
				var astColon = astFromString( 'x = { "foo": "a" };', "script" );
				var keyColon = astColon.body[1].right.properties[1].key;

				var astEquals = astFromString( 'x = { "foo" = "a" };', "script" );
				var keyEquals = astEquals.body[1].right.properties[1].key;

				// Both should be StringLiteral - this works correctly
				expect( keyColon.type ).toBe( "StringLiteral" );
				expect( keyEquals.type ).toBe( "StringLiteral" );
				expect( keyColon.value ).toBe( "foo" );
				expect( keyEquals.value ).toBe( "foo" );
			});

			it( "identifier key is consistent with both separators", function() {
				var astColon = astFromString( 'x = { foo: "a" };', "script" );
				var keyColon = astColon.body[1].right.properties[1].key;

				var astEquals = astFromString( 'x = { foo = "a" };', "script" );
				var keyEquals = astEquals.body[1].right.properties[1].key;

				// Both should be Identifier - this works correctly
				expect( keyColon.type ).toBe( "Identifier" );
				expect( keyEquals.type ).toBe( "Identifier" );
				expect( keyColon.name ).toBe( "foo" );
				expect( keyEquals.name ).toBe( "foo" );
			});

		});
	}

}
