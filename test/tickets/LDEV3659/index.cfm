<cfscript>
	transaction {
		newPerson = entityNew( "Person", {
			"id"       : createUUID(),
			"name"     : "Michael Born",
			"givenName": "Michael",
			"surname"  : "Born"
		});

		entitySave( newPerson );
		ormFlush();
		result = queryExecute( sql="SELECT * FROM persons",
			options={ timeout: 2 } 
		);
		echo( result.name );
	}
</cfscript>