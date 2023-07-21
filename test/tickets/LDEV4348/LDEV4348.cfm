<cfscript>
	settings = getApplicationSettings();
	echo( serializeJson(settings.xmlFeatures) );
</cfscript>