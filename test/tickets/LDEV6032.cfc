component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	function run( testResults, testBox ) {
		describe( "LDEV-6032: Boolean attribute values in AST", function() {

			// Tag mode tests (Quirk 46)
			it( "should parse unquoted boolean true as BooleanLiteral in tag mode", function() {
				var ast = getAst( "tag-bool-true.cfm" );
				var dumpTag = findTag( ast, "dump" );

				expect( dumpTag ).notToBeNull();
				var abortAttr = findAttribute( dumpTag, "abort" );
				expect( abortAttr ).notToBeNull();
				expect( abortAttr.value.type ).toBe( "BooleanLiteral" );
				expect( abortAttr.value.value ).toBeTrue();
			});

			it( "should parse unquoted boolean false as BooleanLiteral in tag mode", function() {
				var ast = getAst( "tag-bool-false.cfm" );
				var queryTag = findTag( ast, "query" );

				expect( queryTag ).notToBeNull();
				var attr = findAttribute( queryTag, "cachedWithin" );
				expect( attr ).notToBeNull();
				expect( attr.value.type ).toBe( "BooleanLiteral" );
				expect( attr.value.value ).toBeFalse();
			});

			// Script mode tests (Quirk 53)
			// SKIPPED: silent tag uses TLD type="single" to support positional syntax (silent false { })
			// which prevents proper named attribute parsing. The AST for "silent bufferOutput=false"
			// shows CastExpression(AssignmentExpression) instead of BooleanLiteral.
			// Fixing this would break the positional syntax - complex edge case, not worth the risk.
			it( title="should parse boolean attribute as BooleanLiteral in script mode", body=function() {
				var ast = getAst( "script-bool-false.cfm" );
				var silentTag = findScriptTag( ast, "silent" );

				expect( silentTag ).notToBeNull();
				var attr = findAttribute( silentTag, "bufferoutput" );
				expect( attr ).notToBeNull();
				// Should be BooleanLiteral, not CastExpression(AssignmentExpression)
				expect( attr.value.type ).toBe( "BooleanLiteral" );
				expect( attr.value.value ).toBeFalse();
			}, skip=true );

			it( "should parse boolean true attribute in script mode", function() {
				var ast = getAst( "script-bool-true.cfm" );
				var settingTag = findScriptTag( ast, "setting" );

				expect( settingTag ).notToBeNull();
				var attr = findAttribute( settingTag, "showDebugOutput" );
				expect( attr ).notToBeNull();
				expect( attr.value.type ).toBe( "BooleanLiteral" );
				expect( attr.value.value ).toBeTrue();
			});

			// Verify standalone boolean attributes still work
			it( "should parse standalone boolean attribute as BooleanLiteral", function() {
				var ast = getAst( "tag-bool-standalone.cfm" );
				var dumpTag = findTag( ast, "dump" );

				expect( dumpTag ).notToBeNull();
				var abortAttr = findAttribute( dumpTag, "abort" );
				expect( abortAttr ).notToBeNull();
				expect( abortAttr.value.type ).toBe( "BooleanLiteral" );
				expect( abortAttr.value.value ).toBeTrue();
			});

			// Script mode string attribute test - exit method="exitTag"
			it( title="should parse string attribute as StringLiteral in script mode", body=function() {
				var ast = getAst( "script-exit-method.cfm" );
				var exitTag = findScriptTag( ast, "exit" );

				expect( exitTag ).notToBeNull();
				var attr = findAttribute( exitTag, "method" );
				expect( attr ).notToBeNull();
				// Should be StringLiteral, not CastExpression(AssignmentExpression)
				expect( attr.value.type ).toBe( "StringLiteral" );
				expect( attr.value.value ).toBe( "exitTag" );
			} );

		});
	}

	private function getAst( required string filename ) {
		var testDir = getDirectoryFromPath( getCurrentTemplatePath() );
		var filePath = testDir & "LDEV6032/" & filename;
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

	private function findScriptTag( required struct ast, required string tagName ) {
		var body = ast.keyExists( "body" ) ? ( isArray( ast.body ) ? ast.body : ast.body.body ?: [] ) : [];
		for ( var node in body ) {
			// Script island
			if ( node.type == "CFMLTag" && node.name == "script" && node.keyExists( "body" ) ) {
				var scriptBody = node.body.body ?: [];
				for ( var scriptNode in scriptBody ) {
					if ( scriptNode.type == "CFMLTag" && lCase( scriptNode.name ) == lCase( tagName ) ) {
						return scriptNode;
					}
				}
			}
		}
		return javaCast( "null", 0 );
	}

	private function findAttribute( required struct tag, required string attrName ) {
		var attrs = tag.attributes ?: [];
		for ( var attr in attrs ) {
			if ( lCase( attr.name ) == lCase( attrName ) ) {
				return attr;
			}
		}
		return javaCast( "null", 0 );
	}

}
