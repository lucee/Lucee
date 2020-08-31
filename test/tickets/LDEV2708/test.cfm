<cfoutput>
	<cfquery name="selectQuery" datasource="LDEV2708">
		SELECT GetDate() as myDate
	</cfquery>
	<cftry>
		<cfquery name="insertQuery" datasource="LDEV2708" result="result">
			INSERT INTO LDEV2708(
			    when_created
			)
			SELECT <CFQUERYPARAM value="#selectQuery.myDate#" cfsqltype="cf_sql_varchar"/> AS when_created
		</cfquery>
		#result.recordcount#
		<cfcatch type="any">
			#cfcatch.message#
		</cfcatch>
	</cftry>
</cfoutput>