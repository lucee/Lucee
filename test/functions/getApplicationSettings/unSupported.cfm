<cfscript>
    supportedSettings = getApplicationSettings( suppressFunction=true, onlySupported=true );
    allSettings = getApplicationSettings( suppressFunction=true );

    // filter out all settings are supported by lucee core
    unsupported = allSettings.filter(function( key, value ) {
        return !supportedSettings.keyExists( arguments.key );
    });

    echo ( structKeyArray( unsupported ).toJson() ); // i.e just [ 'nonStandardSetting', 'useJavaAsRegexEngine' ] (but in uppercase)
</cfscript>