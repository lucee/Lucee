component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	variables.testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV5980/";

	function run( testResults, testBox ) {

		describe( "LDEV-5980: BIF calls keep adding internal metadata", function() {

			it( "dump() should not have internal metadata arguments in AST", function() {
				var code = fileRead( variables.testDir & "dump.cfm" );
				var ast = astFromString( code, "tag" );

				// Find the CallExpression for dump
				var callExpr = findCallExpression( ast, "DUMP" );
				expect( callExpr ).notToBeNull( "dump CallExpression should be present" );

				var args = callExpr.arguments;

				// dump(myVar) should have exactly 1 argument
				expect( arrayLen( args ) ).toBe( 1, "dump() should have 1 argument, not internal metadata" );

				// The argument should be the identifier MYVAR, not named params like __filename
				var firstArg = args[ 1 ];
				expect( firstArg.type ).toBe( "Identifier", "Argument should be an Identifier" );
				expect( firstArg.name ).toBe( "MYVAR", "Argument should be MYVAR" );
			});

			it( "throw() should not have internal metadata arguments in AST", function() {
				var code = fileRead( variables.testDir & "throw.cfm" );
				var ast = astFromString( code, "tag" );

				// Find the CallExpression for throw
				var callExpr = findCallExpression( ast, "THROW" );
				expect( callExpr ).notToBeNull( "throw CallExpression should be present" );

				var args = callExpr.arguments;

				// throw("error message") should have exactly 1 argument
				expect( arrayLen( args ) ).toBe( 1, "throw() should have 1 argument, not internal metadata" );
			});

		});
	}

	/**
	 * Recursively find a CallExpression by callee name
	 */
	private function findCallExpression( required struct node, required string funcName ) {
		if ( ( node.type ?: "" ) == "CallExpression" ) {
			var callee = node.callee ?: {};
			// Direct function call
			if ( ( callee.type ?: "" ) == "Identifier" && ( callee.name ?: "" ) == arguments.funcName ) {
				return node;
			}
		}

		for ( var key in node ) {
			var val = node[ key ];
			if ( isStruct( val ) ) {
				var result = findCallExpression( val, arguments.funcName );
				if ( !isNull( result ) ) return result;
			} else if ( isArray( val ) ) {
				for ( var item in val ) {
					if ( isStruct( item ) ) {
						var result = findCallExpression( item, arguments.funcName );
						if ( !isNull( result ) ) return result;
					}
				}
			}
		}
		return;
	}

}
