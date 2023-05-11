<cfsetting showdebugoutput="false">
<cfscript>
	code = entityNew( 'Code' );
	code.setId( 1 );
	code.setCode( 'a' );
	// should trigger preInsert, postInsert, and onFlush
	entitySave( code );

	// should trigger onFlush event
	ormFlush();
	arr = entityLoad("Code", {code = "a"});

	code2 = entityNew( 'Code' );
	code2.setId( 2 );
	code2.setCode( 'b' );

	// should trigger preInsert and postInsert
	entitySave( code2 );

	code2.setCode( 'c' );
	// should trigger preInsert and postInsert
	entitySave( code2 );

	// should trigger a preLoad and postLoad event
	EntityReload( entity=code );

	// should trigger preDelete, onDelete, and postDelete
	EntityDelete( code );
	ormFlush();

	// should trigger onEvict
	ormEvictEntity( "Code", 1 );

	// trigger onClear
	ormClearSession();

	echo( len( application.ormEventLog ) );
</cfscript>
