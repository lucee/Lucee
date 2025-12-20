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
	}

}
