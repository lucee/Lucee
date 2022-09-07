<cfscript>
    ormGetSession().beginTransaction();
    writeoutput(isWithinTransaction());
</cfscript>