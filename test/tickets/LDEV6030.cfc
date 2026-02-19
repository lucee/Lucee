component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	function run( testResults, testBox ) {
		describe( "LDEV-6030: Labeled loops should include label in AST", function() {

			it( "labeled for loop has label property", function() {
				var code = 'outer: for( var i = 1; i <= 5; i++ ) { }';
				var ast = astFromString( code, "script" );
				var forStmt = ast.body[1];

				expect( forStmt.type ).toBe( "ForStatement" );
				expect( forStmt ).toHaveKey( "label" );
				expect( forStmt.label ).toBe( "outer" );
			});

			it( "labeled while loop has label property", function() {
				var code = 'myLoop: while( true ) { break myLoop; }';
				var ast = astFromString( code, "script" );
				var whileStmt = ast.body[1];

				expect( whileStmt.type ).toBe( "WhileStatement" );
				expect( whileStmt ).toHaveKey( "label" );
				expect( whileStmt.label ).toBe( "myLoop" );
			});

			it( "labeled do-while loop has label property", function() {
				var code = 'test: do { } while( false );';
				var ast = astFromString( code, "script" );
				var doWhileStmt = ast.body[1];

				expect( doWhileStmt.type ).toBe( "DoWhileStatement" );
				expect( doWhileStmt ).toHaveKey( "label" );
				expect( doWhileStmt.label ).toBe( "test" );
			});

			it( "labeled for-in loop has label property", function() {
				var code = 'items: for( var item in [1,2,3] ) { }';
				var ast = astFromString( code, "script" );
				var forOfStmt = ast.body[1];

				expect( forOfStmt.type ).toBe( "ForOfStatement" );
				expect( forOfStmt ).toHaveKey( "label" );
				expect( forOfStmt.label ).toBe( "items" );
			});

			it( "unlabeled for loop has no label property", function() {
				var code = 'for( var i = 1; i <= 5; i++ ) { }';
				var ast = astFromString( code, "script" );
				var forStmt = ast.body[1];

				expect( forStmt.type ).toBe( "ForStatement" );
				expect( forStmt ).notToHaveKey( "label" );
			});

			it( "unlabeled while loop has no label property", function() {
				var code = 'while( false ) { }';
				var ast = astFromString( code, "script" );
				var whileStmt = ast.body[1];

				expect( whileStmt.type ).toBe( "WhileStatement" );
				expect( whileStmt ).notToHaveKey( "label" );
			});

		});

		describe( "LDEV-6030: cfbreak/cfcontinue without label should not have label attribute", function() {

			it( "cfbreak without label has no label attribute", function() {
				var code = '<cfloop from="1" to="5" index="i"><cfbreak></cfloop>';
				var ast = astFromString( code, "tag" );
				var cfloop = ast.body[1];
				var cfbreak = cfloop.body.body[1];

				expect( cfbreak.type ).toBe( "CFMLTag" );
				expect( cfbreak.name ).toBe( "break" );
				expect( cfbreak.attributes ).toBeEmpty();
			});

			it( "cfcontinue without label has no label attribute", function() {
				var code = '<cfloop from="1" to="5" index="i"><cfcontinue></cfloop>';
				var ast = astFromString( code, "tag" );
				var cfloop = ast.body[1];
				var cfcontinue = cfloop.body.body[1];

				expect( cfcontinue.type ).toBe( "CFMLTag" );
				expect( cfcontinue.name ).toBe( "continue" );
				expect( cfcontinue.attributes ).toBeEmpty();
			});

			it( "cfbreak with label has label attribute", function() {
				var code = '<cfloop from="1" to="5" index="i" label="outer"><cfbreak outer></cfloop>';
				var ast = astFromString( code, "tag" );
				var cfloop = ast.body[1];
				var cfbreak = cfloop.body.body[1];

				expect( cfbreak.type ).toBe( "CFMLTag" );
				expect( cfbreak.name ).toBe( "break" );
				expect( cfbreak.attributes ).toHaveLength( 1 );
				expect( cfbreak.attributes[1].name ).toBe( "label" );
			});

			it( "cfcontinue with label has label attribute", function() {
				var code = '<cfloop from="1" to="5" index="i" label="myloop"><cfcontinue myloop></cfloop>';
				var ast = astFromString( code, "tag" );
				var cfloop = ast.body[1];
				var cfcontinue = cfloop.body.body[1];

				expect( cfcontinue.type ).toBe( "CFMLTag" );
				expect( cfcontinue.name ).toBe( "continue" );
				expect( cfcontinue.attributes ).toHaveLength( 1 );
				expect( cfcontinue.attributes[1].name ).toBe( "label" );
			});

		});

		describe( "LDEV-6030: cfreturn should preserve expr attribute", function() {

			it( "cfreturn without value has expr attribute with NullLiteral", function() {
				var code = '<cffunction name="test"><cfreturn></cffunction>';
				var ast = astFromString( code, "tag" );
				var cffunction = ast.body[1];
				var cfreturn = cffunction.body.body[1];

				expect( cfreturn.type ).toBe( "CFMLTag" );
				expect( cfreturn.name ).toBe( "return" );
				expect( cfreturn.attributes ).toHaveLength( 1 );
				expect( cfreturn.attributes[1].name ).toBe( "expr" );
				expect( cfreturn.attributes[1].value.type ).toBe( "NullLiteral" );
			});

			it( "cfreturn with value has expr attribute", function() {
				var code = '<cffunction name="test"><cfreturn "hello"></cffunction>';
				var ast = astFromString( code, "tag" );
				var cffunction = ast.body[1];
				var cfreturn = cffunction.body.body[1];

				expect( cfreturn.type ).toBe( "CFMLTag" );
				expect( cfreturn.name ).toBe( "return" );
				expect( cfreturn.attributes ).toHaveLength( 1 );
				expect( cfreturn.attributes[1].name ).toBe( "expr" );
				expect( cfreturn.attributes[1].value.type ).toBe( "StringLiteral" );
				expect( cfreturn.attributes[1].value.value ).toBe( "hello" );
			});

		});
	}

}
