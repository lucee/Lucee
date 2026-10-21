component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-6031: cfimport custom tags in AST", function() {

			it( "should not leak __custom_tag_path internal attribute", function() {
				var ast = getAst( "cfimport-basic.cfm" );
				var customTag = findCustomTag( ast, "my:hello" );

				expect( customTag ).notToBeNull();

				// Check no internal attributes leaked
				var attrNames = customTag.attributes.map( function( a ) { return a.name; } );
				expect( attrNames ).notToInclude( "__custom_tag_path" );
			});

			it( "should have tag name in 'name' field, not just appendix", function() {
				var ast = getAst( "cfimport-basic.cfm" );
				var customTag = findCustomTag( ast, "my:hello" );

				expect( customTag ).notToBeNull();
				// name should be "hello", not empty string
				expect( customTag.name ).toBe( "hello" );
			});

			it( "should preserve cfimport tag in AST", function() {
				var ast = getAst( "cfimport-basic.cfm" );
				var importTag = findTag( ast, "import" );

				expect( importTag ).notToBeNull();
				expect( importTag.fullname ).toBe( "cfimport" );
			});

			it( "should preserve all custom tag invocations in body", function() {
				var ast = getAst( "cfimport-multiple.cfm" );

				// Should have cfimport + 3 custom tags + whitespace nodes
				var customTags = ast.body.filter( function( node ) {
					return ( node.type == "CFMLTag" && node.keyExists( "nameSpace" ) && node.nameSpace == "my" );
				});

				expect( customTags.len() ).toBe( 3 );
			});

			it( "should handle nested custom tags", function() {
				var ast = getAst( "cfimport-nested.cfm" );
				var outerTag = findCustomTag( ast, "my:outer" );

				expect( outerTag ).notToBeNull();
				expect( outerTag.keyExists( "body" ) ).toBeTrue();

				// Find inner tag in body
				var innerTag = findCustomTag( outerTag.body, "my:inner" );
				expect( innerTag ).notToBeNull();
			});

			// Quirk #50: Imported custom tags should not be missing from AST
			it( "should not omit imported custom tags from AST body", function() {
				var ast = getAst( "cfimport-basic.cfm" );

				// Count all CFMLTag nodes in body
				var allTags = ast.body.filter( function( node ) {
					return node.type == "CFMLTag";
				});

				// Should have at least cfimport + custom tag (not just cfimport)
				expect( allTags.len() ).toBeGTE( 2, "Custom tag should not be missing from AST" );

				// Verify the custom tag is present (not just cfimport)
				var hasCustomTag = allTags.some( function( tag ) {
					return tag.keyExists( "nameSpace" ) && tag.nameSpace == "my";
				});
				expect( hasCustomTag ).toBeTrue( "Imported prefix custom tag should be in AST body" );
			});

		});
	}

	private function getAst( required string filename ) {
		var testDir = getDirectoryFromPath( getCurrentTemplatePath() );
		var filePath = testDir & "LDEV6031/" & filename;
		return astFromPath( filePath );
	}

	private function findTag( required struct ast, required string tagName ) {
		var body = ast.keyExists( "body" ) ? ( isArray( ast.body ) ? ast.body : ast.body.body ?: [] ) : [];
		for ( var node in body ) {
			if ( node.type == "CFMLTag" && node.name == tagName ) {
				return node;
			}
		}
		return javaCast( "null", 0 );
	}

	private function findCustomTag( required struct ast, required string fullname ) {
		var body = ast.keyExists( "body" ) ? ( isArray( ast.body ) ? ast.body : ast.body.body ?: [] ) : [];
		for ( var node in body ) {
			if ( node.type == "CFMLTag" && node.keyExists( "fullname" ) && node.fullname == fullname ) {
				return node;
			}
		}
		return javaCast( "null", 0 );
	}

}
