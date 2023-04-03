<cfscript>
    function returnAUDF() {
        return ()=>["chaining method works"];
    }

    // Syntax error
    writeoutput(returnAUDF()().toList());
</cfscript>