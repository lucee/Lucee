component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	function run( testResults, testBox ) {
		describe( "LDEV-5981: bare template text raw has no added quotes", function() {

			it( "bare text is a StringLiteral whose raw matches the source", function() {
				var ast = astFromString( "Susi" );
				var literal = ast.body[ 1 ].expression;

				expect( ast.body[ 1 ].type ).toBe( "ExpressionStatement" );
				expect( literal.type ).toBe( "StringLiteral" );
				expect( literal.value ).toBe( "Susi" );
				expect( literal.raw ).toBe( "Susi" );
				expect( literal ).notToHaveKey( "quoteChar" );
			} );

			it( "bare text keeps internal quotes without quoting or doubling them", function() {
				var ast = astFromString( 'Say "hi" there' );
				var literal = ast.body[ 1 ].expression;

				expect( literal.type ).toBe( "StringLiteral" );
				expect( literal.value ).toBe( 'Say "hi" there' );
				expect( literal.raw ).toBe( 'Say "hi" there' );
				expect( literal ).notToHaveKey( "quoteChar" );
			} );

			it( "bare text on either side of a tag keeps each raw unquoted", function() {
				var ast = astFromString( "Hello<cfset x = 1>world" );

				expect( ast.body[ 1 ].type ).toBe( "ExpressionStatement" );
				expect( ast.body[ 1 ].expression.raw ).toBe( "Hello" );
				expect( ast.body[ 1 ].expression.value ).toBe( "Hello" );
				expect( ast.body[ 2 ].type ).toBe( "CFMLTag" );
				expect( ast.body[ 3 ].expression.raw ).toBe( "world" );
				expect( ast.body[ 3 ].expression.value ).toBe( "world" );
			} );

			it( "cfoutput body text raw has no added quotes", function() {
				var ast = astFromString( "<cfoutput>Hello</cfoutput>" );
				var literal = ast.body[ 1 ].body.body[ 1 ].expression;

				expect( literal.type ).toBe( "StringLiteral" );
				expect( literal.value ).toBe( "Hello" );
				expect( literal.raw ).toBe( "Hello" );
				expect( literal ).notToHaveKey( "quoteChar" );
			} );

			it( "a hash in bare text outside an expression stays a single hash in raw", function() {
				var hash = chr( 35 );
				var source = "Hello " & hash & "name" & hash & "!";
				var ast = astFromString( source );
				var literal = ast.body[ 1 ].expression;

				expect( literal.type ).toBe( "StringLiteral" );
				expect( literal.value ).toBe( source );
				expect( literal.raw ).toBe( source );
			} );

			it( "a doubled hash inside cfoutput stays doubled in raw and single in value", function() {
				var hash = chr( 35 );
				var ast = astFromString( "<cfoutput>price " & hash & hash & "5</cfoutput>" );
				var literal = ast.body[ 1 ].body.body[ 1 ].expression;

				expect( literal.type ).toBe( "StringLiteral" );
				expect( literal.value ).toBe( "price " & hash & "5" );
				expect( literal.raw ).toBe( "price " & hash & hash & "5" );
				expect( literal ).notToHaveKey( "quoteChar" );
			} );

			it( "a quoted script string still reports its quotes in raw", function() {
				var ast = astFromString( 'x = "Susi";', "script" );
				var literal = ast.body[ 1 ].right;

				expect( literal.type ).toBe( "StringLiteral" );
				expect( literal.value ).toBe( "Susi" );
				expect( literal.raw ).toBe( '"Susi"' );
				expect( literal.quoteChar ).toBe( '"' );
			} );

			it( "a single quoted script string still reports quoteChar and a quoted raw", function() {
				var ast = astFromString( "x = 'Susi';", "script" );
				var literal = ast.body[ 1 ].right;

				expect( literal.type ).toBe( "StringLiteral" );
				expect( literal.value ).toBe( "Susi" );
				expect( literal.raw ).toBe( '"Susi"' );
				expect( literal.quoteChar ).toBe( "'" );
			} );

			it( "an unquoted attribute value still uses the quoted raw representation", function() {
				var ast = astFromString( "<cfloop index=i from=1 to=2></cfloop>" );
				var index = ast.body[ 1 ].attributes[ 1 ];

				expect( index.name ).toBe( "index" );
				expect( index.value.type ).toBe( "StringLiteral" );
				expect( index.value.value ).toBe( "i" );
				expect( index.value.raw ).toBe( '"i"' );
				expect( index.value ).notToHaveKey( "quoteChar" );
			} );

		} );
	}

}
