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

		describe( "LDEV-5990: Function attributes like returnFormat should be preserved", function() {

			it( "should include returnFormat attribute on remote functions", function() {
				var ast = astFromPath( variables.testDir & "hintedParams.cfc" );

				// Find the remoteFunc function
				var func = findFunction( ast, "remoteFunc" );
				expect( func ).notToBeNull( "remoteFunc function should be found in AST" );

				// Bug: returnFormat attribute is missing from AST
				expect( func ).toHaveKey( "returnFormat", "remote function should have returnFormat in AST" );
				expect( func.returnFormat.value ).toBe( "json", "returnFormat value should be preserved" );
			});

		});

		describe( "LDEV-5990: Docblock hints should include source annotation", function() {

			it( "should indicate hint came from docblock vs attribute", function() {
				var ast = astFromPath( variables.testDir & "hintedParams.cfc" );

				// Find the withDocblock function (has hint from docblock)
				var func = findFunction( ast, "withDocblock" );
				expect( func ).notToBeNull( "withDocblock function should be found in AST" );
				expect( func ).toHaveKey( "hint" );
				// Should have some way to know this hint came from a docblock
				expect( func ).toHaveKey( "hintSource", "AST should indicate hint source" );
				expect( func.hintSource ).toBe( "docblock" );
			});

			it( "should indicate hint came from attribute when using hint attribute", function() {
				var ast = astFromPath( variables.testDir & "hintedParams.cfc" );

				// Find the withHintAttr function (has hint="..." attribute)
				var func = findFunction( ast, "withHintAttr" );
				expect( func ).notToBeNull( "withHintAttr function should be found in AST" );
				expect( func ).toHaveKey( "hint" );
				// Should indicate this hint came from an attribute
				expect( func ).toHaveKey( "hintSource", "AST should indicate hint source" );
				expect( func.hintSource ).toBe( "attribute" );
			});

			it( "docblock hints should NOT have quoteChar (original source wasn't quoted)", function() {
				var ast = astFromPath( variables.testDir & "hintedParams.cfc" );

				// Find the withDocblock function (has hint from docblock)
				var func = findFunction( ast, "withDocblock" );
				expect( func ).notToBeNull( "withDocblock function should be found in AST" );
				expect( func ).toHaveKey( "hint" );
				expect( func.hintSource ).toBe( "docblock" );
				// Docblock hints should NOT have quoteChar - the original source wasn't quoted
				expect( func.hint ).notToHaveKey( "quoteChar", "docblock hints should not have quoteChar" );
			});

			it( "attribute hints SHOULD have quoteChar (original source was quoted)", function() {
				var ast = astFromPath( variables.testDir & "hintedParams.cfc" );

				// Find the withHintAttr function (has hint="..." attribute)
				var func = findFunction( ast, "withHintAttr" );
				expect( func ).notToBeNull( "withHintAttr function should be found in AST" );
				expect( func ).toHaveKey( "hint" );
				expect( func.hintSource ).toBe( "attribute" );
				// Attribute hints SHOULD have quoteChar - the original source was quoted
				expect( func.hint ).toHaveKey( "quoteChar", "attribute hints should have quoteChar" );
			});

		});

		describe( "LDEV-5990: Raw docblock should be preserved for round-tripping", function() {

			it( "should include raw docblock text in AST when hintSource is docblock", function() {
				var ast = astFromPath( variables.testDir & "hintedParams.cfc" );

				var func = findFunction( ast, "withDocblock" );
				expect( func ).notToBeNull( "withDocblock function should be found in AST" );
				expect( func.hintSource ).toBe( "docblock" );
				// Raw docblock should be preserved for round-trip fidelity
				expect( func ).toHaveKey( "docblock", "AST should include raw docblock text" );
				expect( func.docblock ).toInclude( "This hint comes from a docblock" );
				// Must include opening and closing comment markers for valid output
				expect( func.docblock ).toInclude( "/**", "docblock should include opening /**" );
				expect( func.docblock ).toInclude( "*/", "docblock should include closing */" );
			});

			it( "should NOT include docblock key when hint comes from attribute", function() {
				var ast = astFromPath( variables.testDir & "hintedParams.cfc" );

				var func = findFunction( ast, "withHintAttr" );
				expect( func ).notToBeNull( "withHintAttr function should be found in AST" );
				expect( func.hintSource ).toBe( "attribute" );
				// No docblock when hint comes from attribute
				expect( func ).notToHaveKey( "docblock", "AST should not have docblock when hint is from attribute" );
			});

		});

		describe( "LDEV-5990: Docblock format variations", function() {

			it( "should handle single-line docblock", function() {
				var ast = astFromPath( variables.testDir & "docblockVariations.cfc" );

				var func = findFunction( ast, "singleLineDoc" );
				expect( func ).notToBeNull( "singleLineDoc function should be found in AST" );
				expect( func ).toHaveKey( "docblock" );
				expect( func.docblock ).toInclude( "Single line docblock" );
				expect( func.docblock ).toInclude( "/**" );
				expect( func.docblock ).toInclude( "*/" );
			});

			it( "should handle docblock with only @tags no description", function() {
				var ast = astFromPath( variables.testDir & "docblockVariations.cfc" );

				var func = findFunction( ast, "onlyTags" );
				expect( func ).notToBeNull( "onlyTags function should be found in AST" );
				expect( func ).toHaveKey( "metadata" );
				expect( func.metadata ).toHaveKey( "return" );
			});

			it( "should handle docblock with multiple @param tags", function() {
				var ast = astFromPath( variables.testDir & "docblockVariations.cfc" );

				var func = findFunction( ast, "multipleParams" );
				expect( func ).notToBeNull( "multipleParams function should be found in AST" );
				// Each param should have its hint from the docblock
				expect( func.params[ 1 ].hint.value ).toBe( "First parameter" );
				expect( func.params[ 2 ].hint.value ).toBe( "Second parameter" );
				expect( func.params[ 3 ].hint.value ).toBe( "Third parameter" );
			});

			it( "should handle empty docblock (just /** */)", function() {
				var ast = astFromPath( variables.testDir & "docblockVariations.cfc" );

				var func = findFunction( ast, "emptyDocblock" );
				expect( func ).notToBeNull( "emptyDocblock function should be found in AST" );
				// Empty docblock should still be preserved
				expect( func ).toHaveKey( "docblock" );
				expect( func.docblock ).toInclude( "/**" );
				expect( func.docblock ).toInclude( "*/" );
			});

			it( "should handle docblock with special characters", function() {
				var ast = astFromPath( variables.testDir & "docblockVariations.cfc" );

				var func = findFunction( ast, "specialChars" );
				expect( func ).notToBeNull( "specialChars function should be found in AST" );
				expect( func ).toHaveKey( "hint" );
				// Hint should preserve special characters
				expect( func.hint.value ).toInclude( "<html>" );
				expect( func.hint.value ).toInclude( "&" );
				expect( func.hint.value ).toInclude( """" );
			});

			it( "should handle component-level docblock", function() {
				var ast = astFromPath( variables.testDir & "docblockVariations.cfc" );

				// Component tag should have its docblock (not Program level)
				var componentTag = ast.body[ 1 ];
				// Script-based components use CFMLTag type
				expect( componentTag.type ).toBe( "CFMLTag" );
				expect( componentTag.name ).toBe( "component" );
				expect( componentTag ).toHaveKey( "docblock" );
				expect( componentTag.docblock ).toInclude( "Component-level docblock" );
				expect( componentTag.docblock ).toInclude( "@author" );
			});

		});

		describe( "LDEV-5990: Docblock metadata tags should be in AST", function() {

			it( "should include @return tag in AST metadata", function() {
				var ast = astFromPath( variables.testDir & "hintedParams.cfc" );

				var func = findFunction( ast, "withFullDocblock" );
				expect( func ).notToBeNull( "withFullDocblock function should be found in AST" );
				// @return should be accessible in AST
				expect( func ).toHaveKey( "metadata", "AST should include metadata from docblock" );
				expect( func.metadata ).toHaveKey( "return", "metadata should include @return" );
				expect( func.metadata.return ).toInclude( "A greeting message" );
			});

			it( "should include @deprecated tag in AST metadata", function() {
				var ast = astFromPath( variables.testDir & "hintedParams.cfc" );

				var func = findFunction( ast, "withFullDocblock" );
				expect( func ).notToBeNull( "withFullDocblock function should be found in AST" );
				// @deprecated should be accessible in AST
				expect( func ).toHaveKey( "metadata", "AST should include metadata from docblock" );
				expect( func.metadata ).toHaveKey( "deprecated", "metadata should include @deprecated" );
				expect( func.metadata.deprecated ).toInclude( "Use greetV2 instead" );
			});

			it( "should include param hints from docblock @param tags", function() {
				var ast = astFromPath( variables.testDir & "hintedParams.cfc" );

				var func = findFunction( ast, "withFullDocblock" );
				expect( func ).notToBeNull( "withFullDocblock function should be found in AST" );
				// Param hints from @name and @age should be on the params
				expect( func.params[ 1 ].hint.value ).toBe( "The name parameter description" );
				expect( func.params[ 2 ].hint.value ).toBe( "The age parameter description" );
			});

		});

		describe( "LDEV-5990: Tag-based CFC support", function() {

			it( "should include hint on cfargument in tag-based CFC", function() {
				var ast = astFromPath( variables.testDir & "tagBasedParams.cfc" );

				var func = findFunction( ast, "withHint" );
				expect( func ).notToBeNull( "withHint function should be found in AST" );

				// Tag-based: cfargument is in body, hint is an attribute
				var arg = findTagArgument( func, "name" );
				expect( arg ).notToBeNull( "cfargument 'name' should be found" );
				var hintAttr = getTagAttr( arg, "hint" );
				expect( hintAttr ).notToBeNull( "cfargument should have hint attribute" );
				expect( hintAttr.value.value ).toBe( "The user's full name" );
			});

			it( "should include default on cfargument in tag-based CFC", function() {
				var ast = astFromPath( variables.testDir & "tagBasedParams.cfc" );

				var func = findFunction( ast, "withDefault" );
				expect( func ).notToBeNull( "withDefault function should be found in AST" );

				// Tag-based: default is an attribute on cfargument
				var arg = findTagArgument( func, "greeting" );
				expect( arg ).notToBeNull( "cfargument 'greeting' should be found" );
				var defaultAttr = getTagAttr( arg, "default" );
				expect( defaultAttr ).notToBeNull( "cfargument should have default attribute" );
				expect( defaultAttr.value.value ).toBe( "Hello" );
			});

			it( "should include hint on cffunction in tag-based CFC", function() {
				var ast = astFromPath( variables.testDir & "tagBasedParams.cfc" );

				var func = findFunction( ast, "withFuncHint" );
				expect( func ).notToBeNull( "withFuncHint function should be found in AST" );

				// Tag-based: hint is an attribute on cffunction
				var hintAttr = getTagAttr( func, "hint" );
				expect( hintAttr ).notToBeNull( "cffunction should have hint attribute" );
				expect( hintAttr.value.value ).toBe( "Function hint from attribute" );
			});

			it( "should include returnFormat on remote cffunction", function() {
				var ast = astFromPath( variables.testDir & "tagBasedParams.cfc" );

				var func = findFunction( ast, "remoteFunc" );
				expect( func ).notToBeNull( "remoteFunc function should be found in AST" );

				// Tag-based: returnformat is an attribute
				var rfAttr = getTagAttr( func, "returnformat" );
				expect( rfAttr ).notToBeNull( "cffunction should have returnformat attribute" );
				expect( rfAttr.value.value ).toBe( "json" );
			});

			it( "should include component-level hint from cfcomponent tag", function() {
				var ast = astFromPath( variables.testDir & "tagBasedParams.cfc" );

				// Find the cfcomponent tag in the body
				var comp = ast.body[ 1 ];
				expect( comp.type ).toBe( "CFMLTag" );
				expect( comp.name ).toBe( "component" );

				// Component hint is an attribute on cfcomponent
				var hintAttr = getTagAttr( comp, "hint" );
				expect( hintAttr ).notToBeNull( "cfcomponent should have hint attribute" );
				expect( hintAttr.value.value ).toBe( "Tag-based component hint" );
			});

		});

	}

	/**
	 * Recursively find a FunctionDeclaration or CFMLTag function by name
	 */
	private function findFunction( required struct node, required string name ) {
		var nodeType = node.type ?: "";

		// Script-based: FunctionDeclaration
		if ( isSimpleValue( nodeType ) && nodeType == "FunctionDeclaration" ) {
			var nodeName = node.name ?: "";
			if ( isStruct( nodeName ) ) nodeName = nodeName.value ?: "";
			if ( isSimpleValue( nodeName ) && uCase( nodeName ) == uCase( name ) ) {
				return node;
			}
		}

		// Tag-based: CFMLTag with name="function"
		if ( isSimpleValue( nodeType ) && nodeType == "CFMLTag" && ( node.name ?: "" ) == "function" ) {
			var funcName = getTagAttrValue( node, "name" );
			if ( uCase( funcName ) == uCase( name ) ) {
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

	/**
	 * Get attribute value from a CFMLTag's attributes array
	 */
	private string function getTagAttrValue( required struct tag, required string attrName ) {
		var attrs = tag.attributes ?: [];
		for ( var attr in attrs ) {
			if ( ( attr.name ?: "" ) == attrName ) {
				return attr.value.value ?: "";
			}
		}
		return "";
	}

	/**
	 * Get attribute struct from a CFMLTag's attributes array
	 */
	private function getTagAttr( required struct tag, required string attrName ) {
		var attrs = tag.attributes ?: [];
		for ( var attr in attrs ) {
			if ( ( attr.name ?: "" ) == attrName ) {
				return attr;
			}
		}
		return;
	}

	/**
	 * Find cfargument tag by name within a cffunction
	 */
	private function findTagArgument( required struct funcTag, required string argName ) {
		var body = funcTag.body.body ?: [];
		for ( var item in body ) {
			if ( ( item.type ?: "" ) == "CFMLTag" && ( item.name ?: "" ) == "argument" ) {
				var name = getTagAttrValue( item, "name" );
				if ( uCase( name ) == uCase( argName ) ) {
					return item;
				}
			}
		}
		return;
	}

}
