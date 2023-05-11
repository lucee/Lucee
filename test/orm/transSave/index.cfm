<cfscript>
	transaction{
		myEntity = entityNew( "Person", {
			"name" : "John",
			"id" : createUUID()
		} );
		entitySave( myEntity );
	}
	result = queryExecute( "SELECT givenName,surname FROM Persons WHERE Id=:id", { id : myEntity.getId() } );
	echo(result.recordCount);
</cfscript>