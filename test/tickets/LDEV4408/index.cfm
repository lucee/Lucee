<cfscript>
	sessions = {};
	sessions.before = duplicate( session );
	//dump(session);
	newMappings = getApplicationSettings().mappings;
	application action='update' mappings='#newMappings#';

	sessions.after = duplicate( session );
	//dump(session);

	content type="application/json";
	echo( sessions.toJson() );
</cfscript>