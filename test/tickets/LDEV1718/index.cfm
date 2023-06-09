<cfscript>
    systemOutput( "", true);
    systemOutput( request.testTempFolder, true);
    systemOutput(expandPath( "/" & request.testTempFolder ), true);
    echo (expandPath( "/" & request.testTempFolder ) );
</cfscript>