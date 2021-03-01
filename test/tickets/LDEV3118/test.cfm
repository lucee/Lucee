<cfparam name = "FORM.scene" default = "">
<cfparam name = "FORM.columnName" default = "">	
<cfparam name = "FORM.value" default = 0>
<cfparam name = "FORM.type" default = "">

<cfif  form.scene EQ 1  AND form.columnName NEQ "">
	<cftry>
		<cfquery name = 'create' datasource = "ldev3091_DSN">
			create table ldev3118 (#form.columnName# #form.type#)
		</cfquery>
		<cfquery name = "insert" datasource = "ldev3091_DSN">
			insert into ldev3118(#form.columnName#) values (<cfqueryparam value='#form.value#'>)
		</cfquery>
		<cfquery name = "res" datasource = "ldev3091_DSN">
			select #form.columnName# as a from ldev3118
		</cfquery>
		<cfoutput>#res.a#</cfoutput>						
		<cfcatch type = "any">
			<cfoutput>#cfcatch.message#</cfoutput>
		</cfcatch>
	</cftry>
</cfif>

<cfif  form.scene EQ 2 AND form.columnName NEQ "">
	<cftry>
		<cfquery name = 'create' datasource = "ldev3091_DSN">
			create table ldev3118 (#form.columnName# #form.type#)
		</cfquery>
		<cfquery name = "insert" datasource = "ldev3091_DSN">
			insert into ldev3118(#form.columnName#) values (<cfqueryparam value='#form.value#' cfsqltype='#form.paramType#'>)
		</cfquery>
		<cfquery name = "res" datasource = "ldev3091_DSN">
			select #form.columnName# as a from ldev3118
		</cfquery>
		<cfoutput>#res.a#</cfoutput>				
		<cfcatch type = "any">
			<cfoutput>#cfcatch.message#</cfoutput>
		</cfcatch>
	</cftry>
</cfif>
