<cfscript>
	if(getConfigSettings().mode=="single") {
		include "web.cfm";
	}
	else {
		location url="web.cfm" addtoken="no";
	}
</cfscript>