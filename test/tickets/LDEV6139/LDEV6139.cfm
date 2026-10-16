<cfscript>
	// LDEV-6139: releaseORM() skips closeAll() when flushAll() throws
	//
	// With flushAtRequestEnd=true, the flush happens in PageContextImpl.releaseORM().
	// If it throws (e.g. unique constraint violation), closeAll() is skipped and
	// ORM session connections are leaked.

	// seed a row first
	item1 = entityNew( "TestEntity" );
	item1.setName( "duplicate_name" );
	entitySave( item1 );
	ormFlush();
	ormClearSession();

	// now create a second entity with the same unique name
	// DON'T flush — let flushAtRequestEnd trigger the constraint violation in releaseORM()
	item2 = entityNew( "TestEntity" );
	item2.setName( "duplicate_name" );
	entitySave( item2 );

	// output active connections before request ends
	// releaseORM() will run after this script completes, triggering the flush error
	writeOutput( "before_release" );
</cfscript>
