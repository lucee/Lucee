<cftransaction action="begin">
	<cftry>
		<cfquery name="qry1" datasource="testdb">
			update test SET name="lucee"  WHERE id = '1'
		</cfquery>
			<cftransaction action="commit" savepoint="SavePoint1"/>
		<cfquery name="qry2" datasource="testdb">
			update test SET nam = 'scott' WHERE id = '1'
		</cfquery>
			<cftransaction action="commit" savepoint="SavePoint2"/>
	<cfcatch type="any">
		<cftransaction action="commit" />
	</cfcatch>
	</cftry>
</cftransaction>

<cfquery name="qry3" datasource="testdb">
	Select * From test WHERE id = '1'
</cfquery>
<cfoutput>#qry3.name#</cfoutput>

<cfquery name="qry3" datasource="testdb">
	DROP table test 
</cfquery>

