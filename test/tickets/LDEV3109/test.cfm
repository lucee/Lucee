<cfparam name="FORM.scene" default="">

<cfif FORM.scene == 1>
	<cfquery name="res" datasource="ldev3109_DSN">
		INSERT INTO ldev3109 (numbers) VALUES (<cfqueryparam null="true" value="" maxlength="15" scale="2">)
		SELECT * FROM ldev3109
	</cfquery>
	<cfoutput>#res.recordcount#</cfoutput>
</cfif>
<cfif FORM.scene == 2>
	<cftry>
		<cfquery name="res" datasource="ldev3109_DSN">
			INSERT INTO ldev3109 (numbers) VALUES (<cfqueryparam cfsqltype="cf_sql_decimal" null="true" value="" maxlength="15" scale="2">)
			SELECT * FROM ldev3109
		</cfquery>
		<cfoutput>#res.recordcount#</cfoutput>
		<cfcatch>
			<cfoutput>#cfcatch.message#</cfoutput>
		</cfcatch>
	</cftry>
</cfif>

	