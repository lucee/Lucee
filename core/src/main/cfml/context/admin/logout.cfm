<cfset StructDelete(application, "stText")>
<cfset StructDelete(session,"password"&request.adminType)>
<cfcookie expires="Now" name="lucee_admin_pw_#server.lucee.version#_#request.adminType#" value="">
<cflocation url="#cgi.SCRIPT_NAME#" addtoken="No">