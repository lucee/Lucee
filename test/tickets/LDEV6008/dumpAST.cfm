<cfscript>
testDir = getDirectoryFromPath( getCurrentTemplatePath() );

systemOutput( "=== QUERY WITH PSQ ===", true );
ast = astFromPath( testDir & "queryWithPSQ.cfm" );

systemOutput( "=== LDEV-6008 AST DUMP ===", true );
systemOutput( serializeJSON( ast, "struct" ), true );
systemOutput( "=== END ===", true );

// Drill into the structure
systemOutput( "", true );
systemOutput( "Body length: " & arrayLen( ast.body ), true );
if ( arrayLen( ast.body ) > 0 ) {
	queryTag = ast.body[1];
	systemOutput( "Tag type: " & queryTag.type, true );
	systemOutput( "Tag name: " & ( queryTag.name ?: "n/a" ), true );

	if ( structKeyExists( queryTag, "body" ) && structKeyExists( queryTag.body, "body" ) ) {
		body = queryTag.body.body;
		systemOutput( "Body items: " & arrayLen( body ), true );
		for ( item in body ) {
			systemOutput( "  Item type: " & item.type, true );
			if ( structKeyExists( item, "expression" ) ) {
				expr = item.expression;
				systemOutput( "    Expression type: " & expr.type, true );
				systemOutput( "    Expression keys: " & structKeyList( expr ), true );
				if ( structKeyExists( expr, "callee" ) ) {
					systemOutput( "    Callee name: " & ( expr.callee.name ?: "n/a" ), true );
				}
				if ( structKeyExists( expr, "expression" ) ) {
					systemOutput( "    Inner expression type: " & expr.expression.type, true );
				}
			}
		}
	}
}

systemOutput( "", true );
systemOutput( "=== REGULAR OUTPUT WITH PSQ ===", true );
ast2 = astFromPath( testDir & "regularOutput.cfm" );
systemOutput( serializeJSON( ast2, "struct" ), true );
</cfscript>
