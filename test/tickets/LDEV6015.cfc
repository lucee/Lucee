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

		});
	}

}
