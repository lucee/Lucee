<cfscript>
	if(getApplicationSettings().singleContext) {
		include "web.cfm";
	}
	else {
		location url="web.cfm?reinit=true" addtoken="no";
	}
</cfscript>