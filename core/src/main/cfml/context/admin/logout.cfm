<cfset StructDelete(application, "stText")>
<cfset structDelete(application, "external")>
<cfset StructDelete(session,"password"&request.adminType)>
<cfcookie expires="Now" name="lucee_admin_pw_#request.adminType#" value="">
<cflocation url="#cgi.SCRIPT_NAME#" addtoken="No">