<cfscript>
	try {
		test = entityNew( "test4150" );
		test.setName( "Steve" );
		test.setId( createUUID() );

		entitySave( test );
		ormFlush();
	
		writeOutput( "success" );
	}
	catch(any e) {
		writeoutput(e.message);
	}
</cfscript>