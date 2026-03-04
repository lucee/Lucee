<cfscript>
	/*
	 * LDEV-6129: trigger a connection leak by causing flushAll() to throw at request end.
	 *
	 * 1. entitySave(p1) + ormFlush() — commits "test" to DB
	 * 2. entitySave(p2) with same unique name — no error yet (Hibernate doesn't query DB)
	 * 3. Request ends: flushAtRequestEnd=true → releaseORM() → flushAll() throws unique violation
	 *
	 * BUG: flushAll() and closeAll() are in the same try block in PageContextImpl.releaseORM().
	 * When flushAll() throws, closeAll() is skipped → DatasourceConnection dc is never returned.
	 */
	uniqueName = createUUID();

	p1 = entityNew( "LDEV6129Person" );
	p1.setName( uniqueName );
	entitySave( p1 );
	ormFlush(); // commit p1 to DB — now uniqueName exists with a unique constraint

	p2 = entityNew( "LDEV6129Person" );
	p2.setName( uniqueName ); // same name — will collide at flush time
	entitySave( p2 );
	systemOutput( "flush_leak.cfm: entitySave(p2) done, request ending now — expect flush error", true );
	// request ends here: auto-flush tries to INSERT p2, throws unique constraint violation
	// closeAll() is skipped → dc leaked
</cfscript>
