<cfscript>
    param name="url.suppressFunction" default="";
    if ( len( url.suppressFunction ) eq 0 )
        settings = getApplicationSettings(); // default
    else 
        settings = getApplicationSettings( suppressFunction=url.suppressFunction );
    echo ( structKeyExists( settings, "myCustomUDF" ) );
</cfscript>