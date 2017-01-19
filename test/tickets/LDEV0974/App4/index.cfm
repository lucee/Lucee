<cfset myStruct = structNew()>

<cfset mystruct.id = 1>
<cfset mystruct.Name = "POTHYS">
<cfset mystruct.DESIGNATION = "Associate Software Engineer">

<cfoutput>#serializeJSON(myStruct)#</cfoutput>