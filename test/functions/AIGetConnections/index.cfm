<cfscript>
	qry = AIGetConnections();
	rows = [];
	loop query=qry {
		arrayAppend( rows, queryRowData( qry, qry.currentRow ) );
	}
	echo( serializeJSON( {
		"isQuery": isQuery( qry ),
		"columns": listSort( qry.columnList, "textnocase" ),
		"rows": rows,
		"nameForDefault": AIGetMetaData( "default:aigetconnectionstest" ).name
	} ) );
</cfscript>
