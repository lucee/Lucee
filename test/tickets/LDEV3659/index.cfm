<cfscript>
	transaction {
		newPerson = entityNew( "LDEV3597", {
			"id"       : createUUID(),
			"name"     : "Michael Born",
			"givenName": "Michael",
			"surname"  : "Born"
		});

		entitySave( newPerson );
		ormFlush();
		result = queryExecute( sql="SELECT * FROM LDEV3597",
			options={ timeout: 2 }
		);
		echo( result.name );
	}
</cfscript>