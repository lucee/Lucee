<cfparam name="scene" default="">

<cfif form.scene eq 1>
	<cfquery name="qGetData">
		SELECT 1 AS id, null AS value
		UNION
		SELECT 2 AS id, 'foo' AS value;
	</cfquery>
	<cfoutput>#qGetData.value[1]#</cfoutput>
<cfelse>
	<cfquery name="qGetData1" returntype="array">
		SELECT 1 AS id, null AS value
		UNION
		SELECT 2 AS id, 'foo' AS value;
	</cfquery>
	<cfoutput>#qGetData1[1].value#</cfoutput>
</cfif>

	


