<cfscript>
    try {
        query name="res"{
            echo("select 1");
        }
        
        transaction {
            // transaction without any query activites
        }
        writeoutput("success");
    }
    catch(any e) {
        writeoutput(e.message);
    }
</cfscript>