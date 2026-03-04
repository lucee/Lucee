<cfscript>
	/*
	 * LDEV-6129: reproduce the dead reconnect code bug naturally via after_transaction release mode.
	 *
	 * With connection.release_mode=after_transaction, Hibernate releases the physical connection
	 * after every ormFlush() (afterTransaction callback sets physicalConnection=null).
	 *
	 * On the next ORM call, isConnected()=false triggers the dead reconnect block in
	 * HibernateORMSession.getSessionAndConn() which calls s.reconnect() — always throws
	 * ResourceClosedException in Hibernate 5.6 — and leaks the dc acquired by sac.connect().
	 *
	 * No reflection required: ormFlush() commits the transaction → after_transaction fires.
	 *
	 * Expected before fix: ERROR: org.hibernate.ResourceClosedException
	 * Expected after fix:  ok
	 */

	// transaction 1: save and flush
	p1 = entityNew( "LDEV6129Person" );
	p1.setName( createUUID() );
	entitySave( p1 );
	ormFlush(); // commits → afterTransaction() → physicalConnection=null → isConnected()=false

	systemOutput( "multi_transaction: after first ormFlush, isConnected=#ormGetSession().isConnected()#", true );

	// transaction 2: next ORM call should trigger getSessionAndConn() with isConnected()=false
	try {
		people = entityLoad( "LDEV6129Person" );
		systemOutput( "multi_transaction: entityLoad after ormFlush returned #arrayLen( people )# rows", true );
		writeOutput( "ok" );
	} catch ( any e ) {
		writeOutput( "ERROR: #e.type# - #e.message#" );
		systemOutput( "multi_transaction: caught exception: #e.stacktrace#", true );
	}
</cfscript>
