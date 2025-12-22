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

		describe( "LDEV-5990: Docblock description goes to annotations.description", function() {

			it( "docblock description should be in annotations.description", function() {
				var ast = astFromPath( variables.testDir & "hintedParams.cfc" );

				var func = findFunction( ast, "withDocblock" );
				expect( func ).notToBeNull( "withDocblock function should be found in AST" );
				expect( func ).toHaveKey( "docblock", "should have docblock" );
				expect( func ).toHaveKey( "annotations", "should have annotations" );
				expect( func.annotations ).toHaveKey( "description", "annotations should have description" );
				expect( func.annotations.description ).toBe( "This hint comes from a docblock" );
			});

			it( "inline hint attribute should go to metadata.hint", function() {
				var ast = astFromPath( variables.testDir & "hintedParams.cfc" );

				var func = findFunction( ast, "withHintAttr" );
				expect( func ).notToBeNull( "withHintAttr function should be found in AST" );
				expect( func ).notToHaveKey( "docblock", "should not have docblock when hint from attribute" );
				// Inline hint goes to metadata
				expect( func ).toHaveKey( "metadata", "should have metadata for inline attributes" );
				expect( func.metadata ).toHaveKey( "hint", "metadata should have hint" );
				expect( func.metadata.hint ).toBe( "This hint comes from an attribute" );
			});

		});

		describe( "LDEV-5990: Raw docblock should be preserved for round-tripping", function() {

			it( "should include raw docblock text when function has docblock", function() {
				var ast = astFromPath( variables.testDir & "hintedParams.cfc" );

				var func = findFunction( ast, "withDocblock" );
				expect( func ).notToBeNull( "withDocblock function should be found in AST" );
				// Raw docblock should be preserved for round-trip fidelity
				expect( func ).toHaveKey( "docblock", "AST should include raw docblock text" );
				expect( func.docblock ).toInclude( "This hint comes from a docblock" );
				// Must include opening and closing comment markers for valid output
				expect( func.docblock ).toInclude( "/**", "docblock should include opening /**" );
				expect( func.docblock ).toInclude( "*/", "docblock should include closing */" );
			});

			it( "should NOT include docblock key when no docblock exists", function() {
				var ast = astFromPath( variables.testDir & "hintedParams.cfc" );

				var func = findFunction( ast, "withHintAttr" );
				expect( func ).notToBeNull( "withHintAttr function should be found in AST" );
				// No docblock when hint comes from attribute only
				expect( func ).notToHaveKey( "docblock", "AST should not have docblock when none exists" );
			});

			it( "should preserve docblock EVEN when inline hint attribute also exists", function() {
				var ast = astFromPath( variables.testDir & "hintedParams.cfc" );

				var func = findFunction( ast, "docblockPlusHint" );
				expect( func ).notToBeNull( "docblockPlusHint function should be found in AST" );
				// MUST preserve docblock for round-tripping even when inline hint wins
				expect( func ).toHaveKey( "docblock", "docblock must be preserved even with inline hint" );
				expect( func.docblock ).toInclude( "Docblock description" );
				// Docblock description goes to annotations.description
				expect( func ).toHaveKey( "annotations" );
				expect( func.annotations ).toHaveKey( "description" );
				expect( func.annotations.description ).toBe( "Docblock description" );
				// Inline hint goes to metadata.hint
				expect( func ).toHaveKey( "metadata" );
				expect( func.metadata ).toHaveKey( "hint" );
				expect( func.metadata.hint ).toBe( "Attribute hint" );
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
				// annotations.description should have the parsed description
				expect( func ).toHaveKey( "annotations" );
				expect( func.annotations ).toHaveKey( "description" );
				expect( func.annotations.description ).toBe( "Single line docblock" );
			});

			it( "should handle docblock with only @tags no description", function() {
				var ast = astFromPath( variables.testDir & "docblockVariations.cfc" );

				var func = findFunction( ast, "onlyTags" );
				expect( func ).notToBeNull( "onlyTags function should be found in AST" );
				// @tags go into annotations, not metadata
				expect( func ).toHaveKey( "annotations" );
				expect( func.annotations ).toHaveKey( "return" );
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
				expect( func ).toHaveKey( "annotations" );
				expect( func.annotations ).toHaveKey( "description" );
				// Description should preserve special characters
				expect( func.annotations.description ).toInclude( "<html>" );
				expect( func.annotations.description ).toInclude( "&" );
				expect( func.annotations.description ).toInclude( """" );
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
				// annotations should have description from docblock
				expect( componentTag ).toHaveKey( "annotations" );
				expect( componentTag.annotations ).toHaveKey( "description" );
				expect( componentTag.annotations.description ).toBe( "Component-level docblock" );
			});

		});

		describe( "LDEV-5990: Docblock should NOT attach to closure default value", function() {

			it( "should attach docblock to outer function, not closure default value", function() {
				var ast = astFromPath( variables.testDir & "hintedParams.cfc" );

				var func = findFunction( ast, "withClosureDefault" );
				expect( func ).notToBeNull( "withClosureDefault function should be found in AST" );

				// The docblock should be on the OUTER function
				expect( func ).toHaveKey( "docblock", "outer function should have docblock" );
				expect( func.docblock ).toInclude( "@cb.hint" );

				// The closure default value should NOT have the docblock
				var param = func.params[ 1 ];
				expect( param.name.value ).toBe( "cb" );
				expect( param ).toHaveKey( "defaultValue", "param should have defaultValue" );
				var closure = param.defaultValue;
				expect( closure.type ).toBeWithCase( "ClosureDeclaration" );
				// BUG: docblock is incorrectly attached to closure instead of outer function
				expect( closure ).notToHaveKey( "docblock", "closure default value should NOT have docblock" );
				expect( closure ).notToHaveKey( "annotations", "closure default value should NOT have annotations" );
			});

		});

		describe( "LDEV-5990: Docblock metadata tags should be in AST", function() {

			it( "should include @return tag in AST annotations", function() {
				var ast = astFromPath( variables.testDir & "hintedParams.cfc" );

				var func = findFunction( ast, "withFullDocblock" );
				expect( func ).notToBeNull( "withFullDocblock function should be found in AST" );
				// @return should be in annotations (docblock @tags)
				expect( func ).toHaveKey( "annotations", "AST should include annotations from docblock" );
				expect( func.annotations ).toHaveKey( "return", "annotations should include @return" );
				expect( func.annotations.return ).toInclude( "A greeting message" );
			});

			it( "should include @deprecated tag in AST annotations", function() {
				var ast = astFromPath( variables.testDir & "hintedParams.cfc" );

				var func = findFunction( ast, "withFullDocblock" );
				expect( func ).notToBeNull( "withFullDocblock function should be found in AST" );
				// @deprecated should be in annotations (docblock @tags)
				expect( func ).toHaveKey( "annotations", "AST should include annotations from docblock" );
				expect( func.annotations ).toHaveKey( "deprecated", "annotations should include @deprecated" );
				expect( func.annotations.deprecated ).toInclude( "Use greetV2 instead" );
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

		describe( "LDEV-5990: Metadata should separate inline attributes from docblock annotations", function() {

			it( "should include inline custom attributes in metadata", function() {
				var ast = astFromPath( variables.testDir & "metadataSources.cfc" );

				var func = findFunction( ast, "inlineAttributeOnly" );
				expect( func ).notToBeNull( "inlineAttributeOnly function should be found in AST" );
				// Inline attributes should be in metadata
				expect( func ).toHaveKey( "metadata", "function should have metadata for inline attributes" );
				expect( func.metadata ).toHaveKey( "mixin", "metadata should include mixin attribute" );
				expect( func.metadata.mixin ).toBe( "controller" );
				// No docblock means no annotations field
				expect( func ).notToHaveKey( "docblock", "function without docblock should not have docblock key" );
			});

			it( "should put docblock annotations in annotations field, NOT metadata", function() {
				var ast = astFromPath( variables.testDir & "metadataSources.cfc" );

				var func = findFunction( ast, "docblockAnnotationsOnly" );
				expect( func ).notToBeNull( "docblockAnnotationsOnly function should be found in AST" );
				// Docblock annotations should be in annotations, NOT metadata
				expect( func ).toHaveKey( "annotations", "function should have annotations from docblock" );
				expect( func.annotations ).toHaveKey( "changes-only.hint", "annotations should include @changes-only.hint" );
				expect( func.annotations[ "changes-only.hint" ] ).toBe( "Only show differences" );
				// metadata should be empty or not exist (no inline attributes)
				if ( structKeyExists( func, "metadata" ) ) {
					expect( structIsEmpty( func.metadata ) ).toBeTrue( "metadata should be empty when only docblock annotations exist" );
				}
			});

			it( "should separate inline attributes and docblock annotations when both exist", function() {
				var ast = astFromPath( variables.testDir & "metadataSources.cfc" );

				var func = findFunction( ast, "bothInlineAndDocblock" );
				expect( func ).notToBeNull( "bothInlineAndDocblock function should be found in AST" );
				// Inline attributes go in metadata
				expect( func ).toHaveKey( "metadata", "function should have metadata for inline attributes" );
				expect( func.metadata ).toHaveKey( "mixin", "metadata should include mixin" );
				expect( func.metadata.mixin ).toBe( "model" );
				expect( func.metadata ).toHaveKey( "changesOnly", "metadata should include changesOnly" );
				// Docblock annotations go in annotations
				expect( func ).toHaveKey( "annotations", "function should have annotations from docblock" );
				expect( func.annotations ).toHaveKey( "someTag.hint", "annotations should include @someTag.hint" );
				// Annotations should NOT be in metadata
				expect( func.metadata ).notToHaveKey( "someTag.hint", "docblock annotations should NOT be in metadata" );
			});

			it( "should preserve raw docblock for round-tripping regardless of annotations", function() {
				var ast = astFromPath( variables.testDir & "metadataSources.cfc" );

				var func = findFunction( ast, "docblockAnnotationsOnly" );
				expect( func ).notToBeNull( "docblockAnnotationsOnly function should be found in AST" );
				// Raw docblock should always be preserved for round-trip
				expect( func ).toHaveKey( "docblock", "function should have raw docblock" );
				expect( func.docblock ).toInclude( "@changes-only.hint" );
				expect( func.docblock ).toInclude( "@format.options" );
			});

			it( "component: should have docblock annotations in annotations field", function() {
				var ast = astFromPath( variables.testDir & "metadataSources.cfc" );

				// Component has BOTH docblock annotations AND inline attributes
				var comp = ast.body[ 1 ];
				expect( comp.type ).toBe( "CFMLTag" );
				expect( comp.name ).toBe( "component" );
				// Inline attributes are in attributes array (standard for CFMLTag)
				expect( comp ).toHaveKey( "attributes", "component should have attributes array" );
				expect( getTagAttrValue( comp, "displayname" ) ).toBe( "MetadataTest" );
				expect( getTagAttrValue( comp, "singleton" ) ).toBe( "true" );
				// Docblock annotations go in separate annotations field
				expect( comp ).toHaveKey( "annotations", "component should have annotations from docblock" );
				// description from docblock first line
				expect( comp.annotations ).toHaveKey( "description", "annotations should include description" );
				expect( comp.annotations.description ).toInclude( "Test fixture for LDEV-5990" );
				// @tags from docblock
				expect( comp.annotations ).toHaveKey( "author", "annotations should include @author" );
				expect( comp.annotations.author ).toBe( "Test Author" );
				expect( comp.annotations ).toHaveKey( "version", "annotations should include @version" );
			});

			it( "component: should NOT have annotations when no docblock", function() {
				var ast = astFromPath( variables.testDir & "componentInlineOnly.cfc" );

				var comp = ast.body[ 1 ];
				expect( comp.type ).toBe( "CFMLTag" );
				expect( comp.name ).toBe( "component" );
				// Inline attributes in attributes array
				expect( comp ).toHaveKey( "attributes", "component should have attributes" );
				expect( getTagAttrValue( comp, "displayname" ) ).toBe( "InlineOnlyComponent" );
				expect( getTagAttrValue( comp, "singleton" ) ).toBe( "true" );
				expect( getTagAttrValue( comp, "accessors" ) ).toBe( "true" );
				// No docblock means no annotations
				expect( comp ).notToHaveKey( "annotations", "component without docblock should not have annotations" );
				expect( comp ).notToHaveKey( "docblock", "component without docblock should not have docblock" );
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
