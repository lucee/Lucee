<cfscript>

    param name="form.scene" default="";

    if(form.scene == 1) {
        writeOutput(form.fieldNames);
    }

    if(structKeyExists(url, "urlstruct")) {
        writeOutput(url.urlstruct);
    }

</cfscript>