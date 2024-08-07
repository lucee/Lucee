<cfoutput>
	<cftry>
		<cfquery name="qry">
			select * from LDEV1953
		</cfquery>
		#qry.id#
		<cfcatch type="any">
			#cfcatch.message#
		</cfcatch>
	</cftry>
</cfoutput>