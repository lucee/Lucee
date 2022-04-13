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
        result = queryExecute( "SELECT * FROM persons" );
        echo( result.name );
    }
</cfscript>