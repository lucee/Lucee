<cfscript>
	// 1) application-level datasource defined in this mini-app's Application.cfc
	queryExecute(
		  "select 1 from information_schema.system_tables limit 1"
		, {}
		, { datasource: "LDEV6293_app" }
	);

	// 2) dynamic datasource — inline struct, no name registered anywhere.
	// Different mem db so HSQLDB doesn't collide with the named one.
	queryExecute(
		  "select 1 from information_schema.system_tables limit 1"
		, {}
		, {
			datasource: {
				  class            : "org.hsqldb.jdbcDriver"
				, connectionString : "jdbc:hsqldb:mem:LDEV6293_dynamic;shutdown=true"
				, username         : "sa"
				, password         : ""
			}
		}
	);

	echo( "ok" );
</cfscript>
