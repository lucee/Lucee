component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	variables.testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6015/";

	function run( testResults, testBox ) {

		describe( "LDEV-6015: AST StringLiteral should include quoteChar", function() {

			it( "should preserve double quote in script", function() {
				var ast = astFromPath( variables.testDir & "scriptDouble.cfc" );
				// Component body -> first statement is assignment
				var comp = ast.body[1];
				var assignment = comp.body.body[1];
				var strLiteral = assignment.right;

				expect( strLiteral.type ).toBe( "StringLiteral" );
				expect( strLiteral.value ).toBe( "hello" );
				expect( strLiteral ).toHaveKey( "quoteChar", "StringLiteral should have quoteChar field" );
				expect( strLiteral.quoteChar ).toBe( '"', "Should preserve double quote" );
			});

			it( "should preserve single quote in script", function() {
				var ast = astFromPath( variables.testDir & "scriptSingle.cfc" );
				var comp = ast.body[1];
				var assignment = comp.body.body[1];
				var strLiteral = assignment.right;

				expect( strLiteral.type ).toBe( "StringLiteral" );
				expect( strLiteral.value ).toBe( "hello" );
				expect( strLiteral ).toHaveKey( "quoteChar", "StringLiteral should have quoteChar field" );
				expect( strLiteral.quoteChar ).toBe( "'", "Should preserve single quote" );
			});

			it( "should preserve double quote in tag attribute", function() {
				var ast = astFromPath( variables.testDir & "doubleQuote.cfm" );
				var setTag = ast.body[1];
				expect( setTag.type ).toBe( "CFMLTag" );
				expect( setTag.name ).toBe( "set" );
				// cfset has "noname" attribute with AssignmentExpression value
				var assignment = setTag.attributes[1].value;
				expect( assignment.type ).toBe( "AssignmentExpression" );
				var strLiteral = assignment.right;
				expect( strLiteral.type ).toBe( "StringLiteral" );
				expect( strLiteral ).toHaveKey( "quoteChar", "StringLiteral should have quoteChar field" );
				expect( strLiteral.quoteChar ).toBe( '"' );
			});

			it( "should preserve single quote in tag attribute", function() {
				var ast = astFromPath( variables.testDir & "singleQuote.cfm" );
				var setTag = ast.body[1];
				expect( setTag.type ).toBe( "CFMLTag" );
				expect( setTag.name ).toBe( "set" );
				// cfset has "noname" attribute with AssignmentExpression value
				var assignment = setTag.attributes[1].value;
				expect( assignment.type ).toBe( "AssignmentExpression" );
				var strLiteral = assignment.right;
				expect( strLiteral.type ).toBe( "StringLiteral" );
				expect( strLiteral ).toHaveKey( "quoteChar", "StringLiteral should have quoteChar field" );
				expect( strLiteral.quoteChar ).toBe( "'" );
			});

			it( "should preserve single quote in TemplateLiteral (interpolated string)", function() {
				var ast = astFromPath( variables.testDir & "templateLiteralSingle.cfc" );
				var comp = ast.body[1];
				var assignment = comp.body.body[1];
				var templateLiteral = assignment.right;

				expect( templateLiteral.type ).toBe( "TemplateLiteral" );
				expect( templateLiteral ).toHaveKey( "quoteChar", "TemplateLiteral should have quoteChar field like StringLiteral" );
				expect( templateLiteral.quoteChar ).toBe( "'", "Should preserve single quote for interpolated string" );
			});

			it( "should preserve quoteChar in inline function param hint", function() {
				// Inline param hints (hint="...") should have quoteChar
				var ast = astFromString( 'function test( string name hint="my description" ) {}', "script" );
				var param = ast.body[1].params[1];
				expect( param.hint.type ).toBe( "StringLiteral" );
				expect( param.hint.value ).toBe( "my description" );
				expect( param.hint ).toHaveKey( "quoteChar", "inline param hint should have quoteChar" );
				expect( param.hint.quoteChar ).toBe( '"', "Should preserve double quote in param hint" );
			});

			it( "should NOT have quoteChar for docblock param hint", function() {
				// Docblock @param hints are unquoted in source, so no quoteChar
				var code = '/**' & chr(10) &
				           ' * @name The name param' & chr(10) &
				           ' */' & chr(10) &
				           'function test( string name ) {}';
				var ast = astFromString( code, "script" );
				var param = ast.body[1].params[1];
				expect( param.hint.type ).toBe( "StringLiteral" );
				expect( param.hint.value ).toBe( "The name param" );
				// Docblock hints should NOT have quoteChar since they're unquoted in source
				expect( param.hint ).notToHaveKey( "quoteChar", "docblock param hint should not have quoteChar" );
			});

			it( "should NOT have quoteChar for unquoted numeric attribute coerced to StringLiteral", function() {
				// When width=100 is parsed, Lucee coerces it to StringLiteral
				// But since the original source had no quotes, quoteChar should NOT be set
				// The raw field shows quotes but that's just the string representation, not source-level info
				var ast = astFromPath( variables.testDir & "unquotedNumericAttr.cfc" );
				var comp = ast.body[1];
				var cfimageTag = comp.body.body[1];
				expect( cfimageTag.type ).toBe( "CFMLTag" );
				expect( cfimageTag.name ).toBe( "image" );

				// Find the width attribute
				var widthAttr = cfimageTag.attributes.filter( function( a ) { return a.name == "width"; } )[1];
				expect( widthAttr ).toBeStruct( "width attribute should exist" );
				expect( widthAttr.value.type ).toBe( "StringLiteral", "unquoted numeric should become StringLiteral" );
				expect( widthAttr.value.value ).toBe( "100" );
				// Since original source was unquoted, quoteChar should NOT be present
				expect( widthAttr.value ).notToHaveKey( "quoteChar", "StringLiteral from unquoted source should not have quoteChar" );
			});

			it( "should preserve quoteChar for bracket notation string key with double quotes", function() {
				// struct["key"] - the string key should have quoteChar
				var ast = astFromString( 'x = myStruct["myKey"];', "script" );
				var assignment = ast.body[1];
				var memberExpr = assignment.right;

				expect( memberExpr.type ).toBe( "MemberExpression" );
				expect( memberExpr.computed ).toBeTrue( "bracket notation should be computed" );
				expect( memberExpr.property.type ).toBe( "StringLiteral" );
				expect( memberExpr.property.value ).toBe( "myKey" );
				expect( memberExpr.property ).toHaveKey( "quoteChar", "bracket notation string key should have quoteChar" );
				expect( memberExpr.property.quoteChar ).toBe( '"', "Should preserve double quote in bracket notation" );
			});

			it( "should preserve quoteChar for bracket notation string key with single quotes", function() {
				// struct['key'] - the string key should have quoteChar
				var ast = astFromString( "x = myStruct['myKey'];", "script" );
				var assignment = ast.body[1];
				var memberExpr = assignment.right;

				expect( memberExpr.type ).toBe( "MemberExpression" );
				expect( memberExpr.computed ).toBeTrue( "bracket notation should be computed" );
				expect( memberExpr.property.type ).toBe( "StringLiteral" );
				expect( memberExpr.property.value ).toBe( "myKey" );
				expect( memberExpr.property ).toHaveKey( "quoteChar", "bracket notation string key should have quoteChar" );
				expect( memberExpr.property.quoteChar ).toBe( "'", "Should preserve single quote in bracket notation" );
			});

			it( "should preserve quoteChar for bracket notation with hyphenated key", function() {
				// struct["hyphen-key"] - common pattern, must have quoteChar to round-trip
				var ast = astFromString( 'x = headers["X-CSRF-Token"];', "script" );
				var assignment = ast.body[1];
				var memberExpr = assignment.right;

				expect( memberExpr.type ).toBe( "MemberExpression" );
				expect( memberExpr.computed ).toBeTrue( "bracket notation should be computed" );
				expect( memberExpr.property.type ).toBe( "StringLiteral" );
				expect( memberExpr.property.value ).toBe( "X-CSRF-Token" );
				expect( memberExpr.property ).toHaveKey( "quoteChar", "hyphenated bracket key must have quoteChar for round-trip" );
				expect( memberExpr.property.quoteChar ).toBe( '"' );
			});

			it( "should preserve quoteChar for nested bracket notation", function() {
				// struct["a"]["b"] - both keys should have quoteChar
				var ast = astFromString( 'x = data["level1"]["level2"];', "script" );
				var assignment = ast.body[1];
				var outerMember = assignment.right;

				expect( outerMember.type ).toBe( "MemberExpression" );
				expect( outerMember.property.type ).toBe( "StringLiteral" );
				expect( outerMember.property.value ).toBe( "level2" );
				expect( outerMember.property ).toHaveKey( "quoteChar", "outer bracket key should have quoteChar" );

				var innerMember = outerMember.object;
				expect( innerMember.type ).toBe( "MemberExpression" );
				expect( innerMember.property.type ).toBe( "StringLiteral" );
				expect( innerMember.property.value ).toBe( "level1" );
				expect( innerMember.property ).toHaveKey( "quoteChar", "inner bracket key should have quoteChar" );
			});

		});

		describe( "Break/Continue label quoteChar", function() {

			// SKIP: break label uses CastExpression, continue uses StringLiteral - Lucee AST inconsistency
		xit( "should use consistent AST type for break and continue labels", function() {
				// Both break and continue should use same AST type for unquoted labels
				var breakAst = astFromString( "outer: for( i = 1; i <= 5; i++ ) { break outer; }", "script" );
				var continueAst = astFromString( "outer: for( i = 1; i <= 5; i++ ) { continue outer; }", "script" );

				var breakStmt = breakAst.body[1].body.body[1];
				var continueStmt = continueAst.body[1].body.body[1];

				var breakLabel = breakStmt.attributes.filter( function( a ) { return a.name == "label"; } )[1];
				var continueLabel = continueStmt.attributes.filter( function( a ) { return a.name == "label"; } )[1];

				// Both should have same AST type for the label value
				expect( breakLabel.value.type ).toBe( continueLabel.value.type,
					"break and continue should use consistent AST type for unquoted labels (break=#breakLabel.value.type#, continue=#continueLabel.value.type#)" );
			});

			it( "should NOT have quoteChar for unquoted continue label", function() {
				// continue outer; - unquoted label should NOT have quoteChar
				var ast = astFromString( "outer: for( i = 1; i <= 5; i++ ) { continue outer; }", "script" );
				var forStmt = ast.body[1];
				var continueStmt = forStmt.body.body[1];

				expect( continueStmt.type ).toBe( "CFMLTag" );
				expect( continueStmt.name ).toBe( "continue" );

				var labelAttr = continueStmt.attributes.filter( function( a ) { return a.name == "label"; } )[1];
				expect( labelAttr ).toBeStruct( "continue should have label attribute" );
				expect( labelAttr.value.type ).toBe( "StringLiteral" );
				expect( labelAttr.value.value ).toBe( "outer" );
				// Unquoted label should NOT have quoteChar
				expect( labelAttr.value ).notToHaveKey( "quoteChar", "unquoted continue label should not have quoteChar" );
			});

		});

		describe( "Function returntype attribute quoteChar", function() {

			it( "should have quoteChar for quoted returntype attribute", function() {
				// function test() returntype="boolean" {} - quoted attribute should have quoteChar
				var ast = astFromString( 'function test() returntype="boolean" {}', "script" );
				var func = ast.body[1];

				expect( func.type ).toBe( "FunctionDeclaration" );
				expect( func.returnType.type ).toBe( "StringLiteral" );
				expect( func.returnType.value ).toBe( "boolean" );
				expect( func.returnType ).toHaveKey( "quoteChar", "quoted returntype attribute should have quoteChar" );
				expect( func.returnType.quoteChar ).toBe( '"' );
			});

			it( "should NOT have quoteChar for inline returntype keyword", function() {
				// boolean function test() {} - inline keyword is NOT an attribute
				var ast = astFromString( 'boolean function test() {}', "script" );
				var func = ast.body[1];

				expect( func.type ).toBe( "FunctionDeclaration" );
				expect( func.returnType.type ).toBe( "StringLiteral" );
				expect( func.returnType.value ).toBe( "boolean" );
				// Inline keyword should NOT have quoteChar - it's not quoted in source
				expect( func.returnType ).notToHaveKey( "quoteChar", "inline returntype keyword should not have quoteChar" );
			});

			it( "should have position for unquoted returntype attribute (distinguishes from keyword)", function() {
				// function test() returntype=boolean {} - attribute syntax has position data
				// boolean function test() {} - keyword syntax has NO position data
				// Position presence distinguishes attribute from keyword, not quoteChar
				var ast = astFromString( 'function test() returntype=boolean {}', "script" );
				var func = ast.body[1];

				expect( func.type ).toBe( "FunctionDeclaration" );
				// Attribute syntax should have position data (distinguishes from keyword)
				expect( func.returnType ).toHaveKey( "start", "unquoted returntype attribute should have position (distinguishes from keyword)" );
			});

		});

		describe( "Param shorthand type attribute", function() {

			it( "should have StringLiteral WITHOUT position for shorthand type", function() {
				// param struct e; - shorthand type produces synthetic StringLiteral (no position)
				var ast = astFromString( 'param struct e;', "script" );
				var paramTag = ast.body[1];

				expect( paramTag.type ).toBe( "CFMLTag" );
				expect( paramTag.name ).toBe( "param" );

				var typeAttr = paramTag.attributes.filter( function( a ) { return a.name == "type"; } )[1];
				expect( typeAttr ).toBeStruct( "param should have type attribute" );
				expect( typeAttr.value.type ).toBe( "StringLiteral" );
				expect( typeAttr.value.value ).toBe( "struct" );
				// Shorthand type is synthetic - no position info
				expect( typeAttr.value ).notToHaveKey( "start", "shorthand type should NOT have position (synthetic node)" );
			});

			it( "should have CastExpression for unquoted explicit type=", function() {
				// param e type=struct; - unquoted explicit type parses as CastExpression
				var ast = astFromString( 'param e type=struct;', "script" );
				var paramTag = ast.body[1];

				expect( paramTag.type ).toBe( "CFMLTag" );
				expect( paramTag.name ).toBe( "param" );

				var typeAttr = paramTag.attributes.filter( function( a ) { return a.name == "type"; } )[1];
				expect( typeAttr ).toBeStruct( "param should have type attribute" );
				// Unquoted type= parses as variable reference (CastExpression wrapping Identifier)
				expect( typeAttr.value.type ).toBe( "CastExpression" );
				expect( typeAttr.value.argument.type ).toBe( "Identifier" );
				expect( typeAttr.value.argument.name ).toBe( "STRUCT" );
			});

			it( "should have StringLiteral WITH quoteChar for quoted explicit type=", function() {
				// param e type="struct"; - quoted explicit type parses as StringLiteral
				var ast = astFromString( 'param e type="struct";', "script" );
				var paramTag = ast.body[1];

				expect( paramTag.type ).toBe( "CFMLTag" );
				expect( paramTag.name ).toBe( "param" );

				var typeAttr = paramTag.attributes.filter( function( a ) { return a.name == "type"; } )[1];
				expect( typeAttr ).toBeStruct( "param should have type attribute" );
				expect( typeAttr.value.type ).toBe( "StringLiteral" );
				expect( typeAttr.value.value ).toBe( "struct" );
				// Quoted type= has quoteChar
				expect( typeAttr.value ).toHaveKey( "quoteChar", "quoted type attribute should have quoteChar" );
				expect( typeAttr.value.quoteChar ).toBe( '"' );
				// And has position (real source node)
				expect( typeAttr.value ).toHaveKey( "start", "quoted type should have position" );
			});

		});

		describe( "Param name attribute quoteChar", function() {

			xit( "should have quoteChar for param name attribute (shorthand syntax)", function() {
				// param url = {}; - shorthand syntax, name should have quoteChar
				var ast = astFromString( 'param url = {};', "script" );
				var paramTag = ast.body[1];

				expect( paramTag.type ).toBe( "CFMLTag" );
				expect( paramTag.name ).toBe( "param" );

				var nameAttr = paramTag.attributes.filter( function( a ) { return a.name == "name"; } )[1];
				expect( nameAttr ).toBeStruct( "param should have name attribute" );
				expect( nameAttr.value.type ).toBe( "StringLiteral" );
				expect( nameAttr.value.value ).toBe( "url" );
				// Param name needs quoteChar for round-trip - otherwise re-parses as variable reference
				expect( nameAttr.value ).toHaveKey( "quoteChar", "param name attribute needs quoteChar for round-trip" );
			});

			xit( "should have quoteChar for param name attribute (explicit name= syntax)", function() {
				// param name="myVar" default=""; - explicit syntax
				var ast = astFromString( 'param name="myVar" default="";', "script" );
				var paramTag = ast.body[1];

				expect( paramTag.type ).toBe( "CFMLTag" );
				expect( paramTag.name ).toBe( "param" );

				var nameAttr = paramTag.attributes.filter( function( a ) { return a.name == "name"; } )[1];
				expect( nameAttr ).toBeStruct( "param should have name attribute" );
				expect( nameAttr.value.type ).toBe( "StringLiteral" );
				expect( nameAttr.value.value ).toBe( "myVar" );
				expect( nameAttr.value ).toHaveKey( "quoteChar", "explicit param name attribute should have quoteChar" );
			});

		});
	}

}
