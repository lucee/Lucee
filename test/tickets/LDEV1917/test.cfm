
<cfstoredproc procedure="LDEV1917SP">
	<cfprocparam type = "IN"   CFSQLType = "NVARCHAR" value = "" null=false>
</cfstoredproc>
<cfquery name="test1917">
	select * from LDEV1917
</cfquery>
<cfoutput>#isEmpty(test1917.null_Value)#</cfoutput>