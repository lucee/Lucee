<cfquery name="qry">
	select name, name from usersDetails
</cfquery>
<cfoutput>
   #ListLen(qry.ColumnList)#
</cfoutput>
