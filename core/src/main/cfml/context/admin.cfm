<cfscript>
	if(getApplicationSettings().singleContext) {
		location url="admin/index.cfm" addtoken="no";
	}
	else {
		location url="admin/web.cfm" addtoken="no";
	}
</cfscript>