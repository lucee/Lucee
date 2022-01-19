<cfsetting showdebugoutput="false">
<cfscript>
	code = entityNew( 'Code' );
	code.setId( 1 );
	code.setCode( 'a' );
	entitySave( code );
	ormFlush();
	arr = entityLoad("Code", {code = "a"});

	code2 = entityNew( 'Code' );
	code2.setId( 2 );
	code2.setCode( 'b' );
	entitySave( code2 );

	code2.setCode( 'c' );
	entitySave( code2 );

	entityNameArray();

	EntityReload( entity=code );

	EntityToQuery( entity=code );

	EntityDelete( code );

	// trigger onEvict
	ormEvictEntity( "Code" );

	// trigger onClear
	ormClearSession();

	echo( len( application.ormEventLog ) );
</cfscript>
