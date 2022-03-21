<cfscript>
    result = true;
    try {
        // the transaction with ORM task
        transaction {   
            ormGetSession();
        }
        // the transaction with query
        transaction {
           queryExecute( "SELECT 1 as one" );
        }
    }
    catch(any e) {
        result = false;
    }
    writeOutput(result);
</cfscript>