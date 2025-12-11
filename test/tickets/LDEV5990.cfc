component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	variables.testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV5990/";

	function run( testResults, testBox ) {

		describe( "LDEV-5990: Function param hint and defaultValue missing from AST", function() {

			it( "should include hint value on function parameters", function() {
				// Use astFromPath for CFC files
				var ast = astFromPath( variables.testDir & "hintedParams.cfc" );

				// Find the withHint function
				var func = findFunction( ast, "withHint" );
				expect( func ).notToBeNull( "withHint function should be found in AST" );

				// Check the first param has hint with correct value
				var param = func.params[ 1 ];
				expect( param.name.value ).toBe( "name" );
				expect( param.hint ).notToBeNull( "param should have hint attribute" );
				// Bug: hint.value is empty string instead of the actual hint text
				expect( param.hint.value ).toBe( "The user's full name", "hint value should contain the original hint text" );
			});

			it( "should include defaultValue on function parameters", function() {
				var ast = astFromPath( variables.testDir & "hintedParams.cfc" );

				// Find the withDefault function
				var func = findFunction( ast, "withDefault" );
				expect( func ).notToBeNull( "withDefault function should be found in AST" );

				// Check the first param has defaultValue
				var param = func.params[ 1 ];
				expect( param.name.value ).toBe( "greeting" );
				// Bug: defaultValue key is completely missing from param
				expect( structKeyExists( param, "defaultValue" ) ).toBeTrue( "param should have defaultValue key" );
				expect( param.defaultValue.value ).toBe( "Hello", "defaultValue should contain the default value" );
			});

			it( "should include both hint and defaultValue on same parameter", function() {
				var ast = astFromPath( variables.testDir & "hintedParams.cfc" );

				// Find the withBoth function
				var func = findFunction( ast, "withBoth" );
				expect( func ).notToBeNull( "withBoth function should be found in AST" );

				// Check the first param has both hint and defaultValue
				var param = func.params[ 1 ];
				expect( param.name.value ).toBe( "message" );
				// Bug: hint.value is empty, defaultValue key is missing
				expect( param.hint.value ).toBe( "The message to display", "hint value should be preserved" );
				expect( structKeyExists( param, "defaultValue" ) ).toBeTrue( "param should have defaultValue key" );
				expect( param.defaultValue.value ).toBe( "Welcome", "defaultValue should be preserved" );
			});

		});

	}

	/**
	 * Recursively find a FunctionDeclaration by name
	 */
	private function findFunction( required struct node, required string name ) {
		var nodeType = node.type ?: "";
		if ( isSimpleValue( nodeType ) && nodeType == "FunctionDeclaration" ) {
			var nodeName = node.name ?: "";
			if ( isStruct( nodeName ) ) nodeName = nodeName.value ?: "";
			if ( isSimpleValue( nodeName ) && uCase( nodeName ) == uCase( name ) ) {
				return node;
			}
		}
		for ( var key in node ) {
			var val = node[ key ];
			if ( isStruct( val ) ) {
				var result = findFunction( val, name );
				if ( !isNull( result ) ) return result;
			} else if ( isArray( val ) ) {
				for ( var item in val ) {
					if ( isStruct( item ) ) {
						var result = findFunction( item, name );
						if ( !isNull( result ) ) return result;
					}
				}
			}
		}
		return;
	}

}
