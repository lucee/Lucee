<cfscript>
    param name="url.mainString" default="";
    param name="url.subString" default="";
    param name="url.replacement" default="";
    result = "";
    try {
        result = ReplaceNoCase(url.mainString, url.subString, url.replacement);
    } catch (any e) {  
        writeOutput(e.Message);
    }
    writeOutput(result);
</cfscript>