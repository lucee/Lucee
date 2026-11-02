<cfscript>
	queryExecute( "CREATE TABLE IF NOT EXISTS t5026app ( id int, name varchar(20) )" );
	try {
		// application default
		queryExecute( "INSERT INTO t5026app VALUES ( :id, :name )", { id: 1, name: "a" }, { result: "r1" } );

		// per query override wins over the application default
		queryExecute( "INSERT INTO t5026app VALUES ( :id, :name )", { id: 2, name: "b" }, { result: "r2", includeSQL: true } );

		query name="q3" result="r3" {
			echo( "SELECT * FROM t5026app WHERE id = " );
			queryparam value=1 cfsqltype="integer";
		}

		echo( serializeJSON( {
			r1: { hasSql: structKeyExists( r1, "sql" ), hasParams: structKeyExists( r1, "sqlparameters" ), recordcount: r1.recordcount },
			r2: { hasSql: structKeyExists( r2, "sql" ), hasParams: structKeyExists( r2, "sqlparameters" ), sql: r2.sql, sqlparameters: r2.sqlparameters },
			r3: { hasSql: structKeyExists( r3, "sql" ), hasParams: structKeyExists( r3, "sqlparameters" ), recordcount: r3.recordcount }
		} ) );
	}
	finally {
		queryExecute( "DROP TABLE IF EXISTS t5026app" );
	}
</cfscript>
