<cfoutput>
	<cftry>
		<cfquery name="qry">
			select * from users
		</cfquery>
		#qry.id#
		<cfcatch type="any">
			#cfcatch.message#
		</cfcatch>
	</cftry>
</cfoutput>