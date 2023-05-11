<cftry>
	<cfquery name="res">
		SELECT * FROM LDEV1680
	</cfquery>
	<cfoutput>
		#isSimpleValue(res.dateTimeoff_column[1])# |
		#dateTimeFormat(res.dateTimeoff_column[1], "dd/mm/yyyy hh:nn:ss")#
	</cfoutput>
	<cfcatch>
		<cfoutput>#cfcatch.message#</cfoutput>
	</cfcatch>
</cftry> 