<cfscript>
if(getApplicationSettings().singleContext) {
	location url="index.cfm?reinit=true" addtoken=false;
}
else {
	param name="request.adminType" default="server";
	include "web.cfm";
}
</cfscript>