<cfscript>
	_test = queryNew("_id,_name,_cmpnyName","integer,varchar,varchar", [[01,'saravana', 'MitrahSoft'],[07, 'pothys','MitrahSoft'], [09, 'MichaelOffener','RASIA']]);
</cfscript>

<cftry>
	<cfset hasError = false>
	<cfquery name="qTest" dbtype="query">
		select * from _test
		where _id = <cfqueryparam cfsqltype="cf_sql_integer" value="" /> 
	</cfquery>
	<cfcatch>
		<cfset hasError = true>
	</cfcatch>
</cftry>
<cfoutput>#hasError#</cfoutput>
