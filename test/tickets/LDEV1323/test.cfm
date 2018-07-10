<cftransaction> 
	<cftry>
		<cfquery name="qry1" datasource="sampledsn">
			update users1323 SET FirstName = 'lucee' WHERE Title = 'sample'
		</cfquery>
		<cftransaction action="commit" />
		<cftransaction action="setsavepoint" savepoint="SavePoint1"/>

		<cfquery name="qry2" datasource="sampledsn">
			update user SET FirstName = 'CF' WHERE Title = 'sample'
		</cfquery>
		<cftransaction action="commit" />
		<cftransaction action="setsavepoint" savepoint="SavePoint2"/>

		<cfcatch type="any">
			<cftransaction action="rollback" />
		</cfcatch>
	</cftry>	
</cftransaction>

<cfquery name="qry3" datasource="sampledsn">
	Select * From users1323 WHERE Title = 'sample'
</cfquery>
<cfoutput>
	#qry3.FirstName# 
</cfoutput>