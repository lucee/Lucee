<cfscript>
	if(getApplicationSettings().adminMode=="single") {
		location url="admin/index.cfm" addtoken="no";
	}
	else {
		location url="admin/web.cfm" addtoken="no";
	}
</cfscript>