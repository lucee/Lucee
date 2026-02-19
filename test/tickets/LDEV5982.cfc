component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	function run( testResults, testBox ) {

		describe( "LDEV-5982: cflock internal id attribute leaks into AST", function() {

			it( "cflock should not have internal id attribute in AST", function() {
				var code = '<cflock type="exclusive" timeout="1"></cflock>';
				var ast = astFromString( code, "tag" );

				// Find the cflock tag in the AST
				var lockTag = findTagByName( ast, "lock" );
				expect( lockTag ).notToBeNull( "cflock tag should be present in AST" );

				// Get attribute names
				var attrs = lockTag.attributes ?: [];
				var attrNames = attrs.map( function( a ) { return a.name; } );

				// Should have type and timeout, but NOT id
				expect( attrNames ).toInclude( "type" );
				expect( attrNames ).toInclude( "timeout" );
				expect( attrNames ).notToInclude( "id", "Internal id attribute should not leak into AST" );
			});

			it( "cflock with explicit name should preserve name in AST", function() {
				var code = '<cflock name="myLock" type="exclusive" timeout="1"></cflock>';
				var ast = astFromString( code, "tag" );

				var lockTag = findTagByName( ast, "lock" );
				expect( lockTag ).notToBeNull();

				var attrs = lockTag.attributes ?: [];
				var attrNames = attrs.map( function( a ) { return a.name; } );

				// Should have name, type, timeout - but NOT id
				expect( attrNames ).toInclude( "name" );
				expect( attrNames ).toInclude( "type" );
				expect( attrNames ).toInclude( "timeout" );
				expect( attrNames ).notToInclude( "id", "Internal id attribute should not leak into AST" );
			});

			it( "cfcache tag syntax should not have internal _id attribute in AST", function() {
				var testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV5982/";
				var ast = astFromPath( testDir & "cfcache.cfm" );

				var cacheTag = findTagByName( ast, "cache" );
				expect( cacheTag ).notToBeNull( "cfcache tag should be present in AST" );

				var attrs = cacheTag.attributes ?: [];
				var attrNames = attrs.map( function( a ) { return a.name; } );

				// Should have action, but NOT _id
				expect( attrNames ).toInclude( "action" );
				expect( attrNames ).notToInclude( "_id", "Internal _id attribute should not leak into AST" );
			});

			it( "cfcache script syntax should not have internal _id attribute in AST", function() {
				var code = 'cfcache( action="flush" );';
				var ast = astFromString( code, "script" );

				var cacheTag = findTagByName( ast, "cache" );
				expect( cacheTag ).notToBeNull( "cfcache tag should be present in AST" );

				var attrs = cacheTag.attributes ?: [];
				var attrNames = attrs.map( function( a ) { return a.name; } );

				// Should have action, but NOT _id
				expect( attrNames ).toInclude( "action" );
				expect( attrNames ).notToInclude( "_id", "Internal _id attribute should not leak into AST (script mode)" );
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

}
