<cfscript>
if(getApplicationSettings().adminMode=="single") {
	location url="index.cfm" addtoken=false;
}
else {
	param name="request.adminType" default="server";
	include "web.cfm";
}
</cfscript>