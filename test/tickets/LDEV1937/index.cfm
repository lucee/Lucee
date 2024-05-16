<cfscript>
	msSql= entityNew( 'msSql' );
	msSql.setLabel('test From msSqlDatasource');
	entitySave(msSql);
	id = "1"
	mySQL = entityNew( 'mySql' );
	mySQL.setID(id);
	mySQL.setLabel('test From mySqlDatasource');
	entitySave(mySQL);
	writeOutput(mySQL.getLabel()&"||"&msSql.getLabel());
</cfscript>
