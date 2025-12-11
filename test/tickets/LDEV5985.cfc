component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	variables.testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV5985/";

	function run( testResults, testBox ) {

		describe( "LDEV-5985: Java functions crash AST parser", function() {

			it( "should parse functions with type=java without throwing ClassCastException", function() {
				var code = fileRead( variables.testDir & "javaFunction.cfm" );

				// This currently throws:
				// class lucee.transformer.util.SourceCode cannot be cast to class lucee.transformer.util.PageSourceCode
				var ast = astFromString( code );

				expect( ast ).toBeStruct();
				expect( ast.type ).toBe( "Program" );
			});

			it( "should include java function in AST body", function() {
				var code = fileRead( variables.testDir & "javaFunction.cfm" );
				var ast = astFromString( code );

				// Find the function declaration
				var funcDecl = findNodeByType( ast, "FunctionDeclaration" );
				expect( isNull( funcDecl ) ).toBeFalse( "FunctionDeclaration should be present" );
				// Just verify we found a function - the structure may vary
				expect( funcDecl.type ).toBe( "FunctionDeclaration" );
			});

			it( "should preserve raw Java source in AST body for round-tripping", function() {
				var code = fileRead( variables.testDir & "javaFunction.cfm" );
				var ast = astFromString( code );

				// Find the function declaration
				var funcDecl = findNodeByType( ast, "FunctionDeclaration" );
				expect( isNull( funcDecl ) ).toBeFalse( "FunctionDeclaration should be present" );

				// The body should contain the raw Java source for round-tripping
				expect( funcDecl ).toHaveKey( "body" );
				expect( funcDecl.body ).toHaveKey( "raw" );
				expect( funcDecl.body.raw ).toInclude( "return i*2" );
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

}
