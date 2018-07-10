<cfset q=QueryNew("id,name","Integer,VarChar",[[8,'Micha'],[55,'lucee'],[55,'ACF']])>

<cftry>
	<cfquery name="qoq" dbtype="query">
		SELECT 	count(*),id
		FROM 	q
		group 	by id
		ORDER 	BY name
	</cfquery>
	<cfcatch>
		<cfoutput>
			#cfcatch.detail#
		</cfoutput>
	</cfcatch>
</cftry>
