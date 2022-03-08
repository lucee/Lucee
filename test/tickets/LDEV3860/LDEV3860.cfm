<cfscript>
    param name="FORM.scene" default="";

    if (form.scene == 1) {
        try {
            transaction {
                ormGetSession();
                writeDump(foo); // variable [FOO] doesn't exist 
            }
        }
        catch(any e) {
            writeOutput(findNoCase("foo", e.message) != 0);
        }
    }
    

    try {
        if (form.scene == 2) obj = EntityNew("invalid_entity_name",{name:'test1',givenName:'test2'});
        
        if (form.scene == 3) {
            transaction {
                obj = EntityNew("invalid_entity_name",{name:'test3',givenName:'test4'});
            }
        }

        if (form.scene == 4) {
            transaction {
                writeDump(foo); // variable [FOO] doesn't exist
                entityload("person"); // any ORM stuff
            }
        }

        if (form.scene == 5) {
            transaction {
                entityload("person"); // any ORM stuff
                writeDump(foo); // variable [FOO] doesn't exist
            }
        }

        if (form.scene == 6) {
            transaction {
                entitynew("person"); // any ORM stuff
                queryExecute("SELECT * FROM persons"); // datasource query
                writeDump(foo); // variable [FOO] doesn't exist
            }
        }

        if (form.scene == 7) {
            transaction {
                ormGetSession(); // ormGetSession()
                queryExecute( "SELECT name FROM Persons" ); // datasource query
                writeDump(foo);
            }
        }
    }
    catch (any e) {
        writeoutput(e.message);
    }
</cfscript>