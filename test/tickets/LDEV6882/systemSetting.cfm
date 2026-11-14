<cfscript>
	stText = { evsp: {}, general: { days: "Days", hours: "Hours", minutes: "Minutes", seconds: "Seconds" } };

	function renderCodingTip( code, text ) {
		echo( "<code>#arguments.code#</code>" );
	}

	function formatForConsole( value ) {
		return { ev: arguments.value, sp: arguments.value };
	}

	function renderSettings( name, value ) {
		echo( "<settings>#arguments.value#</settings>" );
	}

	timeSpan = createTimeSpan( url.day, url.hour, url.minute, url.second, url.milli );
</cfscript>
<cfmodule template="#url.adminRoot#systemSetting.cfm" name="clientTimeout" value="#timeSpan#" access="false" description="test"></cfmodule>
