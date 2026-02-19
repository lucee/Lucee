component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	variables.testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV5987/";

	function run( testResults, testBox ) {

		describe( "LDEV-5987: Missing parent statement error with arrow functions and threads", function() {

			it( "should parse arrow function containing thread without error", function() {
				var code = fileRead( variables.testDir & "arrowWithThread.cfm" );

				// This currently throws: missing parent Statement of Statement
				var ast = astFromString( code );

				expect( ast ).toBeStruct();
				expect( ast.type ).toBe( "Program" );
			});

			it( "should include thread tag in AST", function() {
				var code = fileRead( variables.testDir & "arrowWithThread.cfm" );
				var ast = astFromString( code );

				// Find the thread tag - main thing is it parses without error
				var threadTag = findTagByName( ast, "thread" );
				expect( isNull( threadTag ) ).toBeFalse( "Thread tag should be present in AST" );
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

	private function findTagByName( required struct node, required string tagName ) {
		if ( ( node.type ?: "" ) == "CFMLTag" && ( node.name ?: "" ) == arguments.tagName ) {
			return node;
		}
		for ( var key in node ) {
			var val = node[ key ];
			if ( isStruct( val ) ) {
				var result = findTagByName( val, arguments.tagName );
				if ( !isNull( result ) ) return result;
			} else if ( isArray( val ) ) {
				for ( var item in val ) {
					if ( isStruct( item ) ) {
						var result = findTagByName( item, arguments.tagName );
						if ( !isNull( result ) ) return result;
					}
				}
			}
		}
		return;
	}

}
