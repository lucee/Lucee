component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	variables.testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV5978/";

	function run( testResults, testBox ) {

		describe( "LDEV-5978: queryExecute adds extra variable name argument in AST", function() {

			it( "queryExecute with 1 argument should have 1 argument in AST", function() {
				var code = fileRead( variables.testDir & "queryExecute.cfm" );
				var ast = astFromString( code, "cfml" );

				// Find the CallExpression for queryExecute
				var callExpr = findCallByName( ast, "QUERYEXECUTE" );
				expect( callExpr ).notToBeNull( "queryExecute CallExpression should be present" );

				// Should have exactly 1 argument (the SQL string)
				var args = callExpr.arguments;
				expect( args ).toBeArray();
				expect( arrayLen( args ) ).toBe( 1, "queryExecute should have 1 argument, not extra internal arguments" );
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
