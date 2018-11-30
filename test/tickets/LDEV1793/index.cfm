<cfquery name="result">
	select * from LDEV1793
</cfquery>
<cfscript>
entity = EntityLoadByPk( "entity", result.id );
writeOutput(isObject(entity));
</cfscript>