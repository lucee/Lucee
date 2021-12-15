<cfscript>
	transaction{

		myEntity = entityNew( "Person", {
			"name" : "John",
			"id" : createUUID()
		} );

		entitySave( myEntity );
		ormFlush();

		transactionRollback();
	}
	result = queryExecute( "SELECT givenName,surname FROM Persons WHERE Id=:id", { id : myEntity.getId() } );
	echo(result.recordCount);
	
</cfscript>