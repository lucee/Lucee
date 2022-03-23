<cfscript>
	if(getApplicationSettings().singleContext) {
		include "web.cfm";
	}
	else {
		location url="web.cfm" addtoken="no";
	}
</cfscript>