<cfscript>
	scene = form.scene ?: "literal";

	// fresh row with NULL columns for both properties
	queryExecute( "DELETE FROM LDEV6303 WHERE id='r1'", {}, { datasource: "LDEV6303" } );
	queryExecute( "INSERT INTO LDEV6303( id, literalDef, exprDef ) VALUES( 'r1', NULL, NULL )", {}, { datasource: "LDEV6303" } );

	ormClearSession();

	row = entityLoadByPK( "Org6303", "r1" );
	if ( isNull( row ) ) throw( message="entity not found" );

	switch( scene ) {
		case "literal":    echo( row.getLiteralDef() ); break;
		case "expression": echo( row.getExprDef() );    break;
		default: echo( "unknown scene" );
	}
</cfscript>
