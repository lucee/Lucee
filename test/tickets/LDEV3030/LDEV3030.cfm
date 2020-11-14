<cfparam name = "form.scene" default = "">
<cfparam name = "form.datasource" default = "">

<cfif form.scene eq 1 OR form.scene eq 3>
	<cftry>
		<cfquery name="res" datasource="#form.datasource#">
			declare @results table ([id] int null, [metadata] xml null);

			INSERT INTO @results  [id], [metadata] )
			VALUES (
				1, <CFQUERYPARAM cfsqltype="cf_sql_sqlxml" value="<metadata><param /></metadata>" />
			)

			SELECT * FROM @results
		</cfquery>
		<cfoutput>#res.metadata#</cfoutput>
		<cfcatch name="e">
			<cfoutput>#e.message#</cfoutput>
		</cfcatch>
	</cftry>
</cfif>

<cfif form.scene eq 2 OR form.scene eq 4>
	<cftry>
		<cfquery name="res" datasource="#form.datasource#">
			declare @results table ([id] int null, [metadata] xml null);

			INSERT INTO @results ( [id], [metadata] )
			VALUES	(
				1, <CFQUERYPARAM cfsqltype="cf_sql_sqlxml" null="true" />
			)

			SELECT * FROM @results
		</cfquery>
		<cfoutput>#res.id#</cfoutput>
		<cfcatch name="e">
			<cfoutput>#e.message#</cfoutput>
		</cfcatch>
	</cftry>
</cfif>