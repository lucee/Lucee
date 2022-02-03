<cfscript>
    try {
        transaction {
            ormGetSession();
            writeDump(foo); // variable [FOO] doesn't exist 
        }
    }
    catch(any e) {
        writeOutput(findNoCase("foo", e.message) != 0);
    }
</cfscript>