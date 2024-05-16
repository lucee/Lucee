<cfscript>
    param name="url.onlySupported" default="";
    if ( len( url.onlySupported ) eq 0 )
        settings = getApplicationSettings(); // default
    else 
        settings = getApplicationSettings( onlySupported=url.onlySupported );
    echo ( structKeyExists( settings, "nonStandardSetting" ) );
</cfscript>