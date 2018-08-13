<cfset path = ListDeleteAt(getCurrenttemplatepath(), listLen(getCurrenttemplatepath(), "\"), "\") />
<cfset colName = "collectionName">

<cftry>
	<cfcollection action= "Create" collection="#colName#" path= "#path#" language="">
	<cfindex collection="#colName#" action="list" key="#path#" type="path" urlpath="#path#" extensions=".cfm,.cfc,.pdf,.docx" >
	<cfcatch type="any">
		<cfset result ="#cfcatch.message#" />
		<cfoutput>#result#</cfoutput>
	</cfcatch>
</cftry>
