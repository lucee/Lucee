<cfscript>
	if(getConfigSettings().mode=="single") {
		location url="admin/index.cfm" addtoken="no";
	}
	else {
		location url="admin/web.cfm" addtoken="no";
	}
</cfscript>