<cfparam name="FORM.scene" default="2">
<cfparam name="FORM.columnName" default="">	
<cfparam name="FORM.value" default="">

<cfif  form.scene EQ 1  AND form.columnName NEQ "">
	<cftry>
		<cfquery name="insert" datasource="LDEV3091">
			insert into ldev3091(#form.columnName#) values (<cfqueryparam value='#form.value#' >)
		</cfquery>
		<cfoutput>Success</cfoutput>						
	<cfcatch type="any">
		<cfoutput>#cfcatch.message#</cfoutput>
	</cfcatch>
	</cftry>
</cfif>

<cfif  form.scene EQ 2 AND form.columnName NEQ "">
	<cftry>
		<cfquery name="insert" datasource="LDEV3091">
			insert into ldev3091(#form.columnName#) values (<cfqueryparam value='#form.value#' cfsqltype='cf_sql_smallint'>)
		</cfquery>
		<cfoutput>Success</cfoutput>						
	<cfcatch type="any" name="e">
		<cfoutput>#cfcatch.message#</cfoutput>
	</cfcatch>
	</cftry>
</cfif>

