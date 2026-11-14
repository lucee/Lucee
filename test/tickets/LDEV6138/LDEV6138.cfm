<cfscript>
	// LDEV-6138: ORM operations inside transactions leak unmanaged connections
	//
	// The leak sequence across transaction boundaries:
	// 1. Transaction N end() sets autoCommit=true → ORM flush borrows untracked connection @B
	// 2. Transaction N+1 begin() sets autoCommit=false
	// 3. entityLoad() in N+1 → Session.list() → afterOperation() → afterTransaction()
	//    releases @B via ConnectionProviderImpl.closeConnection() → releaseConnection()
	//    → SKIPPED because managed=false && autoCommit=false → LEAKED

	iterations = 10;

	before = getSystemMetrics().activeDatasourceConnections;

	for ( i = 1; i <= iterations; i++ ) {
		transaction {
			// load existing entities
			items = entityLoad( "TestEntity" );

			// insert + flush mid-transaction (triggers connection borrow)
			item = entityNew( "TestEntity" );
			item.setName( "test_#i#_#createUUID()#" );
			entitySave( item );
			ormFlush();

			// HQL query — exercises Session.list() which triggers afterOperation()
			results = ormExecuteQuery(
				"FROM TestEntity WHERE name LIKE :prefix",
				{ prefix: "test_#i#_%" }
			);

			// update existing entities
			for ( r in results ) {
				r.setName( r.getName() & "_updated" );
				entitySave( r );
			}

			transaction action="commit";
		}
	}

	// close the ORM session so its connection is returned before measuring
	ormCloseSession();

	// give pool a moment to settle
	sleep( 500 );

	after = getSystemMetrics().activeDatasourceConnections;
	writeOutput( after - before );
</cfscript>
