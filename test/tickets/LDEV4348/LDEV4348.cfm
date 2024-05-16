<cfscript>
	settings = getApplicationSettings();
	echo( settings.xmlFeatures.toJson() );
</cfscript>