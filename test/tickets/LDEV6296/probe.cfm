<cfscript>
	// Three-phase pool exercise for the mini-app datasource.

	// Phase 1: bare cfquery — server-cred pool created.
	queryExecute(
		  "select 1 from information_schema.system_tables limit 1"
		, {}
		, { datasource: "LDEV6296_app" }
	);

	// Phase 2: matching-cred cfquery. Pre-fix creates duplicate shadow pool;
	// post-fix collapses onto Phase 1's pool.
	queryExecute(
		  "select 1 from information_schema.system_tables limit 1"
		, {}
		, { datasource: "LDEV6296_app", username: "sa", password: "" }
	);

	// Phase 3: different-cred cfquery — legitimate override; HSQLDB rejects
	// the user but the pool is registered before the connection attempt.
	try {
		queryExecute(
			  "select 1 from information_schema.system_tables limit 1"
			, {}
			, { datasource: "LDEV6296_app", username: "other", password: "other" }
		);
	}
	catch ( any e ) {
		// expected — HSQLDB rejects unknown user
	}

	echo( "ok" );
</cfscript>
