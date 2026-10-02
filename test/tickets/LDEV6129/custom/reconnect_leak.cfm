<cfscript>
	/*
	 * LDEV-6129: reproduce the dead reconnect code bug in HibernateORMSession.getSessionAndConn().
	 *
	 * The condition `!s.isConnected()` can fire under load (e.g. MySQL 9.5, pool pressure).
	 * When it does, the current code calls:
	 *   sac.connect(pc)          -- acquires DatasourceConnection dc from pool
	 *   s.reconnect(conn)        -- ALWAYS throws IllegalStateException on Hibernate 5.6
	 *                               factory-opened sessions; dc is leaked
	 *
	 * We force isConnected() = false via reflection on the Hibernate internals, then
	 * call entityLoad() to trigger the path.
	 *
	 * Expected output before fix: ERROR: ... Cannot manually reconnect ...
	 * Expected output after fix:  ok
	 */

	// 1. Load any entity to ensure the session + connection are open and dc is in place
	entityLoad( "LDEV6129Person" );

	// 2. Get the raw Hibernate SessionImpl
	hibSession = ormGetSession();

	// 3. Walk the class hierarchy to find the private jdbcCoordinator field
	//    (declared on AbstractSharedSessionContract, not SessionImpl itself)
	coordField = javaCast( "null", "" );
	klass = hibSession.getClass();
	while ( !isNull( klass ) ) {
		try {
			coordField = klass.getDeclaredField( "jdbcCoordinator" );
			break;
		}
		catch ( any e ) {
			klass = klass.getSuperclass();
		}
	}

	if ( isNull( coordField ) ) {
		writeOutput( "SKIP: jdbcCoordinator field not found — Hibernate internals changed" );
		return;
	}

	coordField.setAccessible( true );
	jdbcCoord = coordField.get( hibSession );

	// 4. Get logicalConnection from JdbcCoordinatorImpl
	logConnField = jdbcCoord.getClass().getDeclaredField( "logicalConnection" );
	logConnField.setAccessible( true );
	logConn = logConnField.get( jdbcCoord );

	// 5. Force closed = true → isConnected() now returns false
	closedField = logConn.getClass().getDeclaredField( "closed" );
	closedField.setAccessible( true );
	closedField.set( logConn, javaCast( "boolean", true ) );

	systemOutput( "isConnected() after force-close: #hibSession.isConnected()#", true );

	// 6. Trigger getSessionAndConn() — enters reconnect path because isConnected() == false
	//    Before fix: throws IllegalStateException (s.reconnect() always throws on managed sessions)
	//    After fix:  reconnect block removed, entityLoad succeeds normally
	try {
		entityLoad( "LDEV6129Person" );
		writeOutput( "ok" );
	}
	catch ( any e ) {
		writeOutput( "ERROR: #e.type# - #e.message#" );
		systemOutput( "reconnect_leak: caught exception: #e.type# - #e.message#", true );
	}
</cfscript>
