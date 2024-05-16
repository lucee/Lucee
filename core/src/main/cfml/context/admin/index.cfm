<cfscript>
	if(getApplicationSettings().singleContext) {
		include "web.cfm";
	}
	else {
		location url="server.cfm?reinit=true" addtoken="no";
	}
</cfscript>