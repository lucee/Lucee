component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	function run( testResults, testBox ) {
		describe( "LDEV-6029: cachedWithin preserves original expression in AST", function() {

			it( "script function with createTimespan shows CallExpression not LongLiteral", function() {
				var code = 'function test() cachedWithin="##createTimespan(0,0,0,0,20)##" { }';
				var ast = astFromString( code, "script" );
				var fn = ast.body[1];

				expect( fn.type ).toBe( "FunctionDeclaration" );
				expect( fn ).toHaveKey( "cachedWithin" );
				expect( fn.cachedWithin.type ).toBe( "CallExpression" );
				expect( fn.cachedWithin.callee.name ).toBe( "createtimespan" );
				expect( arrayLen( fn.cachedWithin.arguments ) ).toBe( 5 );
			});

			it( "script function with numeric cachedWithin shows NumberLiteral not evaluated LongLiteral", function() {
				var code = 'function test() cachedWithin=20 { }';
				var ast = astFromString( code, "script" );
				var fn = ast.body[1];

				expect( fn.type ).toBe( "FunctionDeclaration" );
				expect( fn ).toHaveKey( "cachedWithin" );
				expect( fn.cachedWithin.type ).toBe( "NumberLiteral" );
				expect( fn.cachedWithin.value ).toBe( 20 );
				expect( fn.cachedWithin.raw ).toBe( "20" );
			});

			it( "script function with string cachedWithin preserves StringLiteral", function() {
				var code = 'function test() cachedWithin="request" { }';
				var ast = astFromString( code, "script" );
				var fn = ast.body[1];

				expect( fn.type ).toBe( "FunctionDeclaration" );
				expect( fn ).toHaveKey( "cachedWithin" );
				expect( fn.cachedWithin.type ).toBe( "StringLiteral" );
				expect( fn.cachedWithin.value ).toBe( "request" );
			});

			it( "tag function with createTimespan shows CallExpression in attributes", function() {
				var code = '<cffunction name="test" cachedWithin="##createTimespan(0,0,0,0,20)##"></cffunction>';
				var ast = astFromString( code );
				var fn = ast.body[1];

				expect( fn.type ).toBe( "CFMLTag" );
				expect( fn.name ).toBe( "function" );
				var cachedAttr = fn.attributes.filter( function( a ) { return a.name == "cachedwithin"; } );
				expect( arrayLen( cachedAttr ) ).toBe( 1 );
				expect( cachedAttr[1].value.type ).toBe( "CallExpression" );
				expect( cachedAttr[1].value.callee.name ).toBe( "createtimespan" );
			});

		});
	}

}
