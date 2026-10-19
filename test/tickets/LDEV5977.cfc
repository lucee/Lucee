component extends="org.lucee.cfml.test.LuceeTestCase" labels="ast" {

	variables.testDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV5977/";

	function run( testResults, testBox ) {

		describe( "LDEV-5977: Ternary expression alternate value duplicates consequent", function() {

			it( "ternary alternate should be different from consequent", function() {
				var code = fileRead( variables.testDir & "ternary.cfm" );
				var ast = astFromString( code );

				// Find the ConditionalExpression
				var condExpr = findNodeByType( ast, "ConditionalExpression" );
				expect( condExpr ).notToBeNull( "ConditionalExpression should be present" );

				// Get consequent and alternate
				var consequent = condExpr.consequent;
				var alternate = condExpr.alternate;

				expect( consequent ).notToBeNull( "Consequent should be present" );
				expect( alternate ).notToBeNull( "Alternate should be present" );

				// Consequent should be "B", alternate should be "C"
				expect( consequent.name ).toBe( "B", "Consequent should be B" );
				expect( alternate.name ).toBe( "C", "Alternate should be C, not duplicating consequent" );
			});

		});
	}

	private function findNodeByType( required struct node, required string nodeType ) {
		if ( ( node.type ?: "" ) == arguments.nodeType ) {
			return node;
		}
		for ( var key in node ) {
			var val = node[ key ];
			if ( isStruct( val ) ) {
				var result = findNodeByType( val, arguments.nodeType );
				if ( !isNull( result ) ) return result;
			} else if ( isArray( val ) ) {
				for ( var item in val ) {
					if ( isStruct( item ) ) {
						var result = findNodeByType( item, arguments.nodeType );
						if ( !isNull( result ) ) return result;
					}
				}
			}
		}
		return;
	}

}
