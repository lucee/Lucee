<cftry>
	<cfstoredproc procedure="spThrowException" returnCode="YES">
		<cfprocparam type="in" cfsqltype="cf_sql_varchar" value="Invalid" dbvarname="@unit" />
		<cfprocparam type="out" cfsqltype="cf_sql_varchar" variable="output" dbvarname="@output" />
	</cfstoredproc>

	<cfcatch type="any">
		<cfoutput>
			#serializeJSON(["ExceptionType:#cfcatch.type#","ExceptionMessage:#cfcatch.message#"])#
		</cfoutput>
	</cfcatch>
</cftry>