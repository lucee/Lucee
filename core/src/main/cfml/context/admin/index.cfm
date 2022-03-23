<cfscript>
	if(getApplicationSettings().adminMode=="single") {
		include "web.cfm";
	}
	else {
		location url="web.cfm" addtoken="no";
	}
</cfscript>