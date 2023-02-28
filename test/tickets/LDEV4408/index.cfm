<cfscript>
	newMappings = getApplicationSettings().mappings;
	application action='update' mappings='#newMappings#';

	echo ( structKeyExists(session, "trackingId" ) );
</cfscript>