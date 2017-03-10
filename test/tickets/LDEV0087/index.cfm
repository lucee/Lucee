<cfscript>
	// Dropping & Creating temporary table
	query{
		echo("DROP TABLE IF EXISTS entity;");
	}
	query{
		echo("CREATE TABLE entity ( ID INT PRIMARY KEY NOT NULL );");
	}

	try {
		OrmReload();
		object = EntityLoadByPK( "entity", 1 );
	}
	catch(any e) {
		writeOutput(e.Message);
	}
</cfscript>