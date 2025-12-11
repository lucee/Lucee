component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	variables.testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV5969/";

	function run( testResults, testBox ) {

		describe( "LDEV-5969: cffunction/cfargument attributes missing from AST", function() {

			it( "cffunction attributes should be captured in AST", function() {
				var code = fileRead( variables.testDir & "simple.cfm" );
				var ast = astFromString( code, "tag" );

				// Find the cffunction tag in the AST
				var funcTag = findTagByName( ast, "function" );
				expect( funcTag ).notToBeNull( "cffunction tag should be present in AST" );

				// Verify attributes are present
				var attrs = funcTag.attributes;
				expect( attrs ).toBeArray();
				expect( arrayLen( attrs ) ).toBeGTE( 4, "Should have at least 4 attributes (name, access, returntype, output)" );

				// Check specific attributes exist
				var attrNames = attrs.map( function( a ) { return a.name; } );
				expect( attrNames ).toInclude( "name" );
				expect( attrNames ).toInclude( "access" );
				expect( attrNames ).toInclude( "returntype" );
				expect( attrNames ).toInclude( "output" );
			});

			it( "cfargument attributes should be captured in AST", function() {
				var code = fileRead( variables.testDir & "withArgument.cfm" );
				var ast = astFromString( code, "tag" );

				// Find the cfargument tag in the AST
				var argTag = findTagByName( ast, "argument" );
				expect( argTag ).notToBeNull( "cfargument tag should be present in AST" );

				// Verify attributes are present
				var attrs = argTag.attributes;
				expect( attrs ).toBeArray();
				expect( arrayLen( attrs ) ).toBeGTE( 4, "Should have at least 4 attributes (name, type, required, default)" );

				// Check specific attributes exist
				var attrNames = attrs.map( function( a ) { return a.name; } );
				expect( attrNames ).toInclude( "name" );
				expect( attrNames ).toInclude( "type" );
				expect( attrNames ).toInclude( "required" );
				expect( attrNames ).toInclude( "default" );
			});

			it( "cffunction with multiple cfarguments should capture all argument attributes", function() {
				var code = fileRead( variables.testDir & "multipleArgs.cfm" );
				var ast = astFromString( code, "tag" );

				// Find all cfargument tags
				var argTags = findAllTagsByName( ast, "argument" );
				expect( arrayLen( argTags ) ).toBe( 2, "Should have 2 cfargument tags" );

				// First argument should have name="firstName"
				var firstArg = argTags[ 1 ];
				var firstArgNames = firstArg.attributes.map( function( a ) { return a.name; } );
				expect( firstArgNames ).toInclude( "name" );

				// Second argument should have default attribute
				var secondArg = argTags[ 2 ];
				var secondArgNames = secondArg.attributes.map( function( a ) { return a.name; } );
				expect( secondArgNames ).toInclude( "default" );
			});

			it( "custom/metadata attributes should be captured on cffunction and cfargument", function() {
				var code = fileRead( variables.testDir & "customAttrs.cfm" );
				var ast = astFromString( code, "tag" );

				// Check cffunction custom attributes
				var funcTag = findTagByName( ast, "function" );
				expect( funcTag ).notToBeNull();
				var funcAttrNames = funcTag.attributes.map( function( a ) { return a.name; } );
				expect( funcAttrNames ).toInclude( "customattr", "Custom attribute should be preserved on cffunction" );
				expect( funcAttrNames ).toInclude( "anotherattr", "Another custom attribute should be preserved on cffunction" );

				// Check cfargument custom attributes
				var argTag = findTagByName( ast, "argument" );
				expect( argTag ).notToBeNull();
				var argAttrNames = argTag.attributes.map( function( a ) { return a.name; } );
				expect( argAttrNames ).toInclude( "mymeta", "Custom attribute should be preserved on cfargument" );
				expect( argAttrNames ).toInclude( "extrainfo", "Another custom attribute should be preserved on cfargument" );
			});

		});
	}

	/**
	 * Recursively find a tag by name in the AST
	 */
	private function findTagByName( required struct node, required string tagName ) {
		if ( ( node.type ?: "" ) == "CFMLTag" && ( node.name ?: "" ) == arguments.tagName ) {
			return node;
		}
		if ( structKeyExists( node, "body" ) ) {
			if ( isArray( node.body ) ) {
				for ( var child in node.body ) {
					if ( isStruct( child ) ) {
						var result = findTagByName( child, arguments.tagName );
						if ( !isNull( result ) ) return result;
					}
				}
			} else if ( isStruct( node.body ) ) {
				var result = findTagByName( node.body, arguments.tagName );
				if ( !isNull( result ) ) return result;
			}
		}
		if ( structKeyExists( node, "children" ) && isArray( node.children ) ) {
			for ( var child in node.children ) {
				if ( isStruct( child ) ) {
					var result = findTagByName( child, arguments.tagName );
					if ( !isNull( result ) ) return result;
				}
			}
		}
		return;
	}

	/**
	 * Recursively find all tags by name in the AST
	 */
	private function findAllTagsByName( required struct node, required string tagName, array results = [] ) {
		if ( ( node.type ?: "" ) == "CFMLTag" && ( node.name ?: "" ) == arguments.tagName ) {
			arrayAppend( results, node );
		}
		if ( structKeyExists( node, "body" ) ) {
			if ( isArray( node.body ) ) {
				for ( var child in node.body ) {
					if ( isStruct( child ) ) {
						findAllTagsByName( child, arguments.tagName, results );
					}
				}
			} else if ( isStruct( node.body ) ) {
				findAllTagsByName( node.body, arguments.tagName, results );
			}
		}
		if ( structKeyExists( node, "children" ) && isArray( node.children ) ) {
			for ( var child in node.children ) {
				if ( isStruct( child ) ) {
					findAllTagsByName( child, arguments.tagName, results );
				}
			}
		}
		return results;
	}

}
