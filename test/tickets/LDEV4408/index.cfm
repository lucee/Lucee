<cfscript>
	newMappings = getApplicationSettings().mappings;
	application action='update' mappings='#newMappings#';
	echo ( session.trackingId );
</cfscript>