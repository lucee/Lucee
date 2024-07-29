<cfquery name="qry">
	select name, name from ldev1440
</cfquery>
<cfoutput>
   #ListLen(qry.ColumnList)#
</cfoutput>
