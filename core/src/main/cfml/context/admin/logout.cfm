<cfscript>
    StructDelete(application, "stText");
    StructDelete(application, "UpdateProvider");
    if(structKeyExists(url, "full")) {
        systemOutput("=>"&request.adminType,1,1);
        StructDelete(session,"passwordweb");
        StructDelete(session,"passwordserver");
        cookie expires="Now" name="lucee_admin_pw_web" value="";
        cookie expires="Now" name="lucee_admin_pw_server" value="";
    }
    else {
        StructDelete(session,"password"&request.adminType);
        cookie expires="Now" name="lucee_admin_pw_#request.adminType#" value="";
    }
    location url="#cgi.SCRIPT_NAME#" addtoken="No";
</cfscript>