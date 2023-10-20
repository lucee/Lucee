<cfscript>
    structDelete(application, "stText");
    structDelete(application, "UpdateProvider");
    StructDelete(session,"password"&request.adminType);
    sessionInvalidate();
</cfscript>
<cfcookie expires="Now" name="lucee_admin_pw_#request.adminType#" value="">
<cflocation url="#cgi.SCRIPT_NAME#" addtoken="No">