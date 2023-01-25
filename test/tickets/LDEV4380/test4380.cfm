<cfscript>
    try {
        test = entityload( "test4380", 1, true);
        test.setA("modified A");
        ormflush();
        writeoutput("success");
    }
    catch(any e) {
        writeoutput(e.message);
    }
</cfscript>