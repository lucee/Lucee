<cfoutput>
	<cftry>
		<cfquery name="LDEV6305">
            SELECT *
            FROM LDEV6305
            WHERE float_value = <cfqueryparam value="2.01" cfsqltype="CF_SQL_FLOAT">
		</cfquery>
		#LDEV6305.recordcount#
		<cfcatch type="any">
			#cfcatch.stacktrace#
		</cfcatch>
	</cftry>
</cfoutput>
