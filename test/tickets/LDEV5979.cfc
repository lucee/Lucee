component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	variables.testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV5979/";

	function run( testResults, testBox ) {

		describe( "LDEV-5979: isDefined adds extra scope argument in AST", function() {

			it( "isDefined with 1 argument should have 1 argument in AST", function() {
				var code = fileRead( variables.testDir & "isDefined.cfm" );
				var ast = astFromString( code );

				// Find the CallExpression for isDefined
				var callExpr = findCallByName( ast, "ISDEFINED" );
				expect( callExpr ).notToBeNull( "isDefined CallExpression should be present" );

				// Should have exactly 1 argument (the variable name string)
				var args = callExpr.arguments;
				expect( args ).toBeArray();
				expect( arrayLen( args ) ).toBe( 1, "isDefined should have 1 argument, not extra internal scope argument" );

				// The argument should be the string "foo", not a number
				var firstArg = args[ 1 ];
				expect( firstArg.type ).toBe( "StringLiteral", "First argument should be a StringLiteral" );
			});

		});
	}

	private function findCallByName( required struct node, required string funcName ) {
		if ( ( node.type ?: "" ) == "CallExpression" ) {
			var callee = node.callee ?: {};
			if ( ( callee.name ?: "" ) == arguments.funcName ) {
				return node;
			}
		}
		for ( var key in node ) {
			var val = node[ key ];
			if ( isStruct( val ) ) {
				var result = findCallByName( val, arguments.funcName );
				if ( !isNull( result ) ) return result;
			} else if ( isArray( val ) ) {
				for ( var item in val ) {
					if ( isStruct( item ) ) {
						var result = findCallByName( item, arguments.funcName );
						if ( !isNull( result ) ) return result;
					}
				}
			}
		}
		return;
	}

}
