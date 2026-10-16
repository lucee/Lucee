<cfscript>
	try {
		// First save with dates set - should work
		isOkay = entityNew( "DateEntity" );
		isOkay.setRecordDate( now() );
		isOkay.setSomeDate( now() );
		entitySave( isOkay );
		ormFlush();

		// Second save with NO dates (null) - this breaks
		willBreak = entityNew( "DateEntity" );
		// Don't set recordDate or someDate - they should be null
		entitySave( willBreak );
		ormFlush();

		writeOutput( "success" );
	}
	catch( any e ) {
		writeOutput( e.stacktrace );
	}
</cfscript>
