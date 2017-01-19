<cfset myStruct = structNew()>

<cfset mystruct.id = 1>
<cfset mystruct.Name = "POTHYS">
<cfset mystruct.DESIGNATION = "Associate Software Engineer">
<cfset metadata = {id: {type:"string"}}>
<cfset mystruct.setMetadata(metadata)>
<cfoutput>#serializeJSON(myStruct)#</cfoutput>