<cfparam name="FORM.Scene" default="1">
<cfset data = queryNew("ID, DateJoined", "INTEGER, TIMESTAMP", [{ID=1, DateJoined="2017-01-03 10:57:54"}, {ID=2, DateJoined="2017-01-03 10:57:54"}, {ID=3, DateJoined="2017-01-03 10:57:54"}])>
<cfif FORM.Scene EQ 1>
	<cfset result = serializeJSON( data = data, queryFormat=false, useSecureJSONPrefix=true)>
<cfelse>
	<cfset jsonObject = serializeJSON(data)>
	<cfset result = deserializeJSON(json=jsonObject, strictmapping = true )>
</cfif>
<cfoutput>#result#</cfoutput>
