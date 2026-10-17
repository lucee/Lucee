<cfset qry = queryNew( "a", "varchar", [ [ "x" ], [ "y" ] ] )>
<cfset attrs = { name: "res", dbtype: "query" }>
<cftry>
	<cfset value = "">
	<cffinally>
		<cfquery attributeCollection="#attrs#">select * from qry</cfquery>
	</cffinally>
</cftry>
<cfoutput>#res.recordcount#</cfoutput>
