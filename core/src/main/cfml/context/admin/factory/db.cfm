<cfinclude template="../resources/resources.cfm">

<cfset dir=GetDirectoryFromPath(getCurrenttemplatePath())>
<cfdump var="#dir#">
<cfset dir=GetDirectoryFromPath(cgi.script_name)>
<cfset dir=left(GetDirectoryFromPath(cgi.script_name),len(dir)-1)>
<cfset dir=replace(GetDirectoryFromPath(dir),"/",".","all")>

<cfdirectory directory="../dbdriver" action="list" name="dbdriver" filter="*.cfc">
<cfset drivers=struct()>
<cfoutput query="dbdriver">
	<cfset n=listFirst(dbdriver.name,".")>
	<cfif n NEQ "Driver">
		<cfset drivers[n]=createObject("component",dir&"content.dbdriver."&n)>
	</cfif>
</cfoutput>


<cfoutput><cfset serialize(drivers)></cfoutput>
<!--- 

<cfdump var="#drivers#">

<cffile action="write" file="../resources/text.cfm" output="#content#" addnewline="yes"> --->