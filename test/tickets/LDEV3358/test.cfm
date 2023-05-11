<cfparam name="FORM.scene" default="2">
<cfparam name="FORM.columnName" default="">
<cfparam name="FORM.value" default="">
<cfif form.scene EQ 1  AND form.columnName NEQ "">
	<cftry>
		<cfquery name="select" datasource="LDEV3358">
			select * from LDEV3358 where #form.columnName#=<cfqueryparam value="#form.value#" cfsqltype="smallint" maxlength="5">
		</cfquery>
		<cfoutput>#select[form.columnName]#</cfoutput>
	<cfcatch type="any">
		<cfoutput>#cfcatch.message#</cfoutput>
	</cfcatch>
	</cftry>
</cfif>

<cfif form.scene EQ 2 AND form.columnName NEQ "">
	<cftry>
		<cfquery name="select" datasource="LDEV3358">
			select * from LDEV3358 where #form.columnName#=<cfqueryparam value="#form.value#" maxlength="5">
		</cfquery>
		<cfoutput>#select[form.columnName]#</cfoutput>
	<cfcatch type="any" name="e">
		<cfoutput>#cfcatch.message#</cfoutput>
	</cfcatch>
	</cftry>
</cfif>

