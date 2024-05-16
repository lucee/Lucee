<cfparam name="form.scene" default="">
<cfif form.scene eq 1>
	<cfset form.id = "1">
	<cfquery name="res" datasource="LDEV2581">
		SELECT * FROM LDEV2581 WHERE id = <cfqueryparam value="#form.id#" null="#isempty(form.id)#" cfsqltype="CF_SQL_TINYINT" maxlength="2">
	</cfquery>
	<cfoutput>#res.recordcount#</cfoutput>
<cfelseif form.scene eq 2>
	<cfset form.id = "">
	<cfquery name="res" datasource="LDEV2581">
		SELECT * FROM LDEV2581 WHERE id = <cfqueryparam value="#form.id#" null="#isempty(form.id)#" cfsqltype="CF_SQL_TINYINT" maxlength="2">
	</cfquery>
	<cfoutput>#res.recordcount#</cfoutput>
<cfelseif form.scene eq 3>
	<cfset form.id = "">
	<cfquery name="res" datasource="LDEV2581">
		SELECT * FROM LDEV2581 WHERE id = <cfqueryparam value="#form.id#" null="#isempty(form.id)#" maxlength="2">
	</cfquery>
	<cfoutput>#res.recordcount#</cfoutput>
<cfelseif form.scene eq 4>
	<cfset form.id = "">
	<cfquery name="res" datasource="LDEV2581">
		SELECT * FROM LDEV2581 WHERE id = <cfqueryparam value="#form.id#" cfsqltype="CF_SQL_TINYINT" null="#isempty(form.id)#">
	</cfquery>
	<cfoutput>#res.recordcount#</cfoutput>
</cfif>
